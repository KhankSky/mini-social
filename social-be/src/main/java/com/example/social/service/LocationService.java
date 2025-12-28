package com.example.social.service;

import com.example.social.dto.response.location.ResLocationDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {
    
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";
    private static final String USER_AGENT = "SocialMediaApp/1.0";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public LocationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 
     * @param query 
     * @param limit 
     * @return 
     */
    public List<ResLocationDTO> searchLocations(String query, Integer limit) {
        if (query == null || query.trim().length() < 2) {
            return new ArrayList<>();
        }
        
        int maxResults = (limit != null && limit > 0 && limit <= 10) ? limit : 5;
        
        try {
            String url = NOMINATIM_BASE_URL + "/search?" +
                    "q=" + java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8) +
                    "&format=json" +
                    "&addressdetails=1" +
                    "&limit=" + maxResults +
                    "&accept-language=vi,en";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return new ArrayList<>();
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            List<ResLocationDTO> locations = new ArrayList<>();
            
            if (jsonNode.isArray()) {
                for (JsonNode item : jsonNode) {
                    ResLocationDTO location = parseLocationItem(item);
                    if (location != null) {
                        locations.add(location);
                    }
                }
            }
            
            return locations;
        } catch (Exception e) {
            System.err.println("Error searching locations: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Reverse geocoding - lấy địa chỉ từ tọa độ
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @return Thông tin địa điểm
     */
    public ResLocationDTO reverseGeocode(Double lat, Double lon) {
        if (lat == null || lon == null) {
            return null;
        }
        
        try {
            String url = NOMINATIM_BASE_URL + "/reverse?" +
                    "lat=" + lat +
                    "&lon=" + lon +
                    "&format=json" +
                    "&addressdetails=1" +
                    "&accept-language=vi,en";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);
            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    String.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseLocationItem(jsonNode);
        } catch (Exception e) {
            System.err.println("Error reverse geocoding: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse một item từ Nominatim response
     */
    private ResLocationDTO parseLocationItem(JsonNode item) {
        try {
            String displayName = item.has("display_name") ? item.get("display_name").asText() : "";
            String name = item.has("name") ? item.get("name").asText() : "";
            String type = item.has("type") ? item.get("type").asText() : "";
            Long placeId = item.has("place_id") ? item.get("place_id").asLong() : null;
            
            Double lat = null;
            Double lon = null;
            if (item.has("lat")) {
                try {
                    lat = Double.parseDouble(item.get("lat").asText());
                } catch (NumberFormatException e) {
                }
            }
            if (item.has("lon")) {
                try {
                    lon = Double.parseDouble(item.get("lon").asText());
                } catch (NumberFormatException e) {
                }
            }
            

            Map<String, String> addressMap = new HashMap<>();
            if (item.has("address")) {
                JsonNode addressNode = item.get("address");
                addressNode.fields().forEachRemaining(entry -> {
                    addressMap.put(entry.getKey(), entry.getValue().asText());
                });
            }
            

            String shortName = name;
            if (shortName == null || shortName.isEmpty()) {
                if (addressMap.containsKey("city")) {
                    shortName = addressMap.get("city");
                } else if (addressMap.containsKey("town")) {
                    shortName = addressMap.get("town");
                } else if (addressMap.containsKey("village")) {
                    shortName = addressMap.get("village");
                } else if (addressMap.containsKey("suburb")) {
                    shortName = addressMap.get("suburb");
                } else if (!displayName.isEmpty()) {
                    shortName = displayName.split(",")[0];
                }
            }
            
            return ResLocationDTO.builder()
                    .id(placeId)
                    .displayName(displayName)
                    .name(name.isEmpty() ? displayName.split(",")[0] : name)
                    .shortName(shortName)
                    .lat(lat)
                    .lon(lon)
                    .type(type)
                    .address(addressMap)
                    .build();
        } catch (Exception e) {
            System.err.println("Error parsing location item: " + e.getMessage());
            return null;
        }
    }
}

