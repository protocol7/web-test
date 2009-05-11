package com.protocol7.webtest;

import junit.framework.TestCase;

public class ConfigUtilTest extends TestCase {

    public void test() {
        System.out.println(ConfigUtil.getString("jdbc"));
    }
    
}
