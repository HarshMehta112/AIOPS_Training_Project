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
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:52 PM
 */
public class ApiServer extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(ConfigLoaderUtil.class);

    @Override
    public void start()
    {
        var apiRouter = Router.router(Bootstrap.getVertx());

        var discoveryRouter = Router.router(Bootstrap.getVertx());

        var credentialProfileRouter = Router.router(Bootstrap.getVertx());

        var bodyHandler = BodyHandler.create().setBodyLimit(100000000000000L);

        // Public login route
        apiRouter.post("/login").handler(AuthenticationUtil::loginHandler);

        // Protected route with JWT authentication
        apiRouter.route("/*").handler(AuthenticationUtil::authHandler);

        apiRouter.route("/discovery/*").handler(bodyHandler).subRouter(discoveryRouter);

        apiRouter.route("/credentialProfile/*").handler(bodyHandler).subRouter(credentialProfileRouter);

        new Discovery().initRouter(discoveryRouter);

        new CredentialProfile().initRouter(credentialProfileRouter);

        // Start HTTP server
        Bootstrap.getVertx().createHttpServer(new HttpServerOptions()
                .setSsl(true).setKeyCertOptions(new JksOptions().setPath(ConfigLoaderUtil.getSslKeystorePath())
                        .setPassword(ConfigLoaderUtil.getSslKeystorePassword())))
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
