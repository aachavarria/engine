package org.craftercms.engine.util.spring.social;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.craftercms.commons.lang.Callback;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.engine.config.ConfigurationParser;
import org.craftercms.engine.config.impl.FacebookConnectionFactoryConfigParser;
import org.craftercms.engine.service.context.SiteContext;
import org.craftercms.engine.util.ConfigUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;

/**
 * {@link org.springframework.social.connect.ConnectionFactoryLocator} that resolves {@link org.springframework.social
 * .connect.ConnectionFactory}s according to the current site. Basically, each site has it's own connection registry,
 * which is used to find the corresponding {@code ConnectionFactory} for the site. The registry is created from the
 * site configuration. If there's no site configuration for connections, the default locator is used.
 *
 * @author avasquez
 */
public class ConfigAwareConnectionFactoryLocator implements ConnectionFactoryLocator {

    public static final String SOCIAL_CONNECTIONS_KEY = "socialConnections";

    public static final String CONNECTION_FACTORY_LOCATOR_CACHE_KEY = "connectionFactoryLocator";

    protected ConnectionFactoryLocator defaultLocator;
    protected CacheTemplate cacheTemplate;
    protected List<ConfigurationParser<?>> configParsers;

    public ConfigAwareConnectionFactoryLocator() {
        configParsers = new ArrayList<>(1);
        configParsers.add(new FacebookConnectionFactoryConfigParser());
    }

    @Required
    public void setDefaultLocator(ConnectionFactoryLocator defaultLocator) {
        this.defaultLocator = defaultLocator;
    }

    @Required
    public void setCacheTemplate(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public void setConfigParsers(List<ConfigurationParser<?>> configParsers) {
        this.configParsers = configParsers;
    }

    @Override
    public ConnectionFactory<?> getConnectionFactory(final String providerId) {
        return getCurrentConnectionFactoryLocator().getConnectionFactory(providerId);
    }

    @Override
    public <A> ConnectionFactory<A> getConnectionFactory(final Class<A> apiType) {
        return getCurrentConnectionFactoryLocator().getConnectionFactory(apiType);
    }

    @Override
    public Set<String> registeredProviderIds() {
        return getCurrentConnectionFactoryLocator().registeredProviderIds();
    }

    protected ConnectionFactoryLocator getCurrentConnectionFactoryLocator() {
        Callback<ConnectionFactoryLocator> callback = new Callback<ConnectionFactoryLocator>() {

            @Override
            public ConnectionFactoryLocator execute() {
                HierarchicalConfiguration config = ConfigUtils.getCurrentConfig();
                ConnectionFactoryRegistry registry = null;

                if (config != null) {
                    try {
                        SubnodeConfiguration socialConnectionsConfig = config.configurationAt(SOCIAL_CONNECTIONS_KEY);
                        for (ConfigurationParser<?> parser : configParsers) {
                            ConnectionFactory<?> factory = (ConnectionFactory<?>)parser.parse(socialConnectionsConfig);
                            if (factory != null) {
                                if (registry == null) {
                                    registry = new ConnectionFactoryRegistry();
                                }

                                registry.addConnectionFactory(factory);
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        // Ignore if != 1
                    }
                }

                if (registry != null) {
                    return registry;
                } else {
                    return defaultLocator;
                }
            }

        };

        SiteContext context = SiteContext.getCurrent();
        if (context != null) {
            return cacheTemplate.getObject(context.getContext(), callback, CONNECTION_FACTORY_LOCATOR_CACHE_KEY);
        } else {
            return defaultLocator;
        }
    }

}
