package com.company.pressrelease.core.services;

import java.util.List;
import org.apache.sling.api.resource.ResourceResolver;
import com.company.pressrelease.core.models.PageListingItem;

public interface PageListingService {

    List<PageListingItem> getPageListingItems(
            ResourceResolver resourceResolver,
            String parentPath,
            String[] tags,
            Integer numberOfCards
    );
}
