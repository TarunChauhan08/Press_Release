package com.company.pressrelease.core.services;

import java.util.List;
import org.apache.sling.api.resource.ResourceResolver;
import com.company.pressrelease.core.models.PageListingItem;

/**
 * Service interface for retrieving lists of pages based on parent path, tags, and limit.
 */
public interface PageListingService {

    /**
     * Retrieves a list of mapped PageListingItem objects based on query criteria.
     *
     * @param resourceResolver the ResourceResolver used to resolve resources and map paths
     * @param parentPath       the path of the parent page under which to search
     * @param tags             an array of tag IDs to filter by (OR relation); can be empty or null
     * @param numberOfCards    the maximum number of items to retrieve
     * @return a list of matching PageListingItem objects, sorted by publish date descending
     */
    List<PageListingItem> getPageListingItems(
            ResourceResolver resourceResolver,
            String parentPath,
            String[] tags,
            Integer numberOfCards
    );
}
