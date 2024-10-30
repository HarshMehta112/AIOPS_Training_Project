package com.org.motadata.services;

import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AuthenticationServices
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigurationService.class);

    private AuthenticationServices() {}

    // Add a map to store refresh tokens
    private static final HashMap<String, String> refreshTokenStore = new HashMap<>();

    public static Map<String, String> getRefreshTokenStore()
    {
        return refreshTokenStore;
    }

    public static void loginHandler(RoutingContext routingContext)
    {
        var username = routingContext.request().getParam(Constants.USER_NAME);

        var password = routingContext.request().getParam(Constants.PASSWORD);

        var loginUsername = ConfigurationService.getLoginUsername();

        var loginPassword = ConfigurationService.getLoginPassword();

        //TODO HARSH USE OPTIONAL INTERFACE
        if (loginUsername != null && loginUsername.equals(username)
                && loginPassword != null && loginPassword.equals(password))
        {
            // Generate an access token & refresh token
            var accessToken = ConfigurationService.getJwtAuth().generateToken(
                    new JsonObject().put(Constants.USER_NAME,username),
                    new JWTOptions().setAlgorithm(Constants.JWT_TOKEN_ALGORITHM)
                            .setExpiresInSeconds(30));

            var refreshToken = UUID.randomUUID().toString();

            // Store it with an association to the user
            refreshTokenStore.put(username,CommonUtil.buildString(refreshToken,
                    Constants.VALUE_SEPARATOR,LocalDateTime.now().toString()));

            // Return both tokens
            var tokens = new JsonObject()
                    .put(Constants.ACCESS_TOKEN, accessToken)
                    .put(Constants.REFRESH_TOKEN, refreshToken);

            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                    .end(tokens.encodePrettily());
        }
        else
        {
            routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE)
                    .end("Unauthorized: Invalid username or password.");
        }
    }

    // to handle refresh token requests
    public static void refreshTokenHandler(RoutingContext routingContext, String refreshToken)
    {
        // could be retrieved dynamically if needed
        var username = "admin";

        //TODO HARSH USE OPTIONAL INTERFACE
        if (refreshTokenStore.containsKey(username) && refreshTokenStore.get(username) != null)
        {
            // Generate a new access token
            var newAccessToken = ConfigurationService.getJwtAuth().generateToken(new JsonObject()
                    .put(Constants.USER_NAME, username), new JWTOptions().
                    setAlgorithm(Constants.JWT_TOKEN_ALGORITHM).setExpiresInSeconds(30));

            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                    .end(new JsonObject().put("message","Your previous access token expired, So generated new Token... please request again using below mentioned token")
                            .put("accessToken", newAccessToken).encodePrettily());

            refreshTokenStore.put(username,CommonUtil.buildString(refreshToken,
                    Constants.VALUE_SEPARATOR,LocalDateTime.now().toString()));
        }
        else
        {
            routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE).end("Unauthorized: Invalid refresh token.");
        }
    }
}
