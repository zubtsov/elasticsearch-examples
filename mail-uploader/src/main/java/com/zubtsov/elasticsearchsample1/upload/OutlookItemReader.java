package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;
import java.util.Properties;

public class OutlookItemReader implements ItemReader<XContentBuilder> {

    Properties props = new Properties();

    public OutlookItemReader() {
        props.setProperty("mail.imap.ssl.enable", "true");
    }

    @Override
    public XContentBuilder read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Session mailSession = Session.getInstance(props);
        //mailSession.setDebug(true);
        Store mailStore = mailSession.getStore("imap");
        mailStore.connect("outlook.office365.com", "Ruslan_Zubtsov@epam.com", "Ybxnj yt bcnbyyj, dct ljpdjktyj");
        Folder inbox = mailStore.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        SearchTerm st = new SearchTerm() {
            @Override
            public boolean match(Message msg) {
                try {
                    if ("Full text search training plan".equals(msg.getSubject())) {
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println(e);
                }
                return false;
            }
        };
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                .field("FromAddress", inbox.search(st)[0].getFrom()[0].toString())
                .endObject();
        inbox.close();
        return builder;
    }
}
