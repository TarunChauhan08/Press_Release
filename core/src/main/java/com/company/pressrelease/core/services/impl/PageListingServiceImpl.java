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

@Component(service = PageListingService.class, immediate = true)
public class PageListingServiceImpl implements PageListingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PageListingServiceImpl.class);

    @Reference
    private QueryBuilder queryBuilder;

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

        int limit = (numberOfCards != null) ? numberOfCards : 5;

        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("path", parentPath);
        queryMap.put("type", "cq:Page");
        queryMap.put("p.limit", String.valueOf(limit));

        queryMap.put("orderby", "@jcr:content/customPublishDate");
        queryMap.put("orderby.sort", "desc");

        queryMap.put("1_property", "jcr:content/cq:template");
        queryMap.put("1_property.1_value", "/conf/pressrelease/settings/wcm/templates/press-report-page");
        queryMap.put("1_property.2_value", "/conf/pressrelease/settings/wcm/templates/story-page");
        queryMap.put("1_property.3_value", "/conf/pressrelease/settings/wcm/templates/article-page");

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
            } catch (RepositoryException  e) {
                LOGGER.error("Error in getting hit: {}", e.getMessage(), e);
            }
        }

        return items;
    }


    private PageListingItem mapPageToItem(Page page, ResourceResolver resourceResolver) {
        ValueMap properties = page.getProperties();

        Calendar publishDate = properties.get("customPublishDate", Calendar.class);
        String title = properties.get("customTitle", String.class);
        String description = properties.get("customDescription", String.class);
        String image = properties.get("customImage", String.class);
        String pagePath = resourceResolver.map(page.getPath());

        if (StringUtils.isBlank(title)) {
            title = page.getTitle();
        }

        if (StringUtils.isBlank(description)) {
            description = page.getDescription();
        }

        if (StringUtils.isBlank(image)) {
            image = properties.get("image/fileReference", String.class);
        }

        if (StringUtils.isBlank(image)) {
            image = properties.get("fileReference", String.class);
        }

        return new PageListingItem(title, description, image, publishDate, pagePath);
    }
}
