package com.sap.support.logon;

import android.content.Context;

import com.sap.cloud.mobile.foundation.authentication.OAuth2Configuration;

/**
 * This class provides the OAuth configuration object for the application.
 *
 */
public class SAPOAuthConfigProvider {

    private final static String OAUTH_REDIRECT_URL = "https://oauthasservices-dgq5qdz5dm.int.sap.eu2.hana.ondemand.com";
    private final static String OAUTH_CLIENT_ID = "6677fe6e-1568-43ec-aa88-b6e98afdb271";
    private final static String AUTH_END_POINT = "https://oauthasservices-dgq5qdz5dm.int.sap.eu2.hana.ondemand.com/oauth2/api/v1/authorize";
    private final static String TOKEN_END_POINT = "https://oauthasservices-dgq5qdz5dm.int.sap.eu2.hana.ondemand.com/oauth2/api/v1/token";

    public static OAuth2Configuration getOAuthConfiguration(Context context) {

        OAuth2Configuration oAuth2Configuration = new OAuth2Configuration.Builder(context)
                .clientId(OAUTH_CLIENT_ID)
                .responseType("code")
                .authUrl(AUTH_END_POINT)
                .tokenUrl(TOKEN_END_POINT)
                .redirectUrl(OAUTH_REDIRECT_URL)
                .build();

        return oAuth2Configuration;
    }
}
