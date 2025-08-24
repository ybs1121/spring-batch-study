package com.system.batch.batchsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchSystemApplication {

    public static void main(String[] args) {
        System.exit(SpringApplication.exit(SpringApplication.run(BatchSystemApplication.class, args)));
    }

}
