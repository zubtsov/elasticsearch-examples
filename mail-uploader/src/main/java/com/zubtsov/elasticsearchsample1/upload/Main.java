package com.zubtsov.elasticsearchsample1.upload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//TODO: automate mapping/schema creation
//TODO: dockerize this application
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String [] args) {
        log.debug("Starting uploader application...");
        ApplicationContext context = new AnnotationConfigApplicationContext(UploaderConfiguration.class);
    }
}