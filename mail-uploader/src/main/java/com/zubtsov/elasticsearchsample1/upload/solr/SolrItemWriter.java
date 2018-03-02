package com.zubtsov.elasticsearchsample1.upload.solr;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterWrite;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

//TODO: make it restartable
public class SolrItemWriter implements ItemStreamWriter<SolrInputDocument> {

    private static final Logger logger = LoggerFactory.getLogger(SolrItemWriter.class);

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

    @AfterWrite
    private void afterWrite() {
        logger.debug("Items has been written successfully to Solr");
    }
}
