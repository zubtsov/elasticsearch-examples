package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import javax.mail.*;
import java.io.IOException;
import java.util.Arrays;

//TODO: refactor + use MapStruct
public class MessageToEmailMessage implements ItemProcessor <Message, EmailMessage> {

    private static final Logger logger = LoggerFactory.getLogger(MessageToEmailMessage.class);

    @Override
    public EmailMessage process(Message message) {
        try {
            Object content = message.getContent();

            EmailMessage emailMessage = new EmailMessage();
            emailMessage.setFolder(message.getFolder().getName());

            Address[] from = message.getFrom();
            if (from != null) {
                emailMessage.setFrom(Arrays.stream(from).map(Address::toString).toArray(String[]::new));
            }

            emailMessage.setSubject(message.getSubject());
            emailMessage.setSentDate(message.getSentDate());
            emailMessage.setReceivedDate(message.getReceivedDate());

            Address[] recipients = message.getAllRecipients();
            if (recipients != null) {
                emailMessage.setRecipients(Arrays.stream(recipients).map(Address::toString).toArray(String[]::new));
            }

            Address[] replyTo = message.getReplyTo();
            if (replyTo != null) {
                emailMessage.setReplyTo(Arrays.stream(replyTo).map(Address::toString).toArray(String[]::new));
            }

            emailMessage.setContent(messageContentToString(content));
            return emailMessage;
        } catch (MessagingException | IOException e) {
            logger.warn("Skipping item {}", message);
            return null;
        }
    }

    private String messageContentToString(Object content) throws MessagingException, IOException {
        //TODO: handle other types of content (not text/plain=String)
        StringBuilder messageContentBuilder = new StringBuilder();

        if (content instanceof String) {
            messageContentBuilder.append((String) content);
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                Object bodyContent = bodyPart.getContent();
                if (bodyContent instanceof String) {
                    messageContentBuilder.append((String) bodyContent);
                }
            }
        }
        return messageContentBuilder.toString();
    }
}
