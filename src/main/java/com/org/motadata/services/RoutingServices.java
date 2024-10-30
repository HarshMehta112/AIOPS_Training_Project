package com.org.motadata.services;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.authentication.TokenCredentials;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:52 PM
 */
public class RoutingServices extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigurationService.class);

    @Override
    public void start()
    {
        var apiRouter = Router.router(Bootstrap.getVertx());

        // Public login route
        apiRouter.post("/login").handler(AuthenticationServices::loginHandler);

        // Protected route with JWT authentication
        apiRouter.route("/*").handler(this::authHandler);

        // Actual protected endpoint
        apiRouter.post("/test").handler(ctx ->
        {
            ctx.response().end("This is a protected resource!");
        });

        // Start HTTP server
        Bootstrap.getVertx().createHttpServer(new HttpServerOptions()
                .setSsl(true).setKeyCertOptions(new JksOptions().setPath(ConfigurationService.getSslKeystorePath())
                        .setPassword(ConfigurationService.getSslKeystorePassword())))
                .requestHandler(apiRouter).listen(8443)
                .onComplete(asyncResult->
        {
            if(asyncResult.succeeded())
            {
                LOGGER.info("Server started on https ");
            }
            else
            {
                LOGGER.error("Some error occurred " + asyncResult.cause().getMessage(), asyncResult.cause().getStackTrace());
            }
        });

    }

    // Custom authentication handler
    private void authHandler(RoutingContext routingContext)
    {
        var authHeader = routingContext.request().getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer "))
        {
            var token = authHeader.substring(7); // Remove "Bearer " prefix

            if (token.isEmpty())
            {
                routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE)
                        .end("Unauthorized: Token is empty.");
                return;
            }

            ConfigurationService.getJwtAuth().authenticate(new TokenCredentials(token), res ->
            {
                if (res.succeeded())
                {
                    routingContext.next(); // Authentication succeeded, proceed to the next handler
                }
                else
                {
                    String refreshToken = AuthenticationServices.getRefreshTokenStore().get("admin");

                    if(isTokenExpired(refreshToken.split(Constants.VALUE_SEPARATOR_WITH_ESCAPE)[1]))
                    {
                        AuthenticationServices.refreshTokenHandler(routingContext,refreshToken);
                    }
                    else
                    {
                        routingContext.response().setStatusCode(404).end("Unauthorized: Invalid token provided.");

                        LOGGER.error("Some error occurred in API Authentication process " + res.cause().getMessage(),res.cause().getStackTrace());
                    }
                }
            });
        }
        else
        {
            routingContext.response().setStatusCode(Constants.HTTP_UNAUTHORIZED_STATUS_CODE).end("Unauthorized: No token provided.");

            LOGGER.warn("Wrong Bearer Token received in API Authentication process ");

        }
    }

    private boolean isTokenExpired(String generatedDate)
    {
        LocalDateTime pastDateTime = LocalDateTime.parse(generatedDate);

        LocalDateTime currentDateTime = LocalDateTime.now();

        Duration duration = Duration.between(pastDateTime, currentDateTime);

        long seconds = duration.getSeconds();

        LOGGER.info("Duration of access token : " + seconds + " seconds.");

        return seconds >= 30;
    }


}
