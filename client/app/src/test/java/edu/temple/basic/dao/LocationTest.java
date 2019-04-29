package edu.temple.basic.dao;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocationTest {
        private static final float sercLat = 39.9818f;
        private static final float sercLng = -75.1531f;
    Location location = new Location("Tech", new Page("google.com"),
            new LatLng(sercLat, sercLng), "19");
    @Test
    public void getUserId() {
        assertEquals(location.getUserId(), "19");
    }

    @Test
    public void getName() {
        assertEquals(location.getName(), "Tech");
    }

    @Test
    public void getLatLng() {
        assertEquals(location.getLatLng(), new LatLng(sercLat, sercLng));
    }

    @Test
    public void getPageURL() {
        assertEquals(location.getPageURL(), "google.com");
    }
}