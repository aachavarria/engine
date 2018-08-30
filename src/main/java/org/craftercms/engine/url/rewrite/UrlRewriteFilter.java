package org.craftercms.engine.url.rewrite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.engine.http.impl.DefaultExceptionHandler;
import org.craftercms.engine.service.SiteItemService;
import org.craftercms.engine.service.context.SiteContext;
import org.springframework.beans.factory.annotation.Required;
import org.tuckey.web.filters.urlrewrite.UrlRewriteWrappedResponse;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Crafter's implementation of Tuckey's {@code org.tuckey.web.filters.urlrewrite.UrlRewriteFilter}. It uses the same
 * {@code org.tuckey.web.filters.urlrewrite.UrlRewriter}, but skips most of the Servlet filter configuration and
 * uses per-site configuration, which can be specified in {@code /config/engine/urlrewrite.xml} (for Tuckey's classic
 * XML style configuration) or {@code /config/engine/urlrewrite.conf} (for Apache's mod_rewrite style configuration).
 *
 * @author avasquez
 *
 * @see <a href="http://tuckey.org/urlrewrite/">Tuckey URL Rewrite</a>
 */
public class UrlRewriteFilter implements Filter {

    private static final Log logger = LogFactory.getLog(DefaultExceptionHandler.class);

    protected SiteItemService siteItemService;

    @Required
    public void setSiteItemService(SiteItemService siteItemService) {
        this.siteItemService = siteItemService;
    }

    @Override
    public void init(FilterConfig filterConfig) {
        // Do nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        UrlRewriter urlRewriter = getUrlRewriter();
        boolean requestRewritten = false;

        if (urlRewriter != null) {
            httpServletResponse = new UrlRewriteWrappedResponse(httpServletResponse, httpServletRequest, urlRewriter);
            requestRewritten = urlRewriter.processRequest(httpServletRequest, httpServletResponse, chain);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("URL rewriter engine not loaded, ignoring request");
            }
        }

        // if no rewrite has taken place continue as normal
        if (!requestRewritten) {
            chain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    protected UrlRewriter getUrlRewriter() {
        SiteContext siteContext = SiteContext.getCurrent();
        if (siteContext != null) {
            return siteContext.getUrlRewriter();
        } else {
            throw new IllegalStateException("No site context found to get the URL rewriter from");
        }
    }

    @Override
    public void destroy() {
        // Do nothing
    }

}
