package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.mail.Message;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmailListener extends MessageCountAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EmailListener.class);

    @Autowired
    @Qualifier("outlookProcessor")
    private ItemProcessor<Message, EmailMessage> outlookProcessor;

    @Autowired
    @Qualifier("elasticProcessor")
    private ItemProcessor<EmailMessage, XContentBuilder> elasticProcessor;

    @Autowired
    @Qualifier("elasticsearchWriter")
    ItemWriter<XContentBuilder> elasticsearchWriter;

    @Override
    public void messagesAdded(MessageCountEvent e) {
        logger.debug("{} new messages received", e.getMessages().length);
        Message[] messages = e.getMessages();

        List<XContentBuilder> messagesContents = Arrays.stream(messages).map(m -> {
            try {
                return elasticProcessor.process(outlookProcessor.process(m));
            } catch (Exception e1) {
                return null;
            }
        }).collect(Collectors.toList());

        try {
            elasticsearchWriter.write(messagesContents);
        } catch (Exception ex) {
            logger.error("Failed to add {} messages", messagesContents.size());
        }
    }
}
