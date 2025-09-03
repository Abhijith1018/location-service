package com.example.risk_assessment_service.DTO;

import lombok.Data;
@Data
public class RiskRequest {
    private String userId;
    private int mouseScore;
    private LocationData location;

    @Data
    public static class LocationData {
        private double latitude;
        private double longitude;
    }
}
