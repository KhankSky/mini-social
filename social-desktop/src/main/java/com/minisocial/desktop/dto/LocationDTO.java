package com.minisocial.desktop.dto;

import java.util.Map;

public class LocationDTO {
    
    private Long id;
    private String displayName;
    private String name;
    private String shortName;
    private Double lat;
    private Double lon;
    private String type;
    private Map<String, String> address;
    
    // Constructors
    public LocationDTO() {}
    
    public LocationDTO(Long id, String displayName, String name, String shortName, 
                       Double lat, Double lon, String type, Map<String, String> address) {
        this.id = id;
        this.displayName = displayName;
        this.name = name;
        this.shortName = shortName;
        this.lat = lat;
        this.lon = lon;
        this.type = type;
        this.address = address;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    
    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Map<String, String> getAddress() { return address; }
    public void setAddress(Map<String, String> address) { this.address = address; }
    
    @Override
    public String toString() {
        return shortName != null ? shortName : displayName;
    }
}