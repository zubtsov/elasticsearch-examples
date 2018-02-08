package com.zubtsov.elasticsearchsample1.upload;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String [] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(BatchConfiguration.class);
        JobLauncher launcher = context.getBean("jobLauncher", JobLauncher.class);
        launcher.run(context.getBean("job", Job.class), new JobParameters());
    }
}