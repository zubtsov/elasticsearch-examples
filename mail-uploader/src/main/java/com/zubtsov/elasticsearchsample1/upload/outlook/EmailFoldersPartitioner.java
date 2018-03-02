package com.zubtsov.elasticsearchsample1.upload.outlook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.util.*;

public class EmailFoldersPartitioner implements Partitioner {

    private static final Logger logger = LoggerFactory.getLogger(EmailFoldersPartitioner.class);

    public static final String FOLDER_KEY = "folders"; //TODO: where to place?

    @Autowired
    @Value("${mail.folder.name.pattern}")
    private String folderNamePattern;

    @Autowired
    private Store mailStore;

    public EmailFoldersPartitioner(Store mailStore) {
        this.mailStore = mailStore;
    }

    //TODO: fix logic
    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        Map<String, ExecutionContext> contextMap = new HashMap<>(gridSize);

        try {
            Folder[] folders = mailStore.getDefaultFolder().list(folderNamePattern);
            String[] folderNames = Arrays.stream(folders).map(Folder::getFullName).toArray(size -> new String[size]);

            int extendedSteps = folderNames.length % gridSize;
            int foldersPerStep = folderNames.length / gridSize;

            for (int i = 0; i < extendedSteps; i++) {
                ExecutionContext context = new ExecutionContext();
                context.put(FOLDER_KEY, Arrays.copyOfRange(folderNames, i * (foldersPerStep + 1), (i + 1) * (foldersPerStep + 1)));
                contextMap.put("Folders partition #" + i, context);
            }

            for (int i = 0; i < gridSize-extendedSteps; i++) {
                ExecutionContext context = new ExecutionContext();
                context.put(FOLDER_KEY, Arrays.copyOfRange(folderNames,
                        extendedSteps * (foldersPerStep + 1) + i * foldersPerStep,
                        extendedSteps * (foldersPerStep + 1) + (i + 1) * foldersPerStep));
                contextMap.put("Folders partition #" + (i + extendedSteps), context);
            }
        } catch (MessagingException e) {
            logger.error("Error occured while getting list of folders", e);
        }

        return contextMap;
    }
}
