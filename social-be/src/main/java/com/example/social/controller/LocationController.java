package com.example.social.controller;

import com.example.social.dto.response.location.ResLocationDTO;
import com.example.social.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    
    private final LocationService locationService;
    
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }
    
    /**
     * Tìm kiếm địa điểm
     * GET /api/locations/search?q={query}&limit={limit}
     */
    @GetMapping("/search")
    public ResponseEntity<List<ResLocationDTO>> searchLocations(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", required = false, defaultValue = "5") Integer limit) {
        
        List<ResLocationDTO> locations = locationService.searchLocations(query, limit);
        return ResponseEntity.ok(locations);
    }
    
    /**
     * Reverse geocoding - lấy địa chỉ từ tọa độ
     * GET /api/locations/reverse?lat={latitude}&lon={longitude}
     */
    @GetMapping("/reverse")
    public ResponseEntity<ResLocationDTO> reverseGeocode(
            @RequestParam("lat") Double lat,
            @RequestParam("lon") Double lon) {
        
        ResLocationDTO location = locationService.reverseGeocode(lat, lon);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }
}

