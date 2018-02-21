package com.zubtsov.elasticsearchsample1.upload;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

//TODO: add logging & solve problem with slf4j implementation
//TODO: fix parallel job running
//TODO: automate mapping/schema creation
//TODO: dockerize this application
public class Main {
    public static void main(String [] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(UploaderConfiguration.class);
    }
}