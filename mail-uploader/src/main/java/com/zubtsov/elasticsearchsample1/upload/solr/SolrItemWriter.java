package com.zubtsov.elasticsearchsample1.upload.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

//TODO: make it restartable
public class SolrItemWriter implements ItemWriter<SolrInputDocument>, ItemStream {

    @Value("${solr.collection.name}")
    private String collectionName;

    @Autowired
    private SolrClient solrClient;

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
    public void write(List<? extends SolrInputDocument> items) throws Exception {

        List<UpdateResponse> responses = new ArrayList<>();

        for (SolrInputDocument item : items) {
            responses.add(solrClient.add(collectionName, item));
        }

        solrClient.commit(collectionName);
    }
}
