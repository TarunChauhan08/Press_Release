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

    /** Parent path value from the component properties */
    @ValueMapValue
    private String parentPath;

    /** Array of tags to filter the page list */
    @ValueMapValue
    private String[] tags;

    /** Number of cards/limit to display */
    @ValueMapValue
    private Integer numberOfCards;

    /** AEM ResourceResolver for mapping paths */
    @SlingObject
    private ResourceResolver resourceResolver;

    /** PageListingService reference injected by OSGi */
    @OSGiService
    private PageListingService pageListingService;

    /** List of retrieved page items */
    private List<PageListingItem> items = new ArrayList<>();

    /**
     * PostConstruct initialization method that fetches the page list using the service.
     */
    @PostConstruct
    protected void init() {
        if (pageListingService != null && resourceResolver != null) {
            this.items = pageListingService.getPageListingItems(resourceResolver, parentPath, tags, numberOfCards);
        }
    }

    /**
     * Gets the list of retrieved page items.
     *
     * @return the list of {@link PageListingItem}
     */
    public List<PageListingItem> getItems() {
        return items;
    }

    /**
     * Checks if the list of retrieved page items is empty.
     *
     * @return true if the list of items is empty, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
