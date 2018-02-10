package com.zubtsov.elasticsearchsample1.upload;

import com.zubtsov.elasticsearchsample1.upload.elasticsearch.ElasticsearchItemWriter;
import com.zubtsov.elasticsearchsample1.upload.elasticsearch.EmailMessageToXContentBuilder;
import com.zubtsov.elasticsearchsample1.upload.outlook.EmailMessage;
import com.zubtsov.elasticsearchsample1.upload.outlook.OutlookItemReader;
import com.zubtsov.elasticsearchsample1.upload.solr.EmailMessageToSolrInputDocument;
import com.zubtsov.elasticsearchsample1.upload.solr.SolrItemWriter;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
    @Qualifier("Upload e-mails to Elasticsearch job")
    public Job uploadEmailsToElasticsearch(@Qualifier("Upload e-mails to Elasticsearch step") Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Elasticsearch")
                .incrementer(new RunIdIncrementer()) //what is it? TODO: clarify
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Elasticsearch step")
    public Step retrieveAndStoreEmailsElasticsearch(@Qualifier("Outlook reader") ItemReader<EmailMessage> outlookItemReader,
                                                    @Qualifier("Elasticsearch processor") ItemProcessor<EmailMessage, XContentBuilder> processor,
                                                    @Qualifier("Elasticsearch writer") ItemWriter<XContentBuilder> elasticsearchItemWriter) {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Elasticsearch transport client")
                .<EmailMessage, XContentBuilder>chunk(25) //TODO: select proper chunk size
                .reader(outlookItemReader)
                .processor(processor)
                .writer(elasticsearchItemWriter)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Solr job")
    public Job uploadEmailsToSolr(@Qualifier("Upload e-mails to Solr step") Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Solr")
                .incrementer(new RunIdIncrementer()) //what is it? TODO: clarify
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Solr step")
    public Step retrieveAndStoreEmailsSolr(@Qualifier("Outlook reader") ItemReader<EmailMessage> outlookItemReader,
                                       @Qualifier("Solr processor") ItemProcessor<EmailMessage, SolrInputDocument> processor,
                                       @Qualifier("Solr writer") ItemWriter<SolrInputDocument> elasticsearchItemWriter) {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Elasticsearch transport client")
                .<EmailMessage, SolrInputDocument>chunk(25) //TODO: select proper chunk size
                .reader(outlookItemReader)
                .processor(processor)
                .writer(elasticsearchItemWriter)
                .build();
    }

    @Bean
    @Qualifier("Outlook reader")
    public OutlookItemReader outlookItemReader() {
        return new OutlookItemReader();
    }

    @Bean
    @Qualifier("Elasticsearch processor")
    public EmailMessageToXContentBuilder elasticsearchItemProcessor() {
        return new EmailMessageToXContentBuilder();
    }

    @Bean
    @Qualifier("Elasticsearch writer")
    public ElasticsearchItemWriter elasticsearchItemWriter() {
        return new ElasticsearchItemWriter();
    }

    @Bean
    @Qualifier("Solr processor")
    public EmailMessageToSolrInputDocument solrItemProcessor() {
        return new EmailMessageToSolrInputDocument();
    }

    @Bean
    @Qualifier("Solr writer")
    public SolrItemWriter solrItemWriter() {
        return new SolrItemWriter();
    }

    //TODO: implement closing connection. use same approach for elasticsearch API
    @Bean
    public SolrClient httpSolrClient(@Value("${solr.url}") String solrUrl) {
        return new HttpSolrClient.Builder(solrUrl)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }
}
