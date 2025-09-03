package com.example.risk_assessment_service.service;



import com.example.risk_assessment_service.DTO.RiskRequest;
import com.example.risk_assessment_service.DTO.RiskResponse;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RiskService {

    // --- SIMULATED USER DATABASE ---
    private static final Map<String, UserProfile> userDatabase = new ConcurrentHashMap<>();
    static {
        // Known good location for the demo user (Hyderabad, Telangana, India)
        UserProfile user123 = new UserProfile(17.3850, 78.4867);
        userDatabase.put("user123", user123);
    }

    public RiskResponse assessRisk(RiskRequest request) {
        UserProfile knownProfile = userDatabase.get(request.getUserId());
        if (knownProfile == null) {
            return new RiskResponse("Challenge", 75.0);
        }

        // 1. Calculate Individual Risk Scores (0-100 scale)
        double distance = calculateDistance(
                request.getLocation().getLatitude(), request.getLocation().getLongitude(),
                knownProfile.getLatitude(), knownProfile.getLongitude()
        );
        double geoRisk = calculateGeoRiskScore(distance);
        double mouseRisk = calculateMouseRiskScore(request.getMouseScore());

        System.out.printf("Distance: %.2f km (Geo Risk: %.2f), Mouse Score: %d (Mouse Risk: %.2f)%n",
                distance, geoRisk, request.getMouseScore(), mouseRisk);

        // 2. Calculate Final Weighted Score
        final double GEO_WEIGHT = 0.40;
        final double MOUSE_WEIGHT = 0.60;
        double totalRiskScore = (geoRisk * GEO_WEIGHT) + (mouseRisk * MOUSE_WEIGHT);
        System.out.printf("Total Weighted Risk Score: %.2f%n", totalRiskScore);

        // 3. Make a Final Decision Based on Thresholds
        String decision;
        if (totalRiskScore < 35) {
            decision = "Allow";
        } else if (totalRiskScore < 65) {
            decision = "Challenge";
        } else {
            decision = "Deny";
        }
        return new RiskResponse(decision, totalRiskScore);
    }

    private double calculateGeoRiskScore(double distanceInKm) {
        if (distanceInKm < 50) return 0;
        if (distanceInKm < 500) return 60;
        return 100;
    }

    private double calculateMouseRiskScore(int score) {
        if (score < 30) return 0;
        if (score < 70) return 50;
        return 90;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class UserProfile {
        private double latitude;
        private double longitude;
    }
}
