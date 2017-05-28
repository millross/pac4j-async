package org.pac4j.core.profile;

import org.pac4j.core.config.Config;
import org.pac4j.core.context.WebContextBase;
import org.pac4j.core.util.CommonHelper;

import java.util.function.Function;

/**
 * For classes that can set the profile manager factory - async version.
 *
 * @author Jerome Leleu
 * @since 2.0.0
 */
public abstract class ProfileManagerFactoryAware<C extends WebContextBase<?>, PM> {

        protected abstract Function<C, PM> defaultProfileManagerFactory();

        private Function<C, PM> profileManagerFactory;
        /**
         * Given a webcontext generate a profileManager for it.
         * Can be overridden for custom profile manager implementations
         * @param context the web context
         * @param config the configuration
         * @return profile manager implementation built from the context
         */
        protected PM getProfileManager(final C context, final Config<?, C, ?, ?, ?,  ?, ?, ?, PM> config) {
            final Function<C, PM> configProfileManagerFactory =  config.getProfileManagerFactory();
            if (configProfileManagerFactory != null) {
                return configProfileManagerFactory.apply(context);
            } else if (profileManagerFactory != null) {
                return profileManagerFactory.apply(context);
            } else {
                return defaultProfileManagerFactory().apply(context);
            }
        }

        public Function<C, PM> getProfileManagerFactory() {
            return profileManagerFactory;
        }

        public void setProfileManagerFactory(final Function<C, PM> factory) {
            CommonHelper.assertNotNull("factory", factory);
            this.profileManagerFactory = factory;
        }
}
