package com.system.batch.batchsystem.listener;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SampleJobConfiguration {
    @Bean
    public Job systemMonitoringJob(JobRepository jobRepository, Step monitoringStep) {
        return new JobBuilder("systemMonitoringJob", jobRepository)
                .listener(new BigBrotherJobExecutionListener())
                .start(monitoringStep)
                .build();
    }
}
