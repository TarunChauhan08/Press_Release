package com.company.pressrelease.core.models;

import java.util.Calendar;

/**
 * Model representing a single page item in the page listing component.
 */
public class PageListingItem {

    /** Title of the page */
    private final String title;

    /** Description of the page */
    private final String description;

    /** Path to the image associated with the page */
    private final String image;

    /** Publish date of the page */
    private final Calendar publishDate;

    /** Mapped JCR path to the page */
    private final String pagePath;

    /**
     * Constructs a new PageListingItem.
     *
     * @param title       the title of the page
     * @param description the description of the page
     * @param image       the path/reference to the page image
     * @param publishDate the publish date of the page
     * @param pagePath    the resolved JCR path of the page
     */
    public PageListingItem(String title, String description, String image, Calendar publishDate, String pagePath) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.publishDate = publishDate;
        this.pagePath = pagePath;
    }

    /**
     * Gets the title of the page.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the description of the page.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the image path/reference of the page.
     *
     * @return the image path
     */
    public String getImage() {
        return image;
    }

    /**
     * Gets the publish date of the page.
     *
     * @return the publish date
     */
    public Calendar getPublishDate() {
        return publishDate;
    }

    /**
     * Gets the mapped page path.
     *
     * @return the page path
     */
    public String getPagePath() {
        return pagePath;
    }
}
