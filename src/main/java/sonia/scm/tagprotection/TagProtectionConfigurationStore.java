package sonia.scm.tagprotection;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.store.Store;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.User;

import java.util.Objects;

/**
 * @author Oliver Milke
 */
@Singleton
public class TagProtectionConfigurationStore {

    @VisibleForTesting
    static final Logger logger = LoggerFactory.getLogger(TagProtectionValidator.class);

    @VisibleForTesting
    static final String STORE_TYPE = "tagprotection";

    private final Store<TagProtectionConfig> store;

    @Inject
    public TagProtectionConfigurationStore(StoreFactory factory) {

        store = factory.getStore(TagProtectionConfig.class, STORE_TYPE);
    }

    /**
     * @return Provides the persisted configuration for this plugin or the default configuration if none has been persisted so far.
     */
    public TagProtectionConfig getConfiguration() {

        TagProtectionConfig storedConfig = store.get();

        if (storedConfig == null) {

            return new TagProtectionConfig();
        } else {

            return storedConfig;
        }
    }

    /**
     * Saves the provided configuration and makes sure a change in the pattern is audited.
     */
    public void saveConfiguration(TagProtectionConfig config) {

        auditChangedPattern(config.getProtectionPattern());

        store.set(config);
    }

    /**
     * Audits a changed pattern, i. e. logs which user changes the pattern, including the old and the new pattern.
     *
     * @param newPattern The new pattern.
     */
    private void auditChangedPattern(String newPattern) {

        User user = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);

        String currentPattern = getConfiguration().getProtectionPattern();
        if (patternHasChanged(currentPattern, newPattern)) {

            logger.info("User {} has changed the pattern for protecting tags from <{}> to <{}>", user.getName(), currentPattern, newPattern);
        }
    }

    private boolean patternHasChanged(String currentPattern, String newPattern) {

        return !Objects.equals(currentPattern, newPattern);
    }

}
