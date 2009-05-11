package com.protocol7.webtest;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class LdapRunner {

    public static void main(String[] args) throws Exception {
        Hashtable env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://172.31.0.123:389");

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "CN=LDAPUserCATOS,OU=CATOS,O=GHAB");
        env.put(Context.SECURITY_CREDENTIALS, "ldapread");

        System.out.println("Connecting to directory\n");
        DirContext ctx = new InitialDirContext(env);
        System.out.println("Connecting to directory successful\n");

        System.out.println("Doing LDAP lookup\n");
        ctx.lookup("cn=xyzclnigu,ou=user,ou=hamnen,o=ghab");
        System.out.println("LDAP lookup successful\n");
    }
}
