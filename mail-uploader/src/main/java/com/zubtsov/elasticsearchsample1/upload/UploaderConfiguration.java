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

    private static final Logger logger = LoggerFactory.getLogger(UploaderConfiguration.class);

    @Autowired
    @Qualifier("imapToElasticPartitionedJob")
    private Job imapToElastic;

    @Autowired
    @Qualifier("imapToSolrPartitionedJob")
    private Job imapToSolr;

    @Autowired
    private JobLauncher jobLauncher;

    //TODO: handle exceptions
    //TODO: fix parallel job running
    //TODO: use job parameters
    @Scheduled(fixedDelay = 3600000) //TODO: replace to REST API call
    public void uploadEmails() throws Exception {
        logger.debug("Launching jobs...");
        jobLauncher.run(imapToElastic, new JobParameters());
//        jobLauncher.run(imapToSolr, new JobParameters());
        logger.debug("Jobs was successfully launched");
    }
}