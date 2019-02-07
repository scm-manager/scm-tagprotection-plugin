package sonia.scm.tagprotection;

import com.thekua.spikes.LogbackCapturingAppender;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.security.Role;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oliver Milke
 */
public class TagProtectionConfigurationStoreTest {

    private static final String ADMIN_USER_NAME = "AdminUser";

    private TagProtectionConfigurationStore cut;
    private ConfigurationStore<TagProtectionConfig> mockedStore;

    @Before
    public void prepareTest() {

        mockedStore = mock(ConfigurationStore.class);
        ConfigurationStoreFactory factory = new InMemoryConfigurationStoreFactory(mockedStore);

        cut = new TagProtectionConfigurationStore(factory);
    }

    @Test
    public void assertThatDefaultConfigurationIsProvided() {

        when(mockedStore.get()).thenReturn(null);
        TagProtectionConfig configuration = cut.getConfiguration();

        //prove that without a persisted configuration, the values match with the default values as defined by TagProtectionConfig

        assertThat(configuration.getProtectionPattern()).isEqualTo(new TagProtectionConfig().getProtectionPattern());
        assertThat(configuration.isReduceOwnerPrivilege()).isEqualTo(new TagProtectionConfig().isReduceOwnerPrivilege());
    }

    @Test
    public void assertThatPersistedConfigurationIsProvided() {

        TagProtectionConfig persistedConfiguration = new TagProtectionConfig();
        when(mockedStore.get()).thenReturn(persistedConfiguration);

        TagProtectionConfig configuration = cut.getConfiguration();

        //prove that a persisted configuration is returned
        assertThat(configuration).isSameAs(persistedConfiguration);
    }

    @Test
    public void assertThatChangedPatternIsAudited() {

        setAdminRole();
        when(mockedStore.get()).thenReturn(null);
        LogbackCapturingAppender capturing = LogbackCapturingAppender.weaveInto(TagProtectionConfigurationStore.logger);

        String newProtectionPattern = "protect/*";

        TagProtectionConfig config = new TagProtectionConfig();

        //invert non-audited flag
        config.setReduceOwnerPrivilege(!config.isReduceOwnerPrivilege());
        cut.saveConfiguration(config);

        assertThat(capturing.getCapturedLogMessages()).isEmpty();

        //however, if the pattern changes, it will be audited
        config.setProtectionPattern(newProtectionPattern);
        cut.saveConfiguration(config);

        assertThat(capturing.getCapturedLogMessages().get(0)).contains(ADMIN_USER_NAME);
        assertThat(capturing.getCapturedLogMessages().get(0)).contains("<>");
        assertThat(capturing.getCapturedLogMessages().get(0)).contains(String.format("<%s>", newProtectionPattern));
    }

    private void setAdminRole() {

        Subject adminSubject = mock(Subject.class);
        when(adminSubject.hasRole(Role.ADMIN)).thenReturn(true);

        //provide a username for the subject
        PrincipalCollection mockedPrincipals = mock(PrincipalCollection.class);
        when(mockedPrincipals.oneByType(User.class)).thenReturn(new User(ADMIN_USER_NAME));

        when(adminSubject.getPrincipals()).thenReturn(mockedPrincipals);
        ThreadContext.bind(adminSubject);
    }


}