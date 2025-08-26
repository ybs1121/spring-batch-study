//package com.system.batch.batchsystem.chunk;
//
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//public class SampleChunkJobConfig {
//
//    @Bean
//    public Step processStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
//        return new StepBuilder("processStep", jobRepository)
//                .<CustomerDetail, CustomerSummary>chunk(10, transactionManager)  // 청크 지향 처리 활성화
//                .reader(itemReader())       // 데이터 읽기 담당
//                .processor(itemProcessor()) // 데이터 처리 담당
//                .writer(itemWriter())      // 데이터 쓰기 담당
//                .build();
//    }
//
//    @Bean
//    public Job customerProcessingJob(JobRepository jobRepository, Step processStep) {
//        return new JobBuilder("customerProcessingJob", jobRepository)
//                .start(processStep)  // processStep으로 Job 시작
//                .build();
//    }
//
//}
