/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sonia.scm.tagprotection;

import com.thekua.spikes.LogbackCapturingAppender;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.user.User;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.tagprotection.TagProtectionConfigurationStore.STORE_TYPE;

/**
 * @author Oliver Milke
 */
public class TagProtectionConfigurationStoreTest {

    private static final String ADMIN_USER_NAME = "AdminUser";

    private TagProtectionConfigurationStore cut;
    private ConfigurationStore<TagProtectionConfig> mockedStore;

    @Before
    public void prepareTest() {

        ConfigurationStoreFactory factory = new InMemoryConfigurationStoreFactory();
        mockedStore = factory.withType(TagProtectionConfig.class).withName(STORE_TYPE).build();

        cut = new TagProtectionConfigurationStore(factory);
    }

    @Test
    public void assertThatDefaultConfigurationIsProvided() {

        TagProtectionConfig configuration = cut.getConfiguration();

        //prove that without a persisted configuration, the values match with the default values as defined by TagProtectionConfig

        assertThat(configuration.getProtectionPattern()).isEqualTo(new TagProtectionConfig().getProtectionPattern());
        assertThat(configuration.isReduceOwnerPrivilege()).isEqualTo(new TagProtectionConfig().isReduceOwnerPrivilege());
    }

    @Test
    public void assertThatPersistedConfigurationIsProvided() {

        TagProtectionConfig persistedConfiguration = new TagProtectionConfig();
        mockedStore.set(persistedConfiguration);

        TagProtectionConfig configuration = cut.getConfiguration();

        //prove that a persisted configuration is returned
        assertThat(configuration).isSameAs(persistedConfiguration);
    }

    @Test
    public void assertThatChangedPatternIsAudited() {

        setAdminRole();
        LogbackCapturingAppender capturing = LogbackCapturingAppender.weaveInto(TagProtectionConfigurationStore.logger);

        TagProtectionConfig config = new TagProtectionConfig();

        //invert non-audited flag
        config.setReduceOwnerPrivilege(!config.isReduceOwnerPrivilege());
        cut.saveConfiguration(config);

        assertThat(capturing.getCapturedLogMessages()).isEmpty();

        //however, if the pattern changes, it will be audited
        TagProtectionConfig changedConfig = new TagProtectionConfig();
        String newProtectionPattern = "protect/*";
        changedConfig.setProtectionPattern(newProtectionPattern);
        cut.saveConfiguration(changedConfig);

        String capturedLogMessage = capturing.getCapturedLogMessages().get(0);
        assertThat(capturedLogMessage).contains(ADMIN_USER_NAME);
        assertThat(capturedLogMessage).contains("<>");
        assertThat(capturedLogMessage).contains(String.format("<%s>", newProtectionPattern));
    }

    private void setAdminRole() {
        Subject adminSubject = mock(Subject.class);

        //provide a username for the subject
        PrincipalCollection mockedPrincipals = mock(PrincipalCollection.class);
        when(mockedPrincipals.oneByType(User.class)).thenReturn(new User(ADMIN_USER_NAME));

        when(adminSubject.getPrincipals()).thenReturn(mockedPrincipals);
        ThreadContext.bind(adminSubject);
    }


}