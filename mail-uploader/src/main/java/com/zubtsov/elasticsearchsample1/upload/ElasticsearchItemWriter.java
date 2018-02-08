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

import java.net.InetAddress;
import java.util.List;

public class ElasticsearchItemWriter implements ItemWriter<XContentBuilder> {
    @Override
    public void write(List<? extends XContentBuilder> items) throws Exception {
        TransportClient client;
        client = new PreBuiltTransportClient(Settings.builder().put("cluster.name", "zubtsov-es-cluster").build());
        client.addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        bulkRequestBuilder.add(client.prepareIndex("outlookmail", "mail", "1")
                .setSource(items.get(0))
                .setOpType(DocWriteRequest.OpType.INDEX)
        );

        BulkResponse bulkResponse = bulkRequestBuilder.get();
    }
}
