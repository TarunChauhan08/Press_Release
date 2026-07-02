package com.company.pressrelease.core.models;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import com.company.pressrelease.core.services.PageListingService;

/**
 * Concrete Sling Model class for the Page Listing component.
 * Performs field injection and delegates processing to the PageListingService.
 */
@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    resourceType = PageListing.RESOURCE_TYPE,
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class PageListing {

    protected static final String RESOURCE_TYPE = "pressrelease/components/pagelisting";

    @ValueMapValue
    private String parentPath;

    @ValueMapValue
    private String[] tags;

    @ValueMapValue
    private Integer numberOfCards;

    @SlingObject
    private ResourceResolver resourceResolver;

    @OSGiService
    private PageListingService pageListingService;

    private List<PageListingItem> items = new ArrayList<>();

    @PostConstruct
    protected void init() {
        if (pageListingService != null && resourceResolver != null) {
            this.items = pageListingService.getPageListingItems(resourceResolver, parentPath, tags, numberOfCards);
        }
    }

    public List<PageListingItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
