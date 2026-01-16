package com.assignment.scenarios;

import com.assignment.entity.AnalysisResult;
import com.assignment.entity.Transaction;
import com.assignment.entity.TransactionType;
import com.assignment.service.StatementAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
public class StatementAnalysisServiceTest {

    @Autowired
    private StatementAnalysisService service;

    @Test
    public void shouldFlagDebitGreaterThan20PercentOfTotalCredit() {

        List<Transaction> transactions = List.of(
                new Transaction("TXN_1", LocalDate.now(),
                        new BigDecimal("10000"), TransactionType.CREDIT, "Salary"),
                new Transaction("TXN_2", LocalDate.now(),
                        new BigDecimal("3000"), TransactionType.DEBIT, "Rent")
        );

        AnalysisResult result = service.analyze(transactions);

        assertEquals(new BigDecimal("10000"), result.getTotalCredit());
        assertEquals(new BigDecimal("3000"), result.getTotalDebit());

        assertTrue(result.getRiskAnalysis().isHasSuspiciousActivity());
        assertEquals(1, result.getRiskAnalysis().getFlaggedTransactions().size());
    }

    @Test
    void shouldIgnoreDuplicateTransactionIds() {

        List<Transaction> transactions = List.of(
                new Transaction("DUP_TXN", LocalDate.now(),
                        new BigDecimal("5000"), TransactionType.CREDIT, "Salary"),
                new Transaction("DUP_TXN", LocalDate.now(),
                        new BigDecimal("2000"), TransactionType.DEBIT, "Fraud")
        );

        AnalysisResult result = service.analyze(transactions);

        assertEquals(new BigDecimal("5000"), result.getTotalCredit());
        assertEquals(BigDecimal.ZERO, result.getTotalDebit());
    }

    @Test
    void shouldSkipFutureDatedTransactions() {

        List<Transaction> transactions = List.of(
                new Transaction("FUTURE_TXN",
                        LocalDate.now().plusDays(10),
                        new BigDecimal("10000"),
                        TransactionType.CREDIT,
                        "Future Credit")
        );

        AnalysisResult result = service.analyze(transactions);

        assertEquals(BigDecimal.ZERO, result.getTotalCredit());
        assertEquals(BigDecimal.ZERO, result.getTotalDebit());
    }

    @Test
    void shouldSkipTransactionsWithNullAmount() {

        List<Transaction> transactions = List.of(
                new Transaction("TXN_NULL",
                        LocalDate.now(),
                        null,
                        TransactionType.DEBIT,
                        "Broken Data")
        );

        AnalysisResult result = service.analyze(transactions);

        assertEquals(BigDecimal.ZERO, result.getTotalDebit());
    }

}
