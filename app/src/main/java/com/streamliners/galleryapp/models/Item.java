package com.streamliners.galleryapp.models;

public class Item {

    public String imageUrl;
    public int color;
    public String label;

    /**
     * Parameterised constructor for item class
     * @param imageUrl
     * @param color
     * @param label
     */
    public Item(String imageUrl, int color, String label) {
        this.imageUrl = imageUrl;
        this.color = color;
        this.label = label;
    }
}
