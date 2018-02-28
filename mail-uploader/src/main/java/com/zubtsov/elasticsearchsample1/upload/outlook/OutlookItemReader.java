package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.TreeMap;

//TODO: store state between runnings & get messages reveived later than last run
//TODO: ADD COMMON THREAD-SAFE ATOMIC COUNTER + TASK EXECUTOR
//TODO: refactor
//TODO: research possibility of parallel execution (e.g. one Thread per Folder)
//TODO: implement as Producer-Consumer using stack/queue?
public class OutlookItemReader implements ItemStreamReader<EmailMessage> {

    public static final Logger logger = LoggerFactory.getLogger(OutlookItemReader.class);

    private static final String CURRENT_INDEX = "current.index";
    private static final String CURRENT_FOLDER_NAME = "current.folder.name";

    private String currentFolderName;
    private int currentMessageIndex;

    private @Value("${mail.folder.name.pattern}")
    String folderNamePattern;
    private @Value("${mail.server.host}")
    String mailServerHost;
    private @Value("${mail.server.protocol}")
    String mailServerProtocol;
    private @Value("${mail.user}")
    String user;
    private @Value("${mail.password}")
    String password;

    private Store mailStore;

    private TreeMap<String, Message[]> foldersMessages = new TreeMap<>();

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        logger.debug("Opening reader...");
        try {
            Properties props = new Properties();
            props.setProperty("mail.imap.ssl.enable", "true");
            Session mailSession = Session.getInstance(props);
            mailStore = mailSession.getStore(mailServerProtocol);
            mailStore.connect(mailServerHost, user, password);

            for (Folder folder : mailStore.getDefaultFolder().list(folderNamePattern)) {
                if (folder.getMessageCount() != 0) { //TODO: refactor
                    folder.open(Folder.READ_ONLY);
                    foldersMessages.put(folder.getFullName(), folder.getMessages());
                    folder.close();
                }
            }
        } catch (MessagingException e) {
            logger.error("Error is occured while opening reader", e);
        }

        if (executionContext.containsKey(CURRENT_FOLDER_NAME)) {
            currentFolderName = executionContext.getString(CURRENT_FOLDER_NAME);
            if (executionContext.containsKey(CURRENT_INDEX)) {
                currentMessageIndex = new Long(executionContext.getLong(CURRENT_INDEX)).intValue();
            } else {
                currentMessageIndex = 0;
            }
        } else {
            currentFolderName = foldersMessages.firstKey();
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putLong(CURRENT_INDEX, new Long(currentMessageIndex).longValue());
        executionContext.putString(CURRENT_FOLDER_NAME, currentFolderName);
    }

    @Override
    public void close() throws ItemStreamException {
        try {
            if (mailStore != null) {
                logger.debug("Closing reader...");
                mailStore.close();
            }
        } catch (MessagingException e) {
            logger.error("Error is occured while closing reader", e);
        }
    }

    @Override
    public EmailMessage read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        //TODO: save the current folder number and current message number
        //TODO: or maybe it's possible to read multiple messages into one XContentBuilder?
        //TODO: or maybe it's reasonable to use other object type to package messages together?
        if (currentFolderName == null) {
            return null;
        }

        Message message = foldersMessages.get(currentFolderName)[currentMessageIndex];
        message.getFolder().open(Folder.READ_ONLY);
        EmailMessage emailMessage = messageToEmailMessage(message);
        message.getFolder().close();

        advanceToNextMessage();

        return emailMessage;
    }

    private EmailMessage messageToEmailMessage(Message message) throws MessagingException, IOException {
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

    private void advanceToNextMessage() {
        try {
            if (currentMessageIndex < mailStore.getFolder(currentFolderName).getMessageCount() - 1) {
                currentMessageIndex++;
            } else {
                currentMessageIndex = 0;
                currentFolderName = foldersMessages.higherKey(currentFolderName);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @AfterChunk
    private void afterChunk() {
        logger.debug("Chunk of emails has been loaded successfully");
    }
}
