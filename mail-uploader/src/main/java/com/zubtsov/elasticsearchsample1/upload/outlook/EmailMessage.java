package com.zubtsov.elasticsearchsample1.upload.outlook;

import java.util.Date;

public class EmailMessage {
    private String folder;
    private String[] from;
    private String subject;
    private Date sentDate;
    private Date receivedDate;
    private String[] recipients;
    private String[] replyTo;
    private String content;

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String[] getFrom() {
        return from;
    }

    public void setFrom(String[] from) {
        this.from = from;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }

    public String[] getRecipients() {
        return recipients;
    }

    public void setRecipients(String[] recipients) {
        this.recipients = recipients;
    }

    public String[] getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String[] replyTo) {
        this.replyTo = replyTo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
