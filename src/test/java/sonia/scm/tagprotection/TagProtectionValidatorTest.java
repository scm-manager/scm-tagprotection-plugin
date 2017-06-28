package sonia.scm.tagprotection;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.security.RepositoryPermission;
import sonia.scm.security.Role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oliver Milke
 */
public class TagProtectionValidatorTest {

    private TagProtectionConfig preparedConfiguration;
    private TagProtectionValidator cut;

    @Before
    public void prepareTest() {

        //the configuration is mutable, which allows to set the configuration per test on the fly.
        preparedConfiguration = new TagProtectionConfig();

        TagProtectionConfigurationStore mockedConfigStore = mock(TagProtectionConfigurationStore.class);
        when(mockedConfigStore.getConfiguration()).thenReturn(preparedConfiguration);

        cut = new TagProtectionValidator(mockedConfigStore);
    }

    @Test
    public void assertThatAdminCanRemoveProtectedTags() {

        setAdminRole();

        preparedConfiguration.setProtectionPattern("t1");
        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is admin, hence no protection needed
        assertThat(result).isFalse();
    }

    @Test
    public void assertThatOwnerCanRemoveTags() {

        setOwnerRole();

        preparedConfiguration.setProtectionPattern("t1");
        preparedConfiguration.setReduceOwnerPrivilege(false);

        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is Owner and privilege reduction is not configured, hence no protection needed
        assertThat(result).isFalse();
    }

    @Test
    public void assertThatReducedOwnerCannotRemoveTags() {

        setOwnerRole();

        preparedConfiguration.setProtectionPattern("t1");
        preparedConfiguration.setReduceOwnerPrivilege(true);

        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is Owner, but reduction is configured, hence  protection is needed
        assertThat(result).isTrue();
    }

    @Test
    public void assertThatEmptyTagListRequiresNoProtection() throws Exception {

        setRegularUserRole();

        boolean result = cut.tagsMustBeProtected(mock(Repository.class), Collections.EMPTY_LIST);

        //no tags to handle, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    public void assertThatEmptyPatternRequiresNoProtection() {

        setRegularUserRole();

        preparedConfiguration.setProtectionPattern("");
        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //no pattern configured, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    public void assertThatNonMatchingPatternRequiresNoProtection() {

        setRegularUserRole();

        preparedConfiguration.setProtectionPattern("any");
        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //configured pattern does not match any of the tags, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    public void assertThatMatchingPatternRequiresProtection() {

        setRegularUserRole();

        preparedConfiguration.setProtectionPattern("t1");
        boolean result = cut.tagsMustBeProtected(mock(Repository.class), tagsOf("t1", "t2"));

        //no pattern configured, no protection needed
        assertThat(result).isTrue();
    }

    @Test
    public void assertThatWildCardIsProperlyMatching() throws Exception {

        preparedConfiguration.setProtectionPattern("*");

        boolean result = cut.tagIsOnProtectionList(new Tag("this-is-just-some-dummy", "1.0"));
        assertThat(result).isTrue();

        preparedConfiguration.setProtectionPattern("release/*");

        result = cut.tagIsOnProtectionList(new Tag("release/any-release", "1.0"));
        assertThat(result).isTrue();

        result = cut.tagIsOnProtectionList(new Tag("release", "1.0"));
        assertThat(result).isFalse();

        preparedConfiguration.setProtectionPattern("release/?.?");

        result = cut.tagIsOnProtectionList(new Tag("release/1.0", "1.0"));
        assertThat(result).isTrue();

        result = cut.tagIsOnProtectionList(new Tag("release/1A", "1.0"));
        assertThat(result).isFalse();

        //these are just some basic test, assuming GlobUtil is extensively tested
    }

    private void setOwnerRole() {

        Subject ownerSubject = mock(Subject.class);
        when(ownerSubject.hasRole(Role.ADMIN)).thenReturn(false);
        when(ownerSubject.isAuthenticated()).thenReturn(true);
        when(ownerSubject.getPrincipal()).thenReturn("principal");
        when(ownerSubject.isPermitted(new RepositoryPermission((String) null, PermissionType.OWNER))).thenReturn(true);

        ThreadContext.bind(ownerSubject);
    }

    private void setRegularUserRole() {

        Subject ownerSubject = mock(Subject.class);
        when(ownerSubject.hasRole(Role.ADMIN)).thenReturn(false);
        when(ownerSubject.isAuthenticated()).thenReturn(true);
        when(ownerSubject.getPrincipal()).thenReturn("principal");
        when(ownerSubject.isPermitted(new RepositoryPermission((String) null, PermissionType.OWNER))).thenReturn(false);

        ThreadContext.bind(ownerSubject);
    }

    private void setAdminRole() {

        Subject adminSubject = mock(Subject.class);
        when(adminSubject.hasRole(Role.ADMIN)).thenReturn(true);

        ThreadContext.bind(adminSubject);
    }


    private List<Tag> tagsOf(String... names) {

        List<Tag> result = new ArrayList<>();

        for (String name : names) {

            result.add(new Tag(name, "1.0"));
        }

        return result;
    }


}