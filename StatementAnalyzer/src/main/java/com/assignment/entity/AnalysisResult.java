package com.assignment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResult {
    private BigDecimal totalCredit;
    private BigDecimal totalDebit;
    private BigDecimal balance;
    private RiskAnalysis riskAnalysis;
}
