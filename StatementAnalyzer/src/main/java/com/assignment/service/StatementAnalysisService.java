package com.assignment.service;

import com.assignment.entity.AnalysisResult;
import com.assignment.entity.FlaggedTransaction;
import com.assignment.entity.RiskAnalysis;
import com.assignment.entity.Transaction;
import com.assignment.entity.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class StatementAnalysisService {

    public AnalysisResult analyze(List<Transaction> transactions) {

        LocalDate today = LocalDate.now();
        Set<String> seenTxnIds = new HashSet<>();

        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;

        List<Transaction> validTransactions = new ArrayList<>();

        for (Transaction txn : transactions) {

            if (txn.getTransactionId() == null || seenTxnIds.contains(txn.getTransactionId()))
                continue;

            if (txn.getDate() == null || txn.getDate().isAfter(today))
                continue;

            if (txn.getAmount() == null)
                continue;

            seenTxnIds.add(txn.getTransactionId());
            validTransactions.add(txn);

            if (txn.getType() == TransactionType.CREDIT) {
                totalCredit = totalCredit.add(txn.getAmount());
            } else if (txn.getType() == TransactionType.DEBIT) {
                totalDebit = totalDebit.add(txn.getAmount());
            }
        }

        BigDecimal balance = totalCredit.subtract(totalDebit);

        RiskAnalysis riskAnalysis = calculateRisk(validTransactions, totalCredit);

        return new AnalysisResult(totalCredit, totalDebit, balance, riskAnalysis);
    }

    private RiskAnalysis calculateRisk(List<Transaction> txns, BigDecimal totalCredit) {

        List<FlaggedTransaction> flagged = new ArrayList<>();
        BigDecimal threshold = totalCredit.multiply(new BigDecimal("0.20"));

        for (Transaction txn : txns) {
            if (txn.getType() == TransactionType.DEBIT &&
                    txn.getAmount().compareTo(threshold) > 0) {

                flagged.add(new FlaggedTransaction(
                        txn.getTransactionId(),
                        "Debit amount " + txn.getAmount() +
                                " exceeds 20% of total income."
                ));
            }
        }

        return new RiskAnalysis(!flagged.isEmpty(), flagged);
    }
}
