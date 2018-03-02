package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;

import static com.zubtsov.elasticsearchsample1.upload.outlook.EmailFoldersPartitioner.FOLDER_KEY;

//TODO: we suppose that messages won't be deleted
public class PartitionedOutlookReader implements ItemStreamReader<Message> {

    private static final Logger logger = LoggerFactory.getLogger(PartitionedOutlookReader.class);

    @Autowired
    private Store mailStore;

    private static final String FOLDER_INDEX_KEY = "folder_index";
    private static final String MESSAGE_INDEX_KEY = "message_index";

    private ThreadLocal<String[]> folderNames = new ThreadLocal<>();
    private ThreadLocal<Folder[]> folders = new ThreadLocal<>();
    private ThreadLocal<Message[][]> messages = new ThreadLocal<>();

    private ThreadLocal<Integer> currentFolderIndex = new ThreadLocal<>();
    private ThreadLocal<Integer> currentMessageIndex = new ThreadLocal<>();

    @Override
    public Message read() {
        if (currentFolderIndex.get() == folders.get().length) { //TODO: maybe equals()?
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

        currentMessageIndex.set(currentMessageIndex.get()+1);
        return messages.get()[currentFolderIndex.get()][currentMessageIndex.get()-1];
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

        for (int i = 0; i<folderNames.get().length; i++) {
            try {
                folders.get()[i] = mailStore.getFolder(folderNames.get()[i]);
            } catch (MessagingException e) {
                logger.error("Error occured while getting folder", e); //TODO: add folder name
            }
        }

        for (Folder folder : folders.get()) {
            try {
                if (!folder.isOpen()) {
                    folder.open(Folder.READ_ONLY);
                }
            } catch (MessagingException e) {
                logger.error("Error occured while opening folder", e); //TODO: add folder name
            }
        }

        logger.debug("Partitioned reader {} opened {} folders", this, folders.get().length);

        messages.set(new Message[folders.get().length][]);

        for (int i = 0; i < folders.get().length; i++) {
            try {
                messages.get()[i] = folders.get()[i].getMessages();
            } catch (MessagingException e) {
                logger.error("Error occured while getting messages for folder", e); //TODO: add folder name
            }
        }

        logger.debug("Partitioned reader {} got all messages from {} folders", this, folders.get().length);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(FOLDER_INDEX_KEY, currentFolderIndex.get());
        executionContext.putInt(MESSAGE_INDEX_KEY, currentMessageIndex.get());
    }

    @Override
    public void close() throws ItemStreamException {
        for (Folder folder : folders.get()) {
            try {
                folder.close();
            } catch (MessagingException e) {
                logger.error("Error occured while closing folder", e); //TODO: add folder name
            }
        }

        logger.debug("Partitioned reader {} closed {} folders", this, folders.get().length);
    }

    @AfterChunk
    private void afterChunk(ChunkContext chunkContext) {
        logger.debug("Partitioned reader {} successfully readed chunk: {}", this, chunkContext);
    }
}
