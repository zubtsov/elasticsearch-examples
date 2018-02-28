package com.zubtsov.elasticsearchsample1.upload;

import com.zubtsov.elasticsearchsample1.upload.elasticsearch.ElasticsearchItemWriter;
import com.zubtsov.elasticsearchsample1.upload.elasticsearch.EmailMessageToXContentBuilder;
import com.zubtsov.elasticsearchsample1.upload.outlook.EmailMessage;
import com.zubtsov.elasticsearchsample1.upload.outlook.OutlookItemReader;
import com.zubtsov.elasticsearchsample1.upload.solr.EmailMessageToSolrInputDocument;
import com.zubtsov.elasticsearchsample1.upload.solr.SolrItemWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableBatchProcessing
public class JobsConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @Autowired
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor()); //TODO: refactor using IOC
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Bean
    @Qualifier("Upload e-mails to Elasticsearch job")
    public Job uploadEmailsToElasticsearch(@Qualifier("Upload e-mails to Elasticsearch step") Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Elasticsearch")
                .incrementer(new RunIdIncrementer())
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Elasticsearch step")
    public Step retrieveAndStoreEmailsElasticsearch(@Qualifier("Outlook reader") ItemReader<EmailMessage> outlookItemReader,
                                                    @Qualifier("Elasticsearch processor") ItemProcessor<EmailMessage, XContentBuilder> processor,
                                                    @Qualifier("Elasticsearch writer") ItemWriter<XContentBuilder> elasticsearchItemWriter) {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Elasticsearch transport client")
                .<EmailMessage, XContentBuilder>chunk(10)
                .reader(outlookItemReader)
                .processor(processor)
                .writer(elasticsearchItemWriter)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Solr job")
    public Job uploadEmailsToSolr(@Qualifier("Upload e-mails to Solr step") Step retrieveAndStoreEmails) throws Exception {
        return jobBuilderFactory.get("Upload e-mails to Solr")
                .incrementer(new RunIdIncrementer())
                .start(retrieveAndStoreEmails)
                .build();
    }

    @Bean
    @Qualifier("Upload e-mails to Solr step")
    public Step retrieveAndStoreEmailsSolr(@Qualifier("Outlook reader") ItemReader<EmailMessage> outlookItemReader,
                                       @Qualifier("Solr processor") ItemProcessor<EmailMessage, SolrInputDocument> processor,
                                       @Qualifier("Solr writer") ItemWriter<SolrInputDocument> solrItemWriter) {
        return stepBuilderFactory.get("Retrieve e-mail via IMAP and store via Solr HTTP client")
                .<EmailMessage, SolrInputDocument>chunk(10)
                .reader(outlookItemReader)
                .processor(processor)
                .writer(solrItemWriter)
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

    @Bean(destroyMethod = "close") //by default
    public HttpSolrClient httpSolrClient(@Value("${solr.url}") String solrUrl) {
        return new HttpSolrClient.Builder(solrUrl)
                .withConnectionTimeout(10000)
                .withSocketTimeout(60000)
                .build();
    }

    @Bean(destroyMethod = "close") //by default
    public TransportClient elasticTransportClient(@Value("${elasticsearch.cluster.name}") String clusterName,
                                                  @Value("${elasticsearch.host}") String elasticHost,
                                                  @Value("${elasticsearch.port}") int elasticPort) throws UnknownHostException {
        TransportClient client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", clusterName).build());
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(elasticHost), elasticPort));
        return client;
    }
}
