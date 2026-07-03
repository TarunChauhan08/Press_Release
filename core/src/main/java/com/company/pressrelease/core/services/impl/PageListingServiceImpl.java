package com.company.pressrelease.core.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.api.resource.ValueMap;
import javax.jcr.Session;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.company.pressrelease.core.models.PageListingItem;
import com.company.pressrelease.core.services.PageListingService;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;
import com.day.cq.wcm.api.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the {@link PageListingService} interface.
 * Retrieves AEM pages under a given path, matching optional tags,
 * and mapping them to {@link PageListingItem} objects.
 */
@Component(service = PageListingService.class, immediate = true)
public class PageListingServiceImpl implements PageListingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageListingServiceImpl.class);

    /** Property name for custom publish date */
    public static final String PROPERTY_CUSTOM_PUBLISH_DATE = "customPublishDate";

    /** Property name for custom title */
    public static final String PROPERTY_CUSTOM_TITLE = "customTitle";

    /** Property name for custom description */
    public static final String PROPERTY_CUSTOM_DESCRIPTION = "customDescription";

    /** Property name for custom image */
    public static final String PROPERTY_CUSTOM_IMAGE = "customImage";

    /** Property path for image file reference inside the image node */
    public static final String PROPERTY_IMAGE_FILE_REFERENCE = "image/fileReference";

    /** Property name for file reference */
    public static final String PROPERTY_FILE_REFERENCE = "fileReference";

    /** Template path for press report pages */
    public static final String TEMPLATE_PRESS_REPORT = "/conf/pressrelease/settings/wcm/templates/press-report-page";

    /** Template path for story pages */
    public static final String TEMPLATE_STORY = "/conf/pressrelease/settings/wcm/templates/story-page";

    /** Template path for article pages */
    public static final String TEMPLATE_ARTICLE = "/conf/pressrelease/settings/wcm/templates/article-page";

    /** Default number of cards/results to limit to */
    private static final int DEFAULT_LIMIT = 5;

    @Reference
    private QueryBuilder queryBuilder;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PageListingItem> getPageListingItems(
            ResourceResolver resourceResolver,
            String parentPath,
            String[] tags,
            Integer numberOfCards) {

        List<PageListingItem> items = new ArrayList<>();

        if (resourceResolver == null || queryBuilder == null || StringUtils.isBlank(parentPath)) {
            return items;
        }

        int limit = (numberOfCards != null) ? numberOfCards : DEFAULT_LIMIT;

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("path", parentPath);
        queryMap.put("type", "cq:Page");
        queryMap.put("p.limit", String.valueOf(limit));

        queryMap.put("orderby", "@jcr:content/" + PROPERTY_CUSTOM_PUBLISH_DATE);
        queryMap.put("orderby.sort", "desc");

        queryMap.put("1_property", "jcr:content/cq:template");
        queryMap.put("1_property.1_value", TEMPLATE_PRESS_REPORT);
        queryMap.put("1_property.2_value", TEMPLATE_STORY);
        queryMap.put("1_property.3_value", TEMPLATE_ARTICLE);

        if (tags != null && tags.length > 0) {
            List<String> validTags = new ArrayList<>();
            for (String tag : tags) {
                if (StringUtils.isNotBlank(tag)) {
                    validTags.add(tag);
                }
            }
            if (!validTags.isEmpty()) {
                queryMap.put("2_property", "jcr:content/cq:tags");
                for (int i = 0; i < validTags.size(); i++) {
                    queryMap.put("2_property." + (i + 1) + "_value", validTags.get(i));
                }
                queryMap.put("2_property.and", "false");
            }
        }

        Session session = resourceResolver.adaptTo(Session.class);
        if (session == null) {
            return items;
        }

        Query query = queryBuilder.createQuery(PredicateGroup.create(queryMap), session);
        SearchResult result = query.getResult();

        for (Hit hit : result.getHits()) {
            try {
                Resource pageResource = hit.getResource();
                if (pageResource != null) {
                    Page page = pageResource.adaptTo(Page.class);
                    if (page != null) {
                        PageListingItem item = mapPageToItem(page, resourceResolver);
                        if (item != null) {
                            items.add(item);
                        }
                    }
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error in getting hit: {}", e.getMessage(), e);
            }
        }

        return items;
    }

    /**
     * Maps an AEM {@link Page} to a {@link PageListingItem} using custom properties.
     * Fallbacks to standard page properties if custom ones are blank or missing.
     *
     * @param page             the AEM Page to map
     * @param resourceResolver the ResourceResolver for path mapping
     * @return the mapped PageListingItem
     */
    private PageListingItem mapPageToItem(Page page, ResourceResolver resourceResolver) {
        ValueMap properties = page.getProperties();

        Calendar publishDate = properties.get(PROPERTY_CUSTOM_PUBLISH_DATE, Calendar.class);
        String title = properties.get(PROPERTY_CUSTOM_TITLE, String.class);
        String description = properties.get(PROPERTY_CUSTOM_DESCRIPTION, String.class);
        String image = properties.get(PROPERTY_CUSTOM_IMAGE, String.class);
        String pagePath = resourceResolver.map(page.getPath());

        if (StringUtils.isBlank(title)) {
            title = page.getTitle();
        }

        if (StringUtils.isBlank(description)) {
            description = page.getDescription();
        }

        if (StringUtils.isBlank(image)) {
            image = properties.get(PROPERTY_IMAGE_FILE_REFERENCE, String.class);
        }

        if (StringUtils.isBlank(image)) {
            image = properties.get(PROPERTY_FILE_REFERENCE, String.class);
        }

        return new PageListingItem(title, description, image, publishDate, pagePath);
    }
}
