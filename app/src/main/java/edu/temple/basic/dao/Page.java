package edu.temple.basic.dao;

import edu.temple.basic.util.WikiUtils;

public class Page {

    private String wikiPageName;


    /**
     * Returns the name of the Wiki Page. Ex. For the home page the name would be home.
     * getWikiPageURL will get the full url.
     * @return Wiki page name.
     */
    public String getWikiPageName() {
        return wikiPageName;
    }

    /**
     * Returns the a URL to the wiki page.
     * @return The wiki page url.
     */
    public String getWikiPageURL(){
        return WikiUtils.nameToUrl(getWikiPageName());
    }
}
