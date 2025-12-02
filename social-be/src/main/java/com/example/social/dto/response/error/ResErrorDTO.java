package com.example.social.dto.response.error;

import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResErrorDTO {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private Object message;
    private String path;
}
