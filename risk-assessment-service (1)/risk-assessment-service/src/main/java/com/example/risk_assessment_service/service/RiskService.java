package com.example.risk_assessment_service.service;

import com.example.risk_assessment_service.DTO.RiskRequest;
import com.example.risk_assessment_service.DTO.RiskResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
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

    /**
     * Assesses the risk of a transaction based on multiple parameters.
     * @param request The incoming data from the frontend.
     * @param ipAddress The user's IP address, captured by the Controller.
     * @return A RiskResponse with a decision and score.
     */
    public RiskResponse assessRisk(RiskRequest request, String ipAddress) {
        UserProfile knownProfile = userDatabase.get(request.getUserId());
        if (knownProfile == null) {
            // Default response for an unknown user.
            return new RiskResponse("Challenge", 75.0);
        }

        // --- 1. Calculate Individual Risk Scores (0-100 scale) ---

        // GEOLOCATION RISK (Existing)
        double distance = calculateDistance(
                request.getLocation().getLatitude(), request.getLocation().getLongitude(),
                knownProfile.getLatitude(), knownProfile.getLongitude()
        );
        double geoRisk = calculateGeoRiskScore(distance);

        // MOUSE RISK (Existing)
        double mouseRisk = calculateMouseRiskScore(request.getMouseScore());

        // VPN/PROXY RISK (New - Based on IP Address)
        boolean isVpn = isVpnOrProxy(ipAddress);
        double vpnRisk = isVpn ? 100 : 0;

        // DEVICE INTEGRITY / EMULATOR RISK (New - Based on Frontend Signal)
        boolean isVirtual = request.getDeviceData() != null && request.getDeviceData().isVirtualDevice();
        double deviceIntegrityRisk = isVirtual ? 100 : 0;

        // Log all individual scores for the presentation
        System.out.printf(
                "Distance: %.2f km (Geo Risk: %.0f), Mouse Score: %d (Mouse Risk: %.0f), IP: %s (VPN Risk: %.0f), Is Virtual Device: %b (Device Risk: %.0f)%n",
                distance, geoRisk, request.getMouseScore(), mouseRisk, ipAddress, vpnRisk, isVirtual, deviceIntegrityRisk
        );

        // --- 2. Calculate Final Weighted Score (Re-balanced for new parameters) ---
        final double GEO_WEIGHT = 0.25;          // 25%
        final double VPN_WEIGHT = 0.15;          // 15%
        final double DEVICE_INTEGRITY_WEIGHT = 0.10; // 10%
        final double MOUSE_WEIGHT = 0.50;          // 50%

        double totalRiskScore = (geoRisk * GEO_WEIGHT) +
                (vpnRisk * VPN_WEIGHT) +
                (deviceIntegrityRisk * DEVICE_INTEGRITY_WEIGHT) +
                (mouseRisk * MOUSE_WEIGHT);

        System.out.printf("Total Weighted Risk Score: %.2f%n", totalRiskScore);

        // --- 3. Make a Final Decision Based on Thresholds ---
        String decision;
        if (totalRiskScore < 30) {
            decision = "Allow";
        } else if (totalRiskScore < 65) {
            decision = "Challenge";
        } else {
            decision = "Deny";
        }
        return new RiskResponse(decision, totalRiskScore);
    }

    /**
     * Placeholder for a real VPN/Proxy detection service call.
     * For the demo, we check against a pre-populated list of IPs.
     * @param ipAddress The user's IP address.
     * @return true if the IP is on our "bad" list.
     */
    private boolean isVpnOrProxy(String ipAddress) {
        // This is our pre-populated list for the demo.
        // In a real system, you would call a third-party API here.
        Set<String> knownVpnIps = Set.of(
                "103.24.13.15",   // Example public VPN IP
                "207.97.227.239", // Example proxy IP
                "0:0:0:0:0:0:0:1",  // Common localhost IPv6 address
                "127.0.0.1"       // Common localhost IPv4 address
                // To test this feature yourself, find your computer's IP on your local network
                // (e.g., 192.168.1.10) and add it to this list.
        );
        System.out.println("Checking IP Address: " + ipAddress);
        return knownVpnIps.contains(ipAddress);
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