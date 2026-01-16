package com.assignment.service;

import com.assignment.entity.AnalysisJob;
import com.assignment.entity.JobStatus;
import com.assignment.entity.Transaction;
import com.assignment.repository.InMemoryJobRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisApplicationService {

    private final InMemoryJobRepository repository;
    private final AnalysisOrchestratorService orchestrator;

    public AnalysisApplicationService(
            InMemoryJobRepository repository,
            AnalysisOrchestratorService orchestrator
    ) {
        this.repository = repository;
        this.orchestrator = orchestrator;
    }

    public String submitForAnalysis(List<Transaction> transactions) {

        String jobId = UUID.randomUUID().toString();

        AnalysisJob job = new AnalysisJob();
        job.setJobId(jobId);
        job.setStatus(JobStatus.IN_PROGRESS);

        repository.save(job);

        orchestrator.processAsync(jobId, transactions);

        return jobId;
    }

    public AnalysisJob getJob(String jobId) {
        return repository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid jobId"));
    }
}

