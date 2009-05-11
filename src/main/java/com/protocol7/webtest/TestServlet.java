package com.protocol7.webtest;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class TestServlet extends HttpServlet {

    private static final boolean TEST_JDBC =            ConfigUtil.getBoolean("jdbc");
    private static final boolean TEST_LDAP =            ConfigUtil.getBoolean("ldap");
    private static final boolean TEST_JMS =             ConfigUtil.getBoolean("jms");
    private static final boolean TEST_MAIL =            ConfigUtil.getBoolean("mail");

    private static final String JDBC_NAME =             ConfigUtil.getString("jdbc.name");
    private static final String JDBC_QUERY =            ConfigUtil.getString("jdbc.query");

    private static final String LDAP_URL =              ConfigUtil.getString("ldap.url");
    private static final String LDAP_USER_NAME =        ConfigUtil.getString("ldap.username");
    private static final String LDAP_PASSWORD =         ConfigUtil.getString("ldap.password");
    private static final String LDAP_LOOKUP_DN =        ConfigUtil.getString("ldap.lookupdn");

    private static final String JMS_CF_NAME =           ConfigUtil.getString("jms.cfname");
    private static final String JMS_DEST_NAME =         ConfigUtil.getString("jms.destname");
    
    private static final String MAIL_HOST =             ConfigUtil.getString("mail.host");
    private static final String MAIL_USER_NAME =        ConfigUtil.getString("mail.username");
    private static final String MAIL_PASSWORD =         ConfigUtil.getString("mail.password");
    private static final String MAIL_FROM =             ConfigUtil.getString("mail.from");
    private static final String MAIL_TO =               ConfigUtil.getString("mail.to");

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = resp.getWriter();
        resp.setContentType("text/plain");
        writer.write("Web test page, version 1.1\n");

        writer.write("Network Interfaces\n");
        printInterfaces(writer);
        writer.write("\n");
        
        writer.write("HTTP Request headers\n");
        Enumeration headers = req.getHeaderNames();
        while(headers.hasMoreElements()) {
            String header = (String) headers.nextElement();
            String value = "";
            Enumeration values = req.getHeaders(header);
            
            while(values.hasMoreElements()) {
                value = value + ", " + values.nextElement();
            }
            
            writer.println(header + ": " + value);
        }
        
        writer.write("\n");
        if (TEST_LDAP) {
            writer.write("Going to test LDAP connectivity\n");
            try {
                testLdap(writer);
            } catch (Exception e) {
                writer.write("LDAP test threw exception: \n");
                e.printStackTrace(writer);
            }
            writer.write("LDAP test successful\n");
        } else {
            writer.write("LDAP test disabled\n");
        }

        writer.write("\n");
        if (TEST_JDBC) {
            writer.write("Going to test database connectivity\n");
            try {
                testDatabase(writer);
                writer.write("Database test successful\n");
            } catch (Exception e) {
                writer.write("Database test threw exception: \n");
                e.printStackTrace(writer);
            }
        } else {
            writer.write("JDBC test disabled\n");
        }

        writer.write("\n");
        if (TEST_JMS) {
            writer.write("Going to test JMS connectivity\n");
            try {
                testJMS(writer);
                writer.write("JMS test successful\n");
            } catch (Exception e) {
                writer.write("JMS test threw exception: \n");
                e.printStackTrace(writer);
            }
        } else {
            writer.write("JMS test disabled\n");
        }

        writer.write("\n");
        if (TEST_MAIL) {
            writer.write("Going to test email connectivity\n");
            try {
                testEmail(writer);
                writer.write("Email test successful\n");
            } catch (Exception e) {
                writer.write("Email test threw exception: \n");
                e.printStackTrace(writer);
            }
        } else {
            writer.write("Email test disabled\n");
        }

        writer.write("\n");
        writer.write("Done!\n");

    }
    
    private void printInterfaces(PrintWriter writer) throws SocketException {
        Enumeration nics = NetworkInterface.getNetworkInterfaces();
        while(nics != null && nics.hasMoreElements()) {
            NetworkInterface nic = (NetworkInterface) nics.nextElement();
            
            writer.write(nic.getDisplayName() + "\n");
            
            Enumeration ips = nic.getInetAddresses();
            while(ips != null && ips.hasMoreElements()) {
                InetAddress ip = (InetAddress) ips.nextElement();
                writer.write(ip.getHostAddress() + "\n");
            }            
        }
    }

    private void testEmail(PrintWriter writer) throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", MAIL_HOST);

        if(MAIL_USER_NAME != null) {
            props.setProperty("mail.user", MAIL_USER_NAME);
            props.setProperty("mail.password", MAIL_PASSWORD);
        }

        writer.write("Creating mail session\n");
        javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(props, null);
        
        writer.write("Creating mail transport\n");
        Transport transport = mailSession.getTransport();

        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(MAIL_FROM));
        message.setSubject("Hello from webtest");
        message.setContent("This is a test", "text/plain");
        message.addRecipient(javax.mail.Message.RecipientType.TO,
             new InternetAddress(MAIL_TO));

        writer.write("Connecting to mail transport\n");
        transport.connect();
        writer.write("Sending mail\n");
        transport.sendMessage(message,
            message.getRecipients(javax.mail.Message.RecipientType.TO));
        writer.write("Closing transport\n");
        transport.close();        
    }

    private void testJMS(PrintWriter writer) throws Exception {
        writer.write("Creating JNDI context\n");
        Context ctx = new InitialContext();

        writer.write("Looking up connection factory in JNDI\n");
        ConnectionFactory cf = (ConnectionFactory) ctx.lookup(JMS_CF_NAME);

        writer.write("Looking up destination in JNDI\n");
        Destination dest = (Destination) ctx.lookup(JMS_DEST_NAME);

        
        if (cf != null && dest != null) {
            writer.write("Connecting to JMS\n");
            
            javax.jms.Connection conn = null;
            Session session = null;
            MessageProducer producer = null;
            try {
                conn = cf.createConnection();
                
                session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
                
                producer = session.createProducer(dest);
            
                writer.write("Sending message\n");
                Message msg = session.createTextMessage("Hello from webtest");
                producer.send(msg);
            } finally {
                if (producer != null) {
                    producer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
        } else {
            if(cf == null) {
                writer.write("JMS connection factory not found using the name \"" + JMS_CF_NAME + "\"\n");
            }
            if(dest == null) {
                writer.write("JMS destination not found using the name \"" + JMS_CF_NAME + "\"\n");
            }
        }
    }

    private void testLdap(PrintWriter writer) throws NamingException {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, LDAP_URL);

        if (LDAP_USER_NAME != null) {
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, LDAP_USER_NAME);
            env.put(Context.SECURITY_CREDENTIALS, LDAP_PASSWORD);
        }

        writer.write("Connecting to directory\n");
        DirContext ctx = new InitialDirContext(env);
        writer.write("Connecting to directory successful\n");

        writer.write("Doing LDAP lookup\n");
        ctx.lookup(LDAP_LOOKUP_DN);
        writer.write("LDAP lookup successful\n");
    }

    private void testDatabase(PrintWriter writer) throws Exception {
        writer.write("Creating JNDI context\n");
        Context ctx = new InitialContext();

        writer.write("Looking up data source in JNDI\n");
        DataSource ds = (DataSource) ctx.lookup(JDBC_NAME);

        if (ds != null) {
            writer.write("Connecting to database\n");
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            try {
                conn = ds.getConnection();
                writer.write("Auto-commit is: \n");
                writer.write(conn.getAutoCommit() + "\n");

                writer.write("Creating statement\n");
                stmt = conn.createStatement();

                writer.write("Running query against database\n");
                rs = stmt.executeQuery(JDBC_QUERY);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            }
        } else {
            writer.write("Data source not found using the name \"" + JDBC_NAME + "\"\n");
        }
    }
}
