package com.example.batch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

/**
 * version : 1.0
 * author : JUNSEOB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExampleBatchScheduler {
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final JobLauncher jobLauncher;
    @Qualifier("exampleBatchJob")
    private final Job exampleBatchJob;
    @Value("${batch.period.job}")
    private Integer jobPeriod;
    @Value("${batch.period.monitoring.ms}")
    private long monitoringPeriod;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private Map<String, ScheduledFuture<?>> scheduledFutureMap = new ConcurrentHashMap<>();
    private Map<String, Long> startTimeMap = new HashMap<>();

    public void startScheduler(String batchId) {
        if(!startTimeMap.containsKey(batchId)) {
            startTimeMap.put(batchId, System.currentTimeMillis());
        }
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(() -> {
            try {
                if(Thread.currentThread().isInterrupted()) {
                    log.info("Job for battId: {} was interrupted.", batchId);
                    return;
                }
                runBatchJob(batchId);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, jobPeriod, TimeUnit.SECONDS);
        scheduledFutureMap.put(batchId, scheduledFuture);
    }

    private void runBatchJob(String batchId) {
        try {
            JobParameters parameters = new JobParametersBuilder()
                    .addLong("runTime", System.currentTimeMillis())
                    .addString("batchId", batchId)
                    .addString("message", "This is Batch Example")
                    .toJobParameters();
            jobLauncher.run(exampleBatchJob, parameters);
            // 타임아웃 감시 스레드 시작
            monitorJobExecution(batchId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void monitorJobExecution(String batchId) {
        executorService.submit(() -> {
            long startTime = startTimeMap.get(batchId);
            if (System.currentTimeMillis() - startTime > monitoringPeriod) {
                log.info("Job for batchId: {} exceeded " + (monitoringPeriod / 1000 / 60) + " minutes. Stopping...", batchId);
                startTimeMap.remove(batchId);
                stopScheduler(batchId);
            }
        });
    }

    public void stopScheduler(String batchId) {
        // Job 중단
        interruptJob(batchId);
        ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(batchId);
        if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);  // 인터럽트 플래그 설정
            log.info("========== Interrupted Schedule batchId : {} ========== ", batchId);
        }
        scheduledFutureMap.remove(batchId);
    }

    private void interruptJob(String batchId) {
        Set<JobExecution> jobExecutions = jobExplorer.findRunningJobExecutions(exampleBatchJob.getName());
        Optional.ofNullable(jobExecutions).orElseThrow()
                .stream()
                .filter(jobExecution -> jobExecution.isRunning())
                .filter(jobExecution -> jobExecution.getJobParameters().getString("batchId").equals(batchId))
                .forEach(jobExecution -> {
                    try {
                        jobExecution.setStatus(BatchStatus.COMPLETED);
                        jobRepository.update(jobExecution);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
        log.info("========== Interrupted Job batchId : {} ========== ", batchId);
    }
}
