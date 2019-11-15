package com.sap.support.test.core;

public class Constants {

    public enum AuthType {
        BASIC, OAUTH, SAML, NOAUTH
    }

    public enum OnboardingType {
        DISCOVERY_SERVICE, STANDARD
    }

    public enum UsageConsent {
        DENY, ALLOW
    }
    public static UsageConsent USAGE_CONSENT = UsageConsent.DENY;

    public final static AuthType APPLICATION_AUTH_TYPE = AuthType.OAUTH;
    public final static int NETWORK_REQUEST_TIMEOUT = 5000;

    public static OnboardingType ONBOARDING_TYPE = OnboardingType.DISCOVERY_SERVICE;

}
