package com.assignment.service;

import com.assignment.entity.AnalysisJob;
import com.assignment.entity.AnalysisResult;
import com.assignment.entity.JobStatus;
import com.assignment.entity.Transaction;
import com.assignment.repository.InMemoryJobRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalysisOrchestratorService {

    private final StatementAnalysisService analysisService;
    private final InMemoryJobRepository repository;

    public AnalysisOrchestratorService(
            StatementAnalysisService analysisService,
            InMemoryJobRepository repository
    ) {
        this.analysisService = analysisService;
        this.repository = repository;
    }

    @Async
    public void processAsync(String jobId, List<Transaction> transactions) {
        AnalysisResult result = analysisService.analyze(transactions);

        AnalysisJob job = repository.findById(jobId).orElseThrow();
        job.setResult(result);
        job.setStatus(JobStatus.COMPLETED);
    }
}
