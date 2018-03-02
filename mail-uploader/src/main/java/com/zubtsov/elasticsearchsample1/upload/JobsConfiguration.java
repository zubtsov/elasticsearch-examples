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
import javax.mail.event.MessageCountListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

@Configuration
@EnableBatchProcessing
public class JobsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JobsConfiguration.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    //Storages
    @Bean(destroyMethod = "close") //by default
    public Store mailStore(@Value("${mail.server.host}") String mailServerHost,
                           @Value("${mail.server.protocol}") String mailServerProtocol,
                           @Value("${mail.user}") String user,
                           @Value("${mail.password}") String password) throws NoSuchProviderException, MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        props.setProperty("mail.imap.connectionpoolsize", "10"); //TODO: unhardcode
        Session mailSession = Session.getInstance(props);
        Store mailStore = mailSession.getStore(mailServerProtocol);
        mailStore.connect(mailServerHost, user, password);
        return mailStore;
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

    //Listeners
    @Bean
    public MessageCountListener messageListener() {
        return new EmailListener();
    }

    //Job launcher and jobs
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        return jobLauncher;
    }

    @Bean
    public Job imapToElasticPartitionedJob(@Qualifier("imapToElasticPartitionedStep") Step partitionedStep) {
        return jobBuilderFactory.get("Partitioned step")
                .start(partitionedStep)
                .build();
    }

    @Bean
    public Step imapToElasticPartitionedStep(@Qualifier("partitionedOutlookReader") ItemReader<Message> partitionedOutlookReader,
                               @Qualifier("outlookElasticProcessor") ItemProcessor<Message, XContentBuilder> compositeProcessor,
                               @Qualifier("elasticsearchWriter") ItemWriter<XContentBuilder> elasticsearchItemWriter,
                                @Qualifier("imapFoldersPartitioner") Partitioner partitioner) {
        Step slave = stepBuilderFactory.get("Slave step")
                .<Message, XContentBuilder>chunk(10)
                .reader(partitionedOutlookReader)
                .processor(compositeProcessor)
                .writer(elasticsearchItemWriter)
                .build();
        Step master = stepBuilderFactory.get("Master step")
                .partitioner("Slave step", partitioner)
                .gridSize(4)
                .step(slave) //TODO: inject
                .taskExecutor(new SimpleAsyncTaskExecutor()) //TODO: inject
                .build();
        return master;
    }

    @Bean
    public Job imapToSolrPartitionedJob(@Qualifier("imapToSolrPartitionedStep") Step partitionedStep) {
        return jobBuilderFactory.get("Partitioned step")
                .start(partitionedStep)
                .build();
    }

    @Bean
    public Step imapToSolrPartitionedStep(@Qualifier("partitionedOutlookReader") ItemReader<Message> reader,
                                             @Qualifier("outlookSolrProcessor") ItemProcessor<Message, SolrInputDocument> processor,
                                             @Qualifier("solrWriter") ItemWriter<SolrInputDocument> solrWriter,
                                             @Qualifier("imapFoldersPartitioner") Partitioner partitioner) {
        Step slave = stepBuilderFactory.get("Slave step")
                .<Message, SolrInputDocument>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(solrWriter)
                .build();
        Step master = stepBuilderFactory.get("Master step")
                .partitioner("Slave step", partitioner)
                .gridSize(4)
                .step(slave) //TODO: inject
                .taskExecutor(new SimpleAsyncTaskExecutor()) //TODO: inject
                .build();
        return master;
    }

    //Partitioners
    @Bean
    public Partitioner imapFoldersPartitioner(Store mailStore) {
        return new EmailFoldersPartitioner(mailStore);
    }

    //Readers
    @Bean
    public PartitionedOutlookReader partitionedOutlookReader() {
        return new PartitionedOutlookReader();
    }

    //Processors
    @Bean
    public CompositeItemProcessor<Message, XContentBuilder> outlookElasticProcessor(
            @Qualifier("outlookProcessor") ItemProcessor<Message, EmailMessage> outlookProcessor,
            @Qualifier("elasticProcessor") ItemProcessor<EmailMessage, XContentBuilder> elasticProcessor) {
        CompositeItemProcessor<Message, XContentBuilder> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(outlookProcessor, elasticProcessor));
        return processor;
    }

    @Bean
    public CompositeItemProcessor<Message, SolrInputDocument> outlookSolrProcessor(
            @Qualifier("outlookProcessor") ItemProcessor<Message, EmailMessage> outlookProcessor,
            @Qualifier("solrProcessor") ItemProcessor<EmailMessage, SolrInputDocument> solrProcessor) {
        CompositeItemProcessor<Message, SolrInputDocument> processor = new CompositeItemProcessor<>();
        processor.setDelegates(Arrays.asList(outlookProcessor, solrProcessor));
        return processor;
    }

    @Bean
    public MessageToEmailMessage outlookProcessor() {
        return new MessageToEmailMessage();
    }

    @Bean
    public EmailMessageToXContentBuilder elasticProcessor() {
        return new EmailMessageToXContentBuilder();
    }

    @Bean
    public EmailMessageToSolrInputDocument solrProcessor() {
        return new EmailMessageToSolrInputDocument();
    }

    //Writers
    @Bean
    public ElasticsearchItemWriter elasticsearchWriter() {
        return new ElasticsearchItemWriter();
    }

    @Bean
    public SolrItemWriter solrWriter() {
        return new SolrItemWriter();
    }

}
