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
import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { TagProtectionConfiguration } from "./TagProtectionConfiguration";
import { InputField, Checkbox } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  initialConfiguration: TagProtectionConfiguration;
  readOnly: boolean;
  onConfigurationChange: (p1: TagProtectionConfiguration, p2: boolean) => void;
  transportStrategy: string;
};

type State = TagProtectionConfiguration;

class TagProtectionConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration
    };
  }

  configChangeHandler = (value: string, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  render() {
    const { t, readOnly } = this.props;
    return (
      <>
        <InputField
          name={"protectionPattern"}
          label={t("scm-tagProtection-plugin.config.form.protectionPattern")}
          helpText={t("scm-tagProtection-plugin.config.form.protectionPattern-helptext")}
          disabled={readOnly}
          value={this.state["protectionPattern"]}
          onChange={this.configChangeHandler}
        />
        <Checkbox
          name={"reduceOwnerPrivilege"}
          label={t("scm-tagProtection-plugin.config.form.reduceOwnerPrivilege")}
          helpText={t("scm-tagProtection-plugin.config.form.reduceOwnerPrivilege-helptext")}
          checked={this.state["reduceOwnerPrivilege"]}
          onChange={this.configChangeHandler}
          disabled={readOnly}
        />
      </>
    );
  }
}

export default withTranslation("plugins")(TagProtectionConfigurationForm);
