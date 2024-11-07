package com.org.motadata.utils;

import com.org.motadata.constant.Constants;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.RoutingContext;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class AuthenticationUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigLoaderUtil.class);

    private AuthenticationUtil() {}

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

        var loginUsername = Optional.ofNullable(ConfigLoaderUtil.getLoginUsername());

        var loginPassword = Optional.ofNullable(ConfigLoaderUtil.getLoginPassword());

        Predicate<String> isUsernameValid = user -> user.equals(username);

        Predicate<String> isPasswordValid = pass -> pass.equals(password);

        if (loginUsername.filter(isUsernameValid).isPresent() &&
                loginPassword.filter(isPasswordValid).isPresent())
        {
            // Generate an access token & refresh token
            var accessToken = ConfigLoaderUtil.getJwtAuth().generateToken(
                    new JsonObject().put(Constants.USER_NAME,username),
                    new JWTOptions().setAlgorithm(Constants.JWT_TOKEN_ALGORITHM)
                            .setExpiresInSeconds(30000));

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

        if (refreshTokenStore.containsKey(username) && refreshTokenStore.get(username) != null)
        {
            // Generate a new access token
            var newAccessToken = ConfigLoaderUtil.getJwtAuth().generateToken(new JsonObject()
                    .put(Constants.USER_NAME, username), new JWTOptions().
                    setAlgorithm(Constants.JWT_TOKEN_ALGORITHM).setExpiresInSeconds(30));

            routingContext.response().putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                    .end(new JsonObject().put("message","Your previous access token expired, So generated new Token..." +
                                    " please request again using below mentioned token")
                            .put(Constants.ACCESS_TOKEN, newAccessToken).encodePrettily());

            refreshTokenStore.put(username,CommonUtil.buildString(refreshToken,
                    Constants.VALUE_SEPARATOR,LocalDateTime.now().toString()));
        }
        else
        {
            routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE)
                    .end("Unauthorized: Invalid refresh token.");
        }
    }

    // Custom authentication handler
    public static void authHandler(RoutingContext routingContext)
    {
        var authHeader = routingContext.request().getHeader("Authorization");

        Predicate<String> isBearerToken = header -> header.startsWith("Bearer ");

        Predicate<String> isTokenEmpty = String::isEmpty;

        var optionalAuthHeader = Optional.ofNullable(authHeader);

        if (optionalAuthHeader.filter(isBearerToken).isPresent())
        {
            var token = authHeader.substring(7); // Remove "Bearer " prefix

            if (isTokenEmpty.test(token))
            {
                routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE)
                        .end("Unauthorized: Token is empty.");
                return;
            }

            ConfigLoaderUtil.getJwtAuth().authenticate(new TokenCredentials(token), res ->
            {
                if (res.succeeded())
                {
                    routingContext.next(); // Authentication succeeded, proceed to the next handler
                }
                else
                {
                    String refreshToken = AuthenticationUtil.getRefreshTokenStore().get("admin");

                    if(CommonUtil.isNonNull.test(refreshToken) &&
                            isTokenExpired(refreshToken.split(Constants.VALUE_SEPARATOR_WITH_ESCAPE)[1]))
                    {
                        AuthenticationUtil.refreshTokenHandler(routingContext,refreshToken);
                    }
                    else
                    {
                        routingContext.response().setStatusCode(404).end("Unauthorized: Invalid token provided.");

                        LOGGER.error("Some error occurred in API Authentication process " +
                                res.cause().getMessage(),res.cause().getStackTrace());
                    }
                }
            });
        }
        else
        {
            routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE)
                    .end("Unauthorized: No token provided.");

            LOGGER.warn("Wrong Bearer Token received in API Authentication process ");

        }
    }

    public static boolean isTokenExpired(String generatedDate)
    {
        LocalDateTime pastDateTime = LocalDateTime.parse(generatedDate);

        LocalDateTime currentDateTime = LocalDateTime.now();

        Duration duration = Duration.between(pastDateTime, currentDateTime);

        long seconds = duration.getSeconds();

        LOGGER.info("Duration of access token : " + seconds + " seconds.");

        return seconds >= 3000;
    }
}