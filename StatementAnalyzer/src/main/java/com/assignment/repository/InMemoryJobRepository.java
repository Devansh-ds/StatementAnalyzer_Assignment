package com.assignment.repository;

import com.assignment.entity.AnalysisJob;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryJobRepository {

    private final Map<String, AnalysisJob> store = new ConcurrentHashMap<>();

    public void save(AnalysisJob job) {
        store.put(job.getJobId(), job);
    }

    public Optional<AnalysisJob> findById(String jobId) {
        return Optional.ofNullable(store.get(jobId));
    }
}
