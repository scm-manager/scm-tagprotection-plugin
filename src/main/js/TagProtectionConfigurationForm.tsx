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
