package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.*;
import java.util.Properties;
import java.util.TreeMap;

//TODO: refactor
public class OutlookItemReader implements ItemReader<XContentBuilder>, ItemStream {

    private static final String CURRENT_INDEX = "current.index";
    private static final String CURRENT_FOLDER_NAME = "current.folder.name";

    private String currentFolderName;
    private int currentMessageIndex = 0;

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
        try {
            Properties props = new Properties();
            props.setProperty("mail.imap.ssl.enable", "true");
            Session mailSession = Session.getInstance(props);
            mailSession.setDebug(true); //to turn on or not to turn on?
            mailStore = mailSession.getStore(mailServerProtocol);
            mailStore.connect(mailServerHost, user, password);

            for (Folder folder : mailStore.getDefaultFolder().list("*")) {
                if (folder.getMessageCount() != 0 && "INBOX".equals(folder.getName())) { //TODO: fix it
                    folder.open(Folder.READ_ONLY);
                    foldersMessages.put(folder.getFullName(), folder.getMessages());
                    folder.close();
                }
            }

            System.out.println("Done");
        } catch (MessagingException e) {
            //TODO: handle exception
            e.printStackTrace();
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
                mailStore.close();
            }
        } catch (MessagingException e) {
            //TODO: handle exception
        }
    }

    @Override
    public XContentBuilder read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        //TODO: save the current folder number and current message number
        //TODO: or maybe it's possible to read multiple messages into one XContentBuilder?
        //TODO: or maybe it's reasonable to use other object type to package messages together?
        //TODO: delete the following

        if (currentFolderName == null) {
            return null;
        }

        Message message = foldersMessages.get(currentFolderName)[currentMessageIndex];

        message.getFolder().open(Folder.READ_ONLY);

        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("From", message.getFrom())
                .field("Subject", message.getSubject())
                .field("Sent date", message.getSentDate())
                .field("Received Date", message.getReceivedDate())
                .endObject();

        message.getFolder().close();

        advanceToNextMessage();

        return builder;
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
}
