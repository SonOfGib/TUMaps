package edu.temple.basic.dao;

import org.junit.Test;

import static org.junit.Assert.*;

public class PageTest {

    Page page = new Page("Will's House");

    @Test
    public void getWikiPageName() {
        assertEquals("Will's House", page.getWikiPageName());
    }
}