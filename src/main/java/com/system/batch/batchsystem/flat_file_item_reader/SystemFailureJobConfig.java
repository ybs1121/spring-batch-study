package com.system.batch.batchsystem.flat_file_item_reader;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Configuration
public class SystemFailureJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job systemFailureJob(Step systemFailureStep) {
        return new JobBuilder("systemFailureJob", jobRepository)
                .start(systemFailureStep)
                .build();
    }

    @Bean
    public Step systemFailureStep(
            FlatFileItemReader<SystemFailure> systemFailureFlatFileItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(systemFailureFlatFileItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureFlatFileItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureItemReader")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(",")
                .names("errorId",
                        "errorDateTime",
                        "severity",
                        "processId",
                        "errorMessage")
                .targetType(SystemFailure.class)
                .customEditors(Map.of(LocalDateTime.class, dateTimeEditor()))
                .linesToSkip(1)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile) {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureItemReader")
                .resource(new FileSystemResource(inputFile))
                .fixedLength()
                .columns(new Range[]{
                        new Range(1, 8),     // errorId: ERR001 + 공백 2칸
                        new Range(9, 29),    // errorDateTime: 날짜시간 + 공백 2칸
                        new Range(30, 39),   // severity: CRITICAL/FATAL + 패딩
                        new Range(40, 45),   // processId: 1234 + 공백 2칸
                        new Range(46, 66)    // errorMessage: 메시지 + \n
                })
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                .targetType(SystemFailure.class)
                .build();
    }

    @Bean
    public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }


    @Bean
    public Step systemFailureStep(
            MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(multiSystemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<SystemFailure> multiSystemFailureItemReader(
            @Value("#{jobParameters['inputFilePath']}") String inputFilePath) {

        return new MultiResourceItemReaderBuilder<SystemFailure>()
                .name("multiSystemFailureItemReader")
                .resources(new Resource[]{
                        new FileSystemResource(inputFilePath + "/critical-failures.csv"),
                        new FileSystemResource(inputFilePath + "/normal-failures.csv")
                })
                .delegate(systemFailureFileReader())
                .build();
    }

    @Bean
    public FlatFileItemReader<SystemFailure> systemFailureFileReader() {
        return new FlatFileItemReaderBuilder<SystemFailure>()
                .name("systemFailureFileReader")
                .delimited()
                .delimiter(",")
                .names("errorId", "errorDateTime", "severity", "processId", "errorMessage")
                .targetType(SystemFailure.class)
                .linesToSkip(1)
                .build();
    }


    @Data
    public static class SystemFailure {
        private String errorId;
        //        private String errorDateTime;
        private LocalDateTime errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }

    private PropertyEditor dateTimeEditor() {
        return new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                setValue(LocalDateTime.parse(text, formatter));
            }
        };
    }
}
