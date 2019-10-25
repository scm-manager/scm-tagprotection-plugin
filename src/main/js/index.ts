import { ConfigurationBinder as cfgBinder } from "@scm-manager/ui-components";
import GlobalTagProtectionConfiguration from "./GlobalTagProtectionConfiguration";

cfgBinder.bindGlobal(
  "/tagProtection",
  "scm-tagProtection-plugin.nav-link",
  "tagProtection",
  GlobalTagProtectionConfiguration
);
