package com.example.social.dto.response.location;

import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ResLocationDTO {
    private Long id;
    private String displayName;
    private String name;
    private String shortName;
    private Double lat;
    private Double lon;
    private String type;
    private Map<String, String> address;
}

