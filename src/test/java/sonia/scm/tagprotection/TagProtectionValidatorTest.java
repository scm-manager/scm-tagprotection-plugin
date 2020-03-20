/*
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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Oliver Milke
 */
@SubjectAware(configuration = "classpath:sonia/scm/tagprotection/shiro.ini")
public class TagProtectionValidatorTest {

    private static final Repository REPOSITORY = new Repository("id", "git", "space", "X");

    @Rule
    public ShiroRule shiroRule = new ShiroRule();

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
    @SubjectAware(username = "trillian", password = "secret")
    public void assertThatAdminCanRemoveProtectedTags() {

        preparedConfiguration.setProtectionPattern("t1");
        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is admin, hence no protection needed
        assertThat(result).isFalse();
    }

    @Test
    @SubjectAware(username = "marvin", password = "secret")
    public void assertThatOwnerCanRemoveTags() {

        preparedConfiguration.setProtectionPattern("t1");
        preparedConfiguration.setReduceOwnerPrivilege(false);

        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is Owner and privilege reduction is not configured, hence no protection needed
        assertThat(result).isFalse();
    }

    @Test
    @SubjectAware(username = "marvin", password = "secret")
    public void assertThatReducedOwnerCannotRemoveTags() {

        preparedConfiguration.setProtectionPattern("t1");
        preparedConfiguration.setReduceOwnerPrivilege(true);

        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

        //pattern configured and matching a given tag. But user is Owner, but reduction is configured, hence  protection is needed
        assertThat(result).isTrue();
    }

    @Test
    @SubjectAware(username = "unpriv", password = "secret")
    public void assertThatEmptyTagListRequiresNoProtection() throws Exception {

        boolean result = cut.tagsMustBeProtected(REPOSITORY, Collections.EMPTY_LIST);

        //no tags to handle, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    @SubjectAware(username = "unpriv", password = "secret")
    public void assertThatEmptyPatternRequiresNoProtection() {

        preparedConfiguration.setProtectionPattern("");
        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

        //no pattern configured, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    @SubjectAware(username = "unpriv", password = "secret")
    public void assertThatNonMatchingPatternRequiresNoProtection() {

        preparedConfiguration.setProtectionPattern("any");
        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

        //configured pattern does not match any of the tags, no protection needed
        assertThat(result).isFalse();
    }

    @Test
    @SubjectAware(username = "unpriv", password = "secret")
    public void assertThatMatchingPatternRequiresProtection() {

        preparedConfiguration.setProtectionPattern("t1");
        boolean result = cut.tagsMustBeProtected(REPOSITORY, tagsOf("t1", "t2"));

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

    private List<Tag> tagsOf(String... names) {

        List<Tag> result = new ArrayList<>();

        for (String name : names) {

            result.add(new Tag(name, "1.0"));
        }

        return result;
    }


}