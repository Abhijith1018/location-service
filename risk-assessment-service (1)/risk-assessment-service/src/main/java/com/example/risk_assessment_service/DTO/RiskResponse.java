package com.example.risk_assessment_service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RiskResponse {
    private String decision;
    private double riskScore;
}
