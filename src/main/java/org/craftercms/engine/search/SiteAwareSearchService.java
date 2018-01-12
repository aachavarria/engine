/*
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.engine.search;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.core.service.Content;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.search.exception.SearchException;
import org.craftercms.search.service.Query;
import org.craftercms.search.service.QueryFactory;
import org.craftercms.search.service.SearchService;
import org.springframework.beans.factory.annotation.Required;

/**
 * {@link SearchService} wrapper that forces all operations to use an index ID based on the current site. If a method
 * is called with no index ID, one is created by appending the current site name + a separator + a default index
 * suffix. If an index ID is provided, then the actual, final index ID is formed by appending the current site name +
 * a separator + the provided index ID as a suffix.
 *
 * @author avasquez
 */
public class SiteAwareSearchService implements SearchService, QueryFactory<Query> {

    public static final String DEFAULT_DEFAULT_INDEX_ID_SUFFIX = "default";
    public static final String DEFAULT_INDEX_ID_SEPARATOR = "-";

    protected String defaultIndexIdSuffix;
    protected String indexIdSeparator;
    protected QueryFactory<Query> actualQueryFactory;
    protected SearchService actualSearchService;

    public SiteAwareSearchService() {
        defaultIndexIdSuffix = DEFAULT_DEFAULT_INDEX_ID_SUFFIX;
        indexIdSeparator = DEFAULT_INDEX_ID_SEPARATOR;
    }

    public void setDefaultIndexIdSuffix(String defaultIndexIdSuffix) {
        this.defaultIndexIdSuffix = defaultIndexIdSuffix;
    }

    public void setIndexIdSeparator(String indexIdSeparator) {
        this.indexIdSeparator = indexIdSeparator;
    }

    public void setActualQueryFactory(QueryFactory<Query> actualQueryFactory) {
        this.actualQueryFactory = actualQueryFactory;
    }

    @Required
    public void setActualSearchService(SearchService actualSearchService) {
        this.actualSearchService = actualSearchService;
    }

    @Override
    public Query createQuery() {
        return actualQueryFactory.createQuery();
    }

    @Override
    public Query createQuery(Map<String, String[]> map) {
        return actualQueryFactory.createQuery(map);
    }

    @PostConstruct
    public void init() {
        if (actualQueryFactory == null && actualSearchService instanceof QueryFactory) {
            actualQueryFactory = (QueryFactory)actualSearchService;
        } else if (actualQueryFactory == null) {
            throw new IllegalStateException("No actualQueryFactory provided");
        }
    }

    @Override
    public Map<String, Object> search(Query query) throws SearchException {
        return actualSearchService.search(getActualIndexId(null), query);
    }

    @Override
    public Map<String, Object> search(String indexId, Query query) throws SearchException {
        return actualSearchService.search(getActualIndexId(indexId), query);
    }

    @Override
    public String update(String site, String id, String xml, boolean ignoreRootInFieldNames) throws SearchException {
        return actualSearchService.update(getActualIndexId(null), site, id, xml, ignoreRootInFieldNames);
    }

    @Override
    public String update(String indexId, String site, String id, String xml,
                         boolean ignoreRootInFieldNames) throws SearchException {
        return actualSearchService.update(getActualIndexId(indexId), site, id, xml, ignoreRootInFieldNames);
    }

    @Override
    public String delete(String site, String id) throws SearchException {
        return actualSearchService.delete(getActualIndexId(null), site, id);
    }

    @Override
    public String delete(String indexId, String site, String id) throws SearchException {
        return actualSearchService.delete(getActualIndexId(indexId), site, id);
    }

    @Override
    public String updateDocument(String site, String id, File document) throws SearchException {
        throw new UnsupportedOperationException("Use updateFile methods");
    }

    @Override
    public String updateDocument(String site, String id, File document,
                                 Map<String, String> additionalFields) throws SearchException {
        throw new UnsupportedOperationException("Use updateFile methods");
    }

    @Override
    public String updateFile(String site, String id, File file) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(null), site, id, file);
    }

    @Override
    public String updateFile(String indexId, String site, String id, File file) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(indexId), site, id, file);
    }

    @Override
    public String updateFile(String site, String id, File file,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(null), site, id, file, additionalFields);
    }

    @Override
    public String updateFile(String indexId, String site, String id, File file,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(indexId), site, id, file, additionalFields);
    }

    @Override
    public String updateFile(String site, String id, Content content) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(null), site, id, content);
    }

    @Override
    public String updateFile(String indexId, String site, String id, Content content) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(indexId), site, id, content);
    }

    @Override
    public String updateFile(String site, String id, Content content,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(null), site, id, content, additionalFields);
    }

    @Override
    public String updateFile(String indexId, String site, String id, Content content,
                             Map<String, List<String>> additionalFields) throws SearchException {
        return actualSearchService.updateFile(getActualIndexId(null), site, id, content);
    }

    @Override
    public String commit() throws SearchException {
        return actualSearchService.commit(getActualIndexId(null));
    }

    @Override
    public String commit(String indexId) throws SearchException {
        return actualSearchService.commit(getActualIndexId(indexId));
    }

    protected String getActualIndexId(String indexIdSuffix) {
        String actualIndexId;

        if (StringUtils.isNotEmpty(indexIdSuffix)) {
            actualIndexId = getCurrentSiteName() + indexIdSeparator + indexIdSuffix;
        } else {
            actualIndexId = getCurrentSiteName() + indexIdSeparator + defaultIndexIdSuffix;
        }

        return actualIndexId;
    }

    protected String getCurrentSiteName() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getSiteName();
        } else {
            throw new IllegalStateException("Current site context not found");
        }
    }

}
