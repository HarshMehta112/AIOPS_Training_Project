package com.org.motadata.engines;

import com.org.motadata.Bootstrap;
import com.org.motadata.impl.CrudOperations;
import com.org.motadata.impl.InitializeRouter;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 12:36 PM
 */
public class CredentialProfileEngine implements InitializeRouter, CrudOperations
{

    private static final LoggerUtil LOGGER = new LoggerUtil(CredentialProfileEngine.class);

    @Override
    public void create(RoutingContext routingContext)
    {
        try
        {
            // check name, username, password then return
            var credentialContext = routingContext.body().asJsonObject();

            if(credentialContext != null && credentialContext.containsKey(Constants.CREDENTIAL_PROFILE_NAME)
                    && credentialContext.containsKey(Constants.SSH_USERNAME)
                    && credentialContext.containsKey(Constants.SSH_PASSWORD))
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.INSERT_OPERATION);

                queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.CREDENTIAL_PROFILE_TABLE);

                queryBuildContext.put(Constants.DB_VALUES,credentialContext);

                Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST,queryBuildContext, queryBuilderReply->
                {
                    if(queryBuilderReply.succeeded())
                    {
                        LOGGER.info("Harsh queryBuilderReply " + queryBuilderReply.result().body());

                        queryBuildContext.put(Constants.QUERY,queryBuilderReply.result().body());

                        LOGGER.info("Harsh queryBuilderReply " + queryBuildContext.encodePrettily());

                        Bootstrap.getVertx().eventBus().<Boolean>request(Constants.DB_REQUESTS,queryBuildContext, dbOperationReply ->
                        {
                            var responseMessage = Boolean.TRUE.equals(dbOperationReply.result().body())
                                    ? "Credential Profile added successfully..."
                                    : "Credential Profile not created. Please try again...";

                            routingContext.response()
                                    .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                                    .end(responseMessage);
                        });
                    }
                });
            }
            else
            {
                credentialContext = new JsonObject();

                credentialContext.put(Constants.CREDENTIAL_PROFILE_NAME, "").put(Constants.SSH_USERNAME,"")
                        .put(Constants.SSH_PASSWORD,"");

                routingContext.response()
                        .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                        .end("You must enter the following details to create new credential profile.\n" + credentialContext.encodePrettily());
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    @Override
    public void getAll(RoutingContext routingContext)
    {
        var queryBuildContext = new JsonObject();

        queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.SELECT_OPERATION);

        queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.CREDENTIAL_PROFILE_TABLE);

        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST,queryBuildContext, queryBuilderReply->
        {
            if(queryBuilderReply.succeeded())
            {
                queryBuildContext.put(Constants.QUERY,queryBuilderReply.result().body());

                Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS,queryBuildContext, dbOperationReply ->
                {
                    routingContext.response()
                            .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                            .end(dbOperationReply.result().body().encodePrettily());
                });
            }
        });

    }

    @Override
    public void update(RoutingContext routingContext) {

    }

    @Override
    public void delete(RoutingContext routingContext) {

    }

    @Override
    public void initRouter(Router router)
    {
        router.get("/" + "getAll").handler(this::getAll);

        router.post("/" + "create").handler(this::create);

        router.put("/" + "update" + "/:id").handler(this::update);

        router.delete("/" + "delete" + "/:id").handler(this::delete);
    }
}
