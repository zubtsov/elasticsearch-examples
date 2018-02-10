package com.zubtsov.elasticsearchsample1.upload.elasticsearch;

import com.zubtsov.elasticsearchsample1.upload.outlook.EmailMessage;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.batch.item.ItemProcessor;

//TODO: use mapstruct
public class EmailMessageToXContentBuilder implements ItemProcessor<EmailMessage, XContentBuilder> {
    @Override
    public XContentBuilder process(EmailMessage message) throws Exception {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("Folder", message.getFolder())
                .field("From", message.getFrom())
                .field("Subject", message.getSubject())
                .field("Sent date", message.getSentDate())
                .field("Received Date", message.getReceivedDate())
                .field("Recipients", message.getRecipients())
                .field("Reply to", message.getReplyTo())
                .field("Message Content", message.getContent())
                .endObject();
        return builder;
    }
}
