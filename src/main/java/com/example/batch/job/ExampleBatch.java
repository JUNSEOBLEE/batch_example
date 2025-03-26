package com.example.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * version : 1.0
 * author : JUNSEOB
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ExampleBatch {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job exampleBatchJob() {
        return new JobBuilder("exampleBatchJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(exampleBatchStep())
                .build();
    }

    @Bean
    public Step exampleBatchStep() {
        return new StepBuilder("exampleBatchStep", jobRepository)
                .tasklet(exampleBatchTask(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet exampleBatchTask() {
        return ((contribution, chunkContext) -> {
                String message = chunkContext.getStepContext().getStepExecution().getJobParameters().getString("message");
                log.info("============ Message : {} ============", message);
                return RepeatStatus.FINISHED;
            }
        );
    }
}
