import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";
import TagProtectionConfigurationForm from "./TagProtectionConfigurationForm";

type Props = WithTranslation & {
  link: string;
};

class GlobalTagProtectionConfiguration extends React.Component<Props> {
  render() {
    const { t, link } = this.props;
    return (
      <>
        <Title title={t("scm-tagProtection-plugin.config.form.header")} />
        <Configuration link={link} render={props => <TagProtectionConfigurationForm {...props} />} />
      </>
    );
  }
}

export default withTranslation("plugins")(GlobalTagProtectionConfiguration);
