package sonia.scm.tagprotection;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Oliver Milke
 */
public class TagProtectionConfigTest {

    @Test
    public void verifyDefaultValues() {

        TagProtectionConfig config = new TagProtectionConfig();

        assertThat(config.getProtectionPattern()).isEmpty();
        assertThat(config.isReduceOwnerPrivilege()).isFalse();
    }
}