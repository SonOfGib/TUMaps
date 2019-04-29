package edu.temple.basic.util;

public class WikiUtils {

    public static String WIKI_URL = "http://ec2-34-203-104-209.compute-1.amazonaws.com";
    public static String HOME_PAGE_URL = nameToUrl("home");

    /**
     * Convert a wiki page name to a full wiki url.
     * @param name The name of the wiki page.
     * @return The url to that page.
     */
    public static String nameToUrl(String name){
        return WIKI_URL + "/" +name;
    }
}
