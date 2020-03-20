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

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.user.User;

import java.util.List;


@Extension
@EagerSingleton
public class TagProtectionPreReceiveRepositoryHook {

  private static final Logger logger = LoggerFactory.getLogger(TagProtectionPreReceiveRepositoryHook.class);

  private final TagProtectionValidator tagProtectionValidator;

  @Inject
  public TagProtectionPreReceiveRepositoryHook(TagProtectionValidator tagProtectionValidator) {

    this.tagProtectionValidator = tagProtectionValidator;
  }

  @Subscribe(async = false)
  public void onEvent(PreReceiveRepositoryHookEvent event) {

    Repository repository = event.getRepository();
    if (repository != null) {

      Subject subject = SecurityUtils.getSubject();
      if (!event.getContext().isFeatureSupported(HookFeature.TAG_PROVIDER)) {
        return;
      }

      List<Tag> deletedTags = event.getContext().getTagProvider().getDeletedTags();

      boolean tagsMustBeProtected = tagProtectionValidator.tagsMustBeProtected(repository, deletedTags);
      if (tagsMustBeProtected) {

        User user = subject.getPrincipals().oneByType(User.class);
        String message = String.format("Deleting tags not allowed for user %s in repository %s/%s", user.getName(), repository.getNamespace(), repository.getName());
        logger.info(message);
        event.getContext().getMessageProvider().sendMessage(message);
        throw new TagProtectionException(message);
      } else {

        logger.trace("Tag Protection does not need to be enforced.");
      }
    } else {
      logger.warn("received hook without repository");

    }
  }

}
