package edu.temple.basic.dao.mockup;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import edu.temple.basic.dao.Location;
import edu.temple.basic.dao.Page;

/**
 * This is a mockup class for building and landmark locations placed on the map. The backend
 * database doesn't exist yet, so we will provide an ArrayList to be our pretend database for
 * testing.
 */
public class MockupLocations {

    private static Page tuttlemanMockPage = new Page("tuttleman-learning-center");
    private static final float tuttlemanLat = 39.9807f;
    private static final float tuttlemanLng = -75.1551f;
    private static Page sercMockPage = new Page("science-education-research-center");
    private static final float sercLat = 39.9818f;
    private static final float sercLng = -75.1531f;
    private static ArrayList<Location> locationsMockDB = new ArrayList<>();


    //Inits the mockup database with some seeded locations.
    public static MockupLocations init(){
        Location tuttleman, serc;
        tuttleman = new Location("Tuttleman Learning Center", tuttlemanMockPage,
                new LatLng(tuttlemanLat, tuttlemanLng), "0");
        serc = new Location("Science Education & Research Center", sercMockPage,
                new LatLng(sercLat, sercLng), "0");
        locationsMockDB.add(tuttleman);
        locationsMockDB.add(serc);
        return new MockupLocations();
    }

    /**
     * Returns the first location with name 'name'
     * @param name Name of the location.
     * @return The first location with that name, or null if none found.
     */
    public Location getLocation(String name){
        for(Location l : locationsMockDB){
            if(l.getName() != null && l.getName().contains(name))
                return l;
        }
        return null;
    }

    public List<Location> getAllLocations(){
        return locationsMockDB;
    }

}
