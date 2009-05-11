package com.protocol7.webtest;

import java.util.Properties;

import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailRunner {

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", ConfigUtil.getString("mail.host"));
        String userName = ConfigUtil.getString("mail.username");
        
        if(userName != null) {
            System.out.println("Using user " + userName);
            props.setProperty("mail.user", userName);
            props.setProperty("mail.password", ConfigUtil.getString("mail.password"));
        }
        

        System.out.println("Creating mail session\n");
        javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(props, null);
        
        System.out.println("Creating mail transport\n");
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(ConfigUtil.getString("mail.from")));
        message.setSubject("Hello for webtest");
        message.setContent("This is a test", "text/plain");
        message.addRecipient(javax.mail.Message.RecipientType.TO,
             new InternetAddress(ConfigUtil.getString("mail.to")));

        System.out.println("Connecting to mail transport\n");
        transport.connect();
        System.out.println("Sending mail\n");
        transport.sendMessage(message,
            message.getRecipients(javax.mail.Message.RecipientType.TO));
        System.out.println("Closing transport\n");
        transport.close(); 
    }
}
