package edu.temple.basic.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class WikiUtilsTest {

    private static final String WIKI_URL = "http://ec2-34-203-104-209.compute-1.amazonaws.com";

    @Test
    public void nameToUrl() {
        assertEquals("http://ec2-34-203-104-209.compute-1.amazonaws.com/willshouse", WikiUtils.nameToUrl("willshouse"));
    }
}