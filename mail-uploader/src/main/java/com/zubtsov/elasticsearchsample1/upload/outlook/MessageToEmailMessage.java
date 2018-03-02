package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.springframework.batch.item.ItemProcessor;

import javax.mail.*;
import java.io.IOException;
import java.util.Arrays;

public class MessageToEmailMessage implements ItemProcessor <Message, EmailMessage> {
    @Override
    public EmailMessage process(Message message) throws Exception {
        Object content = message.getContent();

        EmailMessage emailMessage = new EmailMessage();
        emailMessage.setFolder(message.getFolder().getName());
        emailMessage.setFrom(Arrays.stream(message.getFrom()).map(Address::toString).toArray(String[]::new));
        emailMessage.setSubject(message.getSubject());
        emailMessage.setSentDate(message.getSentDate());
        emailMessage.setReceivedDate(message.getReceivedDate());
        emailMessage.setRecipients(Arrays.stream(message.getAllRecipients()).map(Address::toString).toArray(String[]::new)); //TODO: fix NPE
        emailMessage.setReplyTo(Arrays.stream(message.getReplyTo()).map(Address::toString).toArray(String[]::new));
        emailMessage.setContent(messageContentToString(content));
        return emailMessage;
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
