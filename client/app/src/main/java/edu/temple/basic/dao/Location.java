package edu.temple.basic.dao;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private String name;
    private Page wikiPage;
    private LatLng latLng;
    private String userId;


    public Location(String name, Page wikiPage, LatLng latLng, String userId){
        this.name = name;
        this.wikiPage = wikiPage;
        this.latLng = latLng;
        this.userId = userId;
    }

    /**
     * Constructor that uses the page's name as the name for the location.
     * @param wikiPage
     * @param latLng
     * @param userId
     */
    public Location(Page wikiPage, LatLng latLng, String userId){
        this(wikiPage.getWikiPageName(), wikiPage, latLng, userId);
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    /**
     * Returns the page URL of this location.
     * @return The page URL.
     */
    public String getPageURL(){
        return wikiPage.getWikiPageURL();
    }
}
