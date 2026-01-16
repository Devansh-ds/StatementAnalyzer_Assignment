package com.assignment.controller;

import com.assignment.entity.AnalysisJob;
import com.assignment.entity.JobStatus;
import com.assignment.entity.StatementRequest;
import com.assignment.service.AnalysisApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/analyze")
public class StatementAnalysisController {

    private final AnalysisApplicationService applicationService;

    public StatementAnalysisController(AnalysisApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> analyze(
            @Valid @RequestBody StatementRequest request) {

        String jobId = applicationService
                .submitForAnalysis(request.getTransactions());

        return ResponseEntity
                .accepted()
                .body(Map.of("jobId", jobId));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getResult(@PathVariable String jobId) {

        AnalysisJob job = applicationService.getJob(jobId);

        if (job.getStatus() == JobStatus.IN_PROGRESS) {
            return ResponseEntity.ok(Map.of("status", "IN_PROGRESS"));
        }

        return ResponseEntity.ok(job);
    }
}
