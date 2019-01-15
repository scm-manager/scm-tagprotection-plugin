// @flow
import React from "react";
import { Title, Configuration } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import TagProtectionConfigurationForm from "./TagProtectionConfigurationForm";

type Props = {
    link: string,
    t: string => string
};

class GlobalTagProtectionConfiguration extends React.Component<Props> {
    constructor(props: Props) {
        super(props);
    }

    render() {
        const { t, link } = this.props;
        return (
            <>
                <Title title={t("scm-tagProtection-plugin.config.form.header")} />
                <Configuration
                    link={link}
                    render={props => <TagProtectionConfigurationForm {...props} />}
                />
            </>
        );
    }
}

export default translate("plugins")(GlobalTagProtectionConfiguration);
