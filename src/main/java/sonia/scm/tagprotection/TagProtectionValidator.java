package sonia.scm.tagprotection;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import sonia.scm.config.ConfigurationPermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Tag;
import sonia.scm.util.GlobUtil;

import java.util.List;

/**
 * This Validator decides whether the removal of given tags is allowed for a repository or must be rejected.
 * This is done with respect to user requesting the removal. the current user is obtained via {@link SecurityUtils#getSubject()}.
 *
 * @author Oliver Milke
 */
public class TagProtectionValidator {

    private TagProtectionConfigurationStore store;

    @Inject
    public TagProtectionValidator(TagProtectionConfigurationStore store) {

        this.store = store;
    }

    /**
     * Checks whether a rejection of the requested removal must take place.
     *
     * @param repository The repository to check.
     * @param tags       The tags requested for removal.
     * @return Returns {@code true}, if any of the tags must be protected from removal. Returns {@code false} if no intervention is required.
     */
    public boolean tagsMustBeProtected(Repository repository, List<Tag> tags) {

        //short circuit checks
        if (tags.isEmpty()) {
            return false;
        }

        if (featuredIsEffectivelyDisabled()) {
            return false;
        }

        if (currentUserHasDeletePrivilege(repository)) {
            return false;
        }

        //regular pattern checking
        return anyTagIsOnProtectionList(tags);
    }

    /**
     * Checks whether or not a pattern for protection is set. If no pattern is set, removal is generally allowed.
     *
     * @return Returns {@code true}, if there is no pattern and hence no checks required.
     */
    private boolean featuredIsEffectivelyDisabled() {

        String configuredPattern = getProtectionPattern();

        return configuredPattern == null || configuredPattern.isEmpty();
    }

    /**
     * Decides whether the user is subject to permission checking on a per-tag-basis or can administratively remove any tag.
     *
     * @param repository The repository to remove a tag from.
     * @return Returns {@code true}, if the user can generally remove tags in this repository, effectively ignoring the protection pattern.
     */
    private boolean currentUserHasDeletePrivilege(Repository repository) {

        if (ConfigurationPermissions.write(Constants.NAME).isPermitted()) {

            //Admin may always delete tags
            return true;
        }

        // Admins may always delete tags in their repositories (permission repository:<id>:modify),
        // unless the plugin configuration disables this
        boolean userIsOwner = RepositoryPermissions.modify(repository).isPermitted();
        return userIsOwner && !isReduceOwnerPrivilege();

    }

    /**
     * Decides if any of the provided tags matches configured pattern.
     *
     * @param tags The list of tags tag that is checked.
     * @return Returns {@code true} if a single tag matches cannot be removed. Returns false otherwise.
     */
    private boolean anyTagIsOnProtectionList(List<Tag> tags) {

        for (Tag currentTag : tags) {
            if (tagIsOnProtectionList(currentTag)) {
                return true;
            }
        }

        return false;
    }


    /**
     * Decides whether or not a specific tag matches the protection pattern. Pattern is checked as glob (basically allowing * and ? as wildcards).
     *
     * @return Returns {@code true}, if this tag matches the protection pattern.
     */
    @VisibleForTesting
    boolean tagIsOnProtectionList(Tag tag) {

        String pattern = getProtectionPattern();
        return GlobUtil.matches(pattern, tag.getName());
    }

    /**
     * Checks the plugin configuration, that prevents OWNER == ADMIN in the context of tag removal.
     *
     * <p />
     * This is required in order to maintain a special case in which definitely only the
     * Admins are allowed to remove tags that are protected by the protection pattern.
     *
     * @return Returns {@code true} if the Owner of a repository is explicitly forced to respect the protection pattern (Owner != Admin).
     * <p />
     * Returns {@code false} if the Owner is allowed to remove any tag, effectively ignoring the protection pattern (Owner == Admin).
     */
    private boolean isReduceOwnerPrivilege() {

        return store.getConfiguration().isReduceOwnerPrivilege();
    }

    private String getProtectionPattern() {

        return store.getConfiguration().getProtectionPattern();
    }

}
