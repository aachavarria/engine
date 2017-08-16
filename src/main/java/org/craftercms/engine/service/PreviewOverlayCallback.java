package org.craftercms.engine.service;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.engine.macro.MacroResolver;
import org.craftercms.engine.mobile.UserAgentTemplateDetector;
import org.springframework.beans.factory.annotation.Required;

public class PreviewOverlayCallback {

    private String scriptFormat;
	private String[] previewServerJsScriptSources;
    private MacroResolver macroResolver;
    private UserAgentTemplateDetector userAgentTemplateDetector;

    @Required
    public void setScriptFormat(String scriptFormat) {
        this.scriptFormat = scriptFormat;
    }

    @Required
    public void setPreviewServerJsScriptSources(String[] previewServerJsScriptSources) {
        this.previewServerJsScriptSources = previewServerJsScriptSources;
    }

    @Required
    public void setMacroResolver(MacroResolver macroResolver) {
        this.macroResolver = macroResolver;
    }

    @Required
    public void setUserAgentTemplateDetector(UserAgentTemplateDetector userAgentTemplateDetector) {
        this.userAgentTemplateDetector = userAgentTemplateDetector;
    }

    public String render() {
        String queryString = RequestContext.getCurrent().getRequest().getQueryString();
		StringBuilder scriptsStr = new StringBuilder();

        // TODO: Shouldn't we also check if CStudio-Agent header is also present?
		if(StringUtils.isEmpty(queryString) ||
           !queryString.contains(userAgentTemplateDetector.getAgentQueryStringParamName())) {
			for (String scriptSrc : previewServerJsScriptSources) {
                String script = scriptFormat.replace("{scriptSrc}", scriptSrc);

                scriptsStr.append(macroResolver.resolveMacros(script));
                scriptsStr.append(System.getProperty("line.separator"));
			}
		}
		
		return scriptsStr.toString();
	}

}
