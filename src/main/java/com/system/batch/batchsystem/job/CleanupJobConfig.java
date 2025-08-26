package com.system.batch.batchsystem.job;


import com.system.batch.batchsystem.tesklet.CleanupTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class CleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public CleanupJobConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Tasklet cleanupTasklet() {
        return new CleanupTasklet();
    }

    @Bean
    public Step cleanupStep() {
        return new StepBuilder("cleanupStep", jobRepository)
                .tasklet(cleanupTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job cleanupJob() {
        return new JobBuilder("cleanupJob", jobRepository)
                .start(cleanupStep()).build();
    }
}
