package com.company.pressrelease.core.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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


        Map<String, String> queryMap = new HashMap<>();
        queryMap.put("path", parentPath);
        queryMap.put("type", "cq:Page");
        queryMap.put("p.limit", "-1");


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

        List<PageListingItem> rawItems = new ArrayList<>();
        for (Hit hit : result.getHits()) {
            try {
                Resource pageResource = hit.getResource();
                if (pageResource != null) {
                    Page page = pageResource.adaptTo(Page.class);
                    if (page != null) {
                        PageListingItem item = mapPageToItem(page, resourceResolver);
                        if (item != null) {
                            rawItems.add(item);
                        }
                    }
                }
            } catch (RepositoryException  e) {
                LOGGER.error("Error in getting hit: {}", e.getMessage(), e);
            }
        }


        rawItems.sort(
                Comparator.comparing(
                        PageListingItem::getPublishDate,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        int limit = (numberOfCards != null) ? numberOfCards : 5;
        return rawItems.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }


    private PageListingItem mapPageToItem(Page page, ResourceResolver resourceResolver) {
        TemplateType templateType = TemplateType.fromPage(page);

        String title = null;
        String description = null;
        String image = null;
        Calendar publishDate = null;
        String pagePath = resourceResolver.map(page.getPath());

        if (templateType != null) {
            ValueMap properties = page.getProperties();
            publishDate = properties.get(templateType.getPublishDateProp(), Calendar.class);
            title = properties.get(templateType.getTitleProp(), String.class);
            description = properties.get(templateType.getDescProp(), String.class);
            image = properties.get(templateType.getImageProp(), String.class);
        }
            if (StringUtils.isBlank(title)) {
                title = page.getTitle();
            }

            if (StringUtils.isBlank(description)) {
                description = page.getDescription();
            }

        if (StringUtils.isBlank(image)) {
            image = page.getProperties().get("image/fileReference", String.class);
        }


        return new PageListingItem(title, description, image, publishDate, pagePath);
    }



    private enum TemplateType {
        PRESS_REPORT(
                "pressReportPublishedDate",
                "pressReportTitle",
                "pressReportDescription",
                "pressReportImage",
                "/conf/pressrelease/settings/wcm/templates/press-report-page"
        ),
        STORY(
                "storyPublishedDate",
                "storyTitle",
                "storyDescription",
                "storyImage",
                "/conf/pressrelease/settings/wcm/templates/story-page"
        ),
        ARTICLE(
                "articlePublishedDate",
                "articleTitle",
                "articleDescription",
                "articleImage",
                "/conf/pressrelease/settings/wcm/templates/article-page"
        );

        private  String publishDateProp;
        private  String titleProp;
        private  String descProp;
        private  String imageProp;
        private  String templatePath;

        TemplateType(String publishDateProp, String titleProp, String descProp, String imageProp, String templatePath) {
            this.publishDateProp = publishDateProp;
            this.titleProp = titleProp;
            this.descProp = descProp;
            this.imageProp = imageProp;
            this.templatePath = templatePath;
        }

        public String getPublishDateProp() {
            return publishDateProp;
        }

        public String getTitleProp() {
            return titleProp;
        }

        public String getDescProp() {
            return descProp;
        }

        public String getImageProp() {
            return imageProp;
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public static TemplateType fromPage(Page page) {
            if (page == null) {
                return null;
            }
            String template = page.getProperties().get("cq:template", String.class);
            if (template != null) {
                for (TemplateType type : values()) {
                    if (template.equals(type.getTemplatePath()) || template.endsWith("/" + type.name().toLowerCase().replace("_", "-"))) {
                        return type;
                    }
                }
            }

            ValueMap props = page.getProperties();
            if ( props.containsKey("pressReportPublishedDate")) {
                return PRESS_REPORT;
            }
            if ( props.containsKey("storyPublishedDate")) {
                return STORY;
            }
            if ( props.containsKey("articlePublishedDate")) {
                return ARTICLE;
            }

            return null;
        }
    }
}
