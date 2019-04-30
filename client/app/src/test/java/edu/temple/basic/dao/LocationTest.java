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
        assertEquals("19", location.getUserId());
    }

    @Test
    public void getName() {
        assertEquals("Tech", location.getName());
    }

    @Test
    public void getLatLng() {
        assertEquals(new LatLng(sercLat, sercLng), location.getLatLng());
    }

    @Test
    public void getPageURL() {
        assertEquals("google.com", location.getPageURL());
    }
}