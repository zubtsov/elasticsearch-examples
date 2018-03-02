package com.zubtsov.elasticsearchsample1.upload.outlook;

import com.sun.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.event.MessageCountListener;
import java.util.concurrent.atomic.AtomicInteger;

import static com.zubtsov.elasticsearchsample1.upload.outlook.EmailFoldersPartitioner.FOLDER_KEY;

//TODO: we suppose that messages won't be deleted
//TODO: add common counter for all messages
public class PartitionedOutlookReader implements ItemStreamReader<Message> {

    private static final Logger logger = LoggerFactory.getLogger(PartitionedOutlookReader.class);

    private static final AtomicInteger totalMessages = new AtomicInteger(0);

    @Autowired
    private Store mailStore;

    @Autowired
    private MessageCountListener messageListener;

    private static final String FOLDER_INDEX_KEY = "folder_index";
    private static final String MESSAGE_INDEX_KEY = "message_index";

    //TODO: refactor!
    private ThreadLocal<String[]> folderNames = new ThreadLocal<>();
    private ThreadLocal<Folder[]> folders = new ThreadLocal<>(); //parallel to folderNames

    private ThreadLocal<Message[][]> messages = new ThreadLocal<>();

    private ThreadLocal<Integer> currentFolderIndex = new ThreadLocal<>();
    private ThreadLocal<Integer> currentMessageIndex = new ThreadLocal<>();

    @Override
    public Message read() {
        if (currentFolderIndex.get() == folders.get().length) { //TODO: maybe use equals()?
            return null;
        }

        if (currentMessageIndex.get() == messages.get()[currentFolderIndex.get()].length) {
            if (currentFolderIndex.get() != folders.get().length - 1) {
                currentFolderIndex.set(currentFolderIndex.get() + 1);
                currentMessageIndex.set(0);
            } else {
                return null;
            }
        }

        totalMessages.incrementAndGet();
        currentMessageIndex.set(currentMessageIndex.get() + 1);
        return messages.get()[currentFolderIndex.get()][currentMessageIndex.get() - 1];
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        currentFolderIndex.set(0);
        currentMessageIndex.set(0);

        if (executionContext.containsKey(FOLDER_INDEX_KEY)) {
            currentFolderIndex.set(executionContext.getInt(FOLDER_INDEX_KEY));
        }

        if (executionContext.containsKey(MESSAGE_INDEX_KEY)) {
            currentMessageIndex.set(executionContext.getInt(MESSAGE_INDEX_KEY));
        }

        folderNames.set((String[]) executionContext.get(FOLDER_KEY));

        folders.set(new Folder[folderNames.get().length]);

        for (int i = 0; i < folderNames.get().length; i++) {
            String folderName = folderNames.get()[i];
            try {
                folders.get()[i] = mailStore.getFolder(folderName);
            } catch (MessagingException e) {
                logger.error("Error occured while getting folder", folderName);
                logger.error("", e);
            }
        }

        for (int i = 0; i < folders.get().length; i++) {
            Folder folder = folders.get()[i];
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_ONLY);
                }
            } catch (MessagingException e) {
                logger.error("Error occured while opening folder {}", folderNames.get()[i]);
                logger.error("", e);
            }
        }

        logger.debug("Successfully opened {} folders", folders.get().length);

        messages.set(new Message[folders.get().length][]);

        for (int i = 0; i < folders.get().length; i++) {
            String folderName = folderNames.get()[i];
            try {
                Message[] messagesForFolder = folders.get()[i].getMessages();
                messages.get()[i] = messagesForFolder;
                logger.debug("Loaded {} messages from folder {}", messagesForFolder.length, folderName);
            } catch (MessagingException e) {
                logger.error("Error occured while getting messages for folder {}", folderName);
                logger.error("", e);
            }
        }

        //add listeners
        for (Folder folder : folders.get()) {
            folder.addMessageCountListener(messageListener);
            try {
                ((IMAPFolder) folder).idle();
            } catch (MessagingException e) {
                logger.error("Cannot turn on idle mode");
            }
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(FOLDER_INDEX_KEY, currentFolderIndex.get());
        executionContext.putInt(MESSAGE_INDEX_KEY, currentMessageIndex.get());
    }

    @Override
    public void close() throws ItemStreamException {
        logger.debug("Closing reader. Total messages: {}", totalMessages.get());
        for (int i = 0; i < folders.get().length; i++) {
            Folder folder = folders.get()[i];
            try {
                folder.close();
            } catch (MessagingException e) {
                logger.error("Error occured while closing folder {}", folderNames.get()[i]);
                logger.error("", e);
            }
        }

        logger.debug("Successfully closed {} folders", folders.get().length);
    }
}
