package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;

@EnableBatchProcessing
@EnableScheduling
@Configuration
//TODO: use relative path or classpath
@PropertySource("file:D:\\Git\\elasticsearch-sample\\mail-uploader\\src\\main\\resources\\application.properties")
public class UploaderConfiguration {
    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public ElasticsearchItemWriter elasticsearchItemWriter() {
        return new ElasticsearchItemWriter();
    }

    @Bean
    public OutlookItemReader outlookItemReader() {
        return new OutlookItemReader();
    }

    //TODO: replace to cron expression & handle exceptions
    @Scheduled(fixedDelay = 300000)
    public void uploadEmails() throws Exception {
        System.out.println("Uploading e-mails...");
        jobLauncher.run(uploadEmailsToElasticsearch(retrieveAndStoreEmails()), new JobParameters()); //TODO: refactor
    }

    @Bean
    public Job uploadEmailsToElasticsearch(Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Elasticsearch")
                .incrementer(new RunIdIncrementer()) //what is it? TODO: clarify
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    public Step retrieveAndStoreEmails() {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Elasticsearch transport client")
                .<XContentBuilder, XContentBuilder>chunk(50) //TODO: select proper chunk size
                .reader(outlookItemReader())
                .writer(elasticsearchItemWriter())
                .build();
    }
}
