package com.zubtsov.elasticsearchsample1.upload;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;

import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.util.Properties;

//TODO: refactor
public class OutlookItemReader implements ItemReader<XContentBuilder> {

    private @Value("${mail.server.host}") String mailServerHost;
    private @Value("${mail.server.protocol}") String mailServerProtocol;
    private @Value("${mail.user}") String user;
    private @Value("${mail.password}") String password;

    private Store mailStore;

    public OutlookItemReader() {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session mailSession = Session.getInstance(props);
        //mailSession.setDebug(true);
        try {
            mailStore = mailSession.getStore(mailServerProtocol);
            mailStore.connect(mailServerHost, user, password);
        } catch (NoSuchProviderException e) {
            //TODO: handle exception
        } catch (MessagingException e) {
            //TODO: handle exception
        }
    }

    @Override
    public XContentBuilder read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
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
