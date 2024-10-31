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
        apiRouter.route("/*").handler(AuthenticationServices::authHandler);

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



}
