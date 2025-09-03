package com.example.risk_assessment_service.controller;


import com.example.risk_assessment_service.DTO.RiskRequest;
import com.example.risk_assessment_service.DTO.RiskResponse;
import com.example.risk_assessment_service.service.RiskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin // Allows frontend to call this API. For production, be more specific.
public class RiskController {

    private final RiskService riskService;

    @Autowired
    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    @PostMapping("/assess")
    public ResponseEntity<RiskResponse> assessRisk(@RequestBody RiskRequest request, HttpServletRequest httpServletRequest) {
        // Get the client's IP address from the request
        String ipAddress = httpServletRequest.getRemoteAddr();

        // Pass the IP address to the service
        RiskResponse response = riskService.assessRisk(request, ipAddress);
        return ResponseEntity.ok(response);
    }
}