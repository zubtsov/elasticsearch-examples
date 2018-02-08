package com.zubtsov.elasticsearchsample1.upload;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String [] args) throws Exception {
        ApplicationContext context = new AnnotationConfigApplicationContext(UploaderConfiguration.class);
    }
}