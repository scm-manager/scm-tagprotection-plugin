/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
