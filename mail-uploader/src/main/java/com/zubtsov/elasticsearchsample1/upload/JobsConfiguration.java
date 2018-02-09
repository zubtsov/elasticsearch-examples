package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class JobsConfiguration {

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

    @Bean
    @Qualifier("Upload e-mails to Elasticsearch job")
    public Job uploadEmailsToElasticsearch(@Qualifier("Upload e-mails to Elasticsearch step") Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Elasticsearch")
                .incrementer(new RunIdIncrementer()) //what is it? TODO: clarify
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Elasticsearch step")
    public Step retrieveAndStoreEmails(ItemReader<XContentBuilder> outlookItemReader, ItemWriter<XContentBuilder> elasticsearchItemWriter) {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Elasticsearch transport client")
                .<XContentBuilder, XContentBuilder>chunk(1) //TODO: select proper chunk size
                .reader(outlookItemReader)
                .writer(elasticsearchItemWriter)
                .build();
    }
}
