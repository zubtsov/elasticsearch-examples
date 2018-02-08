package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetAddress;
import java.util.List;

//TODO: refactor
public class ElasticsearchItemWriter implements ItemWriter<XContentBuilder> {

    private @Value("${elasticsearch.cluster.name}") String clusterName;
    private @Value("${elasticsearch.index.name}") String indexName;
    private @Value("${elasticsearch.type.name}") String typeName;
    private @Value("elasticsearch.host") String elasticHost;
    private @Value("elasticsearch.port") String elasticPort; //TODO: inject as int

    @Override
    public void write(List<? extends XContentBuilder> items) throws Exception {
        TransportClient client;
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", clusterName).build());
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(elasticHost), Integer.valueOf(elasticPort)));

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        bulkRequestBuilder.add(client.prepareIndex(indexName, typeName, "1")
                .setSource(items.get(0))
                .setOpType(DocWriteRequest.OpType.INDEX)
        );

        BulkResponse bulkResponse = bulkRequestBuilder.get();
    }
}
