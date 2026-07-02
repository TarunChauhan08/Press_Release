package com.company.pressrelease.core.models;

import java.util.Calendar;

public class PageListingItem {

    private final String title;
    private final String description;
    private final String image;
    private final Calendar publishDate;
    private final String pagePath;

    public PageListingItem(String title, String description, String image, Calendar publishDate, String pagePath) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.publishDate = publishDate;
        this.pagePath = pagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public Calendar getPublishDate() {
        return publishDate;
    }

    public String getPagePath() {
        return pagePath;
    }
}
