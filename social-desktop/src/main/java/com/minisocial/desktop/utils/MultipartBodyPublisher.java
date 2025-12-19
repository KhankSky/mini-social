package com.minisocial.desktop.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MultipartBodyPublisher {
    private final List<PartsSpecification> partsSpecificationList = new ArrayList<>();
    private final String boundary = UUID.randomUUID().toString();

    public HttpRequest.BodyPublisher build() {
        if (partsSpecificationList.isEmpty()) {
            throw new IllegalStateException("No parts specified.");
        }
        addFinalBoundaryPart();
        return HttpRequest.BodyPublishers.ofByteArrays(PartsIterator::new);
    }

    public String getBoundary() {
        return boundary;
    }

    public MultipartBodyPublisher addPart(String name, String value) {
        PartsSpecification partsSpecification = new PartsSpecification();
        partsSpecification.type = PartsSpecification.Type.STRING;
        partsSpecification.name = name;
        partsSpecification.value = value;
        partsSpecificationList.add(partsSpecification);
        return this;
    }

    public MultipartBodyPublisher addPart(String name, Path value) {
        PartsSpecification partsSpecification = new PartsSpecification();
        partsSpecification.type = PartsSpecification.Type.FILE;
        partsSpecification.name = name;
        partsSpecification.path = value;
        partsSpecificationList.add(partsSpecification);
        return this;
    }

    private void addFinalBoundaryPart() {
        PartsSpecification partsSpecification = new PartsSpecification();
        partsSpecification.type = PartsSpecification.Type.FINAL_BOUNDARY;
        partsSpecificationList.add(partsSpecification);
    }

    static class PartsSpecification {
        public enum Type {
            STRING, FILE, FINAL_BOUNDARY
        }

        Type type;
        String name;
        String value;
        Path path;
    }

    class PartsIterator implements java.util.Iterator<byte[]> {

        private java.util.Iterator<PartsSpecification> iter;
        private InputStream currentFileInput;

        private boolean done = false;
        private byte[] nextByteArray;

        PartsIterator() {
            iter = partsSpecificationList.iterator();
        }

        @Override
        public boolean hasNext() {
            if (done)
                return false;
            if (nextByteArray != null)
                return true;
            try {
                if (currentFileInput != null) {
                    byte[] buf = new byte[8192];
                    int r = currentFileInput.read(buf);
                    if (r > 0) {
                        byte[] actualBytes = new byte[r];
                        System.arraycopy(buf, 0, actualBytes, 0, r);
                        nextByteArray = actualBytes;
                        return true;
                    } else {
                        currentFileInput.close();
                        currentFileInput = null;
                        nextByteArray = "\r\n".getBytes(StandardCharsets.UTF_8);
                        return true;
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            if (iter.hasNext()) {
                PartsSpecification nextPart = iter.next();
                if (nextPart.type == PartsSpecification.Type.STRING) {
                    String part = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"" + nextPart.name + "\"\r\n" +
                            "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                            nextPart.value + "\r\n";
                    nextByteArray = part.getBytes(StandardCharsets.UTF_8);
                    return true;
                }
                if (nextPart.type == PartsSpecification.Type.FILE) {
                    Path path = nextPart.path;
                    String filename = path.getFileName().toString();
                    String contentType;
                    try {
                        contentType = Files.probeContentType(path);
                    } catch (IOException e) {
                        contentType = "application/octet-stream";
                    }
                    if (contentType == null)
                        contentType = "application/octet-stream";

                    String part = "--" + boundary + "\r\n" +
                            "Content-Disposition: form-data; name=\"" + nextPart.name + "\"; filename=\"" + filename
                            + "\"\r\n" +
                            "Content-Type: " + contentType + "\r\n\r\n";
                    nextByteArray = part.getBytes(StandardCharsets.UTF_8);

                    try {
                        currentFileInput = Files.newInputStream(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                    return true;
                }
                if (nextPart.type == PartsSpecification.Type.FINAL_BOUNDARY) {
                    nextByteArray = ("--" + boundary + "--").getBytes(StandardCharsets.UTF_8);
                    done = true;
                    return true;
                }
            }
            return false;
        }

        @Override
        public byte[] next() {
            if (!hasNext())
                throw new java.util.NoSuchElementException();
            byte[] buf = nextByteArray;
            nextByteArray = null;
            return buf;
        }
    }
}
