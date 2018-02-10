package com.zubtsov.elasticsearchsample1.upload.solr;

import com.zubtsov.elasticsearchsample1.upload.outlook.EmailMessage;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.batch.item.ItemProcessor;

import java.util.UUID;

//TODO: use mapstruct
public class EmailMessageToSolrInputDocument implements ItemProcessor<EmailMessage, SolrInputDocument> {
    @Override
    public SolrInputDocument process(EmailMessage message) throws Exception {
        SolrInputDocument doc = new SolrInputDocument();
        doc.addField("id", UUID.randomUUID().toString());
        doc.addField("folder", message.getFolder());
        doc.addField("from", message.getFrom());
        doc.addField("subject", message.getSubject());
        doc.addField("sent_date", message.getSentDate());
        doc.addField("received_date", message.getReceivedDate());
        doc.addField("recipients", message.getRecipients());
        doc.addField("reply_to", message.getReplyTo());
        doc.addField("content", message.getContent());
        return doc;
    }
}
