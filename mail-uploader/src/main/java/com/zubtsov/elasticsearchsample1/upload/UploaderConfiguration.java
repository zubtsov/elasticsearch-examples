package com.zubtsov.elasticsearchsample1.upload;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@EnableScheduling
@Configuration
@PropertySource("classpath:application.properties")
@Import(JobsConfiguration.class)
public class UploaderConfiguration {

    @Autowired
    @Qualifier("Upload e-mails to Elasticsearch job")
    private Job uploadEmailsToElasticsearch;

    @Autowired
    @Qualifier("Upload e-mails to Solr job")
    private Job uploadEmailsToSolr;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    //TODO: replace to cron expression & handle exceptions
    @Scheduled(fixedDelay = 3600000)
    public void uploadEmails() throws Exception {
        System.out.println("Uploading e-mails...");
        jobLauncher.run(uploadEmailsToElasticsearch, new JobParameters()); //TODO: refactor
//        jobLauncher.run(uploadEmailsToSolr, new JobParameters()); //TODO: refactor
    }
}
