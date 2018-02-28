package com.zubtsov.elasticsearchsample1.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@EnableBatchProcessing
@EnableScheduling
@Configuration
@Import(JobsConfiguration.class)
@PropertySource("application.properties")
public class UploaderConfiguration {

    public static final Logger logger = LoggerFactory.getLogger(UploaderConfiguration.class);

    @Autowired
    @Qualifier("Upload e-mails to Elasticsearch job")
    private Job uploadEmailsToElasticsearch;

    @Autowired
    @Qualifier("Upload e-mails to Solr job")
    private Job uploadEmailsToSolr;

    @Autowired
    private JobLauncher jobLauncher;

    //TODO: handle exceptions
    //TODO: fix parallel job running
    @Scheduled(fixedDelay = 3600000)
    public void uploadEmails() throws Exception {
        logger.debug("Launching jobs...");
        jobLauncher.run(uploadEmailsToElasticsearch, new JobParameters());
        jobLauncher.run(uploadEmailsToSolr, new JobParameters());
        logger.debug("Jobs was successfully launched");
    }
}