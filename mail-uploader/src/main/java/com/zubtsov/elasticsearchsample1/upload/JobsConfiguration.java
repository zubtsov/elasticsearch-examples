package com.zubtsov.elasticsearchsample1.upload;

import com.zubtsov.elasticsearchsample1.upload.elasticsearch.ElasticsearchItemWriter;
import com.zubtsov.elasticsearchsample1.upload.elasticsearch.EmailMessageToXContentBuilder;
import com.zubtsov.elasticsearchsample1.upload.outlook.*;
import com.zubtsov.elasticsearchsample1.upload.solr.EmailMessageToSolrInputDocument;
import com.zubtsov.elasticsearchsample1.upload.solr.SolrItemWriter;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.mail.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

//TODO: check bean scopes
@Configuration
@EnableBatchProcessing
public class JobsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JobsConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    @Autowired
    public Partitioner emailFoldersPartitioner(Store mailStore) {
        return new EmailFoldersPartitioner(mailStore);
    }

    @Bean
    @Autowired
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor()); //TODO: refactor using IOC
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Bean(destroyMethod = "close") //by default
    @Autowired
    public Store mailStore(@Value("${mail.server.host}") String mailServerHost,
                           @Value("${mail.server.protocol}") String mailServerProtocol,
                           @Value("${mail.user}") String user,
                           @Value("${mail.password}") String password) throws NoSuchProviderException, MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session mailSession = Session.getInstance(props);
        Store mailStore = mailSession.getStore(mailServerProtocol);
        mailStore.connect(mailServerHost, user, password);
        return mailStore;
    }

    @Bean
    @Qualifier("Partitioned job")
    public Job partitionedUpload(@Qualifier("Partitioned step") Step partitionedStep) {
        return jobBuilderFactory.get("Partitioned step")
                .start(partitionedStep)
                .build();
    }

    @Bean
    @Qualifier("Partitioned step")
    public Step partitionedStep(@Qualifier("Partitioned outlook reader") ItemReader<Message> partitionedOutlookReader,
                               @Qualifier("Composite processor") ItemProcessor<Message, XContentBuilder> compositeProcessor,
                               @Qualifier("Elasticsearch writer") ItemWriter<XContentBuilder> elasticsearchItemWriter,
                                @Qualifier("Partitioner") Partitioner partitioner) {
        Step slave = stepBuilderFactory.get("Slave step")
                .<Message, XContentBuilder>chunk(10)
                .reader(partitionedOutlookReader)
                .processor(compositeProcessor)
                .writer(elasticsearchItemWriter)
                .build();
        Step master = stepBuilderFactory.get("Master step")
                .partitioner("Slave step", partitioner)
                .gridSize(4)
                .step(slave)
                .taskExecutor(new SimpleAsyncTaskExecutor()) //TODO: inject
                .build();
        return master;
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
    @Qualifier("Partitioner")
    @Autowired
    public Partitioner partitioner(Store mailStore) {
        return new EmailFoldersPartitioner(mailStore);
    }

    @Bean
    @Qualifier("Partitioned outlook reader")
    public PartitionedOutlookReader partitionedOutlookReader() {
        return new PartitionedOutlookReader();
    }

    @Bean
    @Qualifier("Outlook processor")
    public MessageToEmailMessage outlookProcessor() {
        return new MessageToEmailMessage();
    }

    @Bean
    @Qualifier("Composite processor")
    public CompositeItemProcessor<Message, XContentBuilder> compositeProcessor(
            @Qualifier("Outlook processor") ItemProcessor<Message, EmailMessage> outlookProcessor,
            @Qualifier("Elasticsearch processor") ItemProcessor<EmailMessage, XContentBuilder> elasticProcessor) {
        CompositeItemProcessor<Message, XContentBuilder> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(outlookProcessor, elasticProcessor));
        return processor;
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
