package com.zubtsov.elasticsearchsample1;

import org.junit.Test;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;
import java.util.Properties;

public class TestEmail {
    @Test
    public void testEmail() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.imap.ssl.enable", "true");
        Session mailSession = Session.getInstance(props);
        //mailSession.setDebug(true);
        Store mailStore = mailSession.getStore("imap");
        mailStore.connect("outlook.office365.com", "Ruslan_Zubtsov@epam.com", "");
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
        System.out.println((String)inbox.search(st)[0].getContent());
        //System.out.println((String)inbox.getMessages()[0].getContent());
        inbox.close();
    }
}
