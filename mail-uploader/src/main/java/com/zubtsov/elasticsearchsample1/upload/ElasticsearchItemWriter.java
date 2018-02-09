package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.util.List;

//TODO: refactor & make writer restartable (bulk operations may partially fail)
public class ElasticsearchItemWriter implements ItemWriter<XContentBuilder>, ItemStream {

    private @Value("${elasticsearch.cluster.name}") String clusterName;
    private @Value("${elasticsearch.index.name}") String indexName;
    private @Value("${elasticsearch.type.name}") String typeName;
    private @Value("${elasticsearch.host}") String elasticHost;
    private @Value("${elasticsearch.port}") String elasticPort; //TODO: inject as int

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {

    }

    @Override
    public void write(List<? extends XContentBuilder> items) throws Exception {
        TransportClient client;
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", clusterName).build());
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(elasticHost), Integer.valueOf(elasticPort)));

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        for (XContentBuilder item : items) {
            bulkRequestBuilder.add(client.prepareIndex(indexName, typeName)
                    .setSource(item)
                    .setOpType(DocWriteRequest.OpType.INDEX)
            );
        }

        //TODO: handle the response
        BulkResponse bulkResponse = bulkRequestBuilder.get();
    }
}
