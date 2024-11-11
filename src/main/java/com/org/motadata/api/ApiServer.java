package com.org.motadata.api;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.AuthenticationUtil;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * This class handles the all requests from the http server.
 * I write logic that, all request (/*) doing authenticate using JWT first the
 * rerouting to according to the request.
 * At a login time it will generate 2 tokens 1. access token 2. refresh token
 * for this project my refresh token validity = service start to stop time.
 * access token validity = 30 seconds
 * I write logic that, if any request arrives and token is expired
 * then automatically new access token generated.
 * */

public class ApiServer extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ApiServer.class);

    @Override
    public void start()
    {
        var apiRouter = Router.router(Bootstrap.getVertx());

        var discoveryRouter = Router.router(Bootstrap.getVertx());

        var credentialProfileRouter = Router.router(Bootstrap.getVertx());

        var monitorRouter = Router.router(Bootstrap.getVertx());

        var bodyHandler = BodyHandler.create().setBodyLimit(10000000000L);

        // Public login route
        apiRouter.post("/login").handler(AuthenticationUtil::loginHandler);

        // Protected route with JWT authentication
        apiRouter.route("/*").handler(AuthenticationUtil::authHandler);

        apiRouter.route("/discovery/*").handler(bodyHandler).subRouter(discoveryRouter);

        apiRouter.route("/credentialProfile/*").handler(bodyHandler).subRouter(credentialProfileRouter);

        apiRouter.route("/monitor/*").handler(bodyHandler).subRouter(monitorRouter);

        new Discovery().initRouter(discoveryRouter);

        new CredentialProfile().initRouter(credentialProfileRouter);

        new Monitor().initRouter(monitorRouter);

        // Start HTTP server
        Bootstrap.getVertx().createHttpServer(new HttpServerOptions()
                .setSsl(true).setKeyCertOptions(new JksOptions().setPath(ConfigLoaderUtil.getSslKeystorePath())
                        .setPassword(ConfigLoaderUtil.getSslKeystorePassword())))
                .requestHandler(apiRouter).listen(ConfigLoaderUtil.getHttpServerPort())
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
