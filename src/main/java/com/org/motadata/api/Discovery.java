package com.org.motadata.api;

import com.org.motadata.Bootstrap;
import com.org.motadata.impl.CrudOperations;
import com.org.motadata.impl.InitializeRouter;
import com.org.motadata.utils.CipherUtil;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.HandleRequestUtil;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


/**
 * This class have logic for crud operations of discovery profile.
 * I also added api for provision and discovery run here.
 * */

public class Discovery implements InitializeRouter, CrudOperations
{
    private static final LoggerUtil LOGGER = new LoggerUtil(Discovery.class);

    @Override
    public void initRouter(Router router)
    {
        router.get("/" + "getAll").handler(this::getAll);

        router.post("/" + "create").handler(this::create);

        router.put("/" + "update" + "/:id").handler(this::update);

        router.delete("/" + "delete" + "/:id").handler(this::delete);

        router.post("/" + "run" + "/:id").handler(this::run);

        router.post("/" + "provision" + "/:id").handler(this::provision);
    }

    @Override
    public void create(RoutingContext routingContext)
    {
        try
        {
            // check name, ip, port, credential profile id if null then return
            var discoveryContext = routingContext.body().asJsonObject();

            if(CommonUtil.isNonNull.test(discoveryContext) && discoveryContext.containsKey(Constants.DISCOVERY_NAME)
                    && discoveryContext.containsKey(Constants.IP_ADDRESS)
                    && discoveryContext.containsKey(Constants.PORT) &&
                    discoveryContext.containsKey(Constants.CREDENTIAL_PROFILE_ID))
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.INSERT_OPERATION)
                        .put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE)
                        .put(Constants.DB_VALUES,discoveryContext);

                HandleRequestUtil.handleModificationRequest(queryBuildContext,routingContext,
                        "Discovery Profile added successfully...",
                        "Discovery Profile not created. Please try again...");
            }
            else
            {
                discoveryContext = new JsonObject();

                discoveryContext.put(Constants.DISCOVERY_NAME, "").put(Constants.CREDENTIAL_PROFILE_ID, Constants.ZERO)
                        .put(Constants.PORT,Constants.ZERO).put(Constants.IP_ADDRESS,"");

                routingContext.response()
                        .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                        .end("You must enter the following details\n" + discoveryContext.encodePrettily());
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
        try
        {
            var queryBuildContext = new JsonObject();

            queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.SELECT_OPERATION);

            queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE);

            HandleRequestUtil.handleSelectRequest(queryBuildContext,routingContext);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    @Override
    public void update(RoutingContext routingContext)
    {
        try
        {
            // check any param is there otherwise return
            var discoveryContext = routingContext.body().asJsonObject();

            var discoveryId = routingContext.request().getParam(Constants.ID);

            if(CommonUtil.isNonNull.test(discoveryContext))
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.UPDATE_OPERATION)
                        .put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE)
                        .put(Constants.DB_CONDITIONS,CommonUtil
                        .buildString(Constants.ID," = ", discoveryId))
                        .put(Constants.DB_VALUES,discoveryContext);

                HandleRequestUtil.handleModificationRequest(queryBuildContext,routingContext,
                        "Discovery Profile updated successfully...",
                        "Discovery Profile not updated. Please try again...");
            }
            else
            {
                discoveryContext = new JsonObject();

                discoveryContext.put(Constants.DISCOVERY_NAME, "").put(Constants.CREDENTIAL_PROFILE_ID, Constants.ZERO)
                        .put(Constants.PORT,Constants.ZERO).put(Constants.IP_ADDRESS,"");

                routingContext.response()
                        .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                        .end("You must enter one of the following details to update discovery profile.\n"
                                + discoveryContext.encodePrettily());
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    @Override
    public void delete(RoutingContext routingContext)
    {
        try
        {
            var queryBuildContext = new JsonObject();

            var discoveryId = routingContext.request().getParam(Constants.ID);

            queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.DELETE_OPERATION)
                    .put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE)
                    .put(Constants.DB_CONDITIONS,CommonUtil
                    .buildString(Constants.ID," = " , discoveryId));

            HandleRequestUtil.handleModificationRequest(queryBuildContext,routingContext,
                    "Discovery Profile deleted successfully...",
                    "Discovery Profile not deleted. Please try again...");
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    private void run(RoutingContext routingContext)
    {
        try
        {
            var discoveryId = routingContext.request().getParam(Constants.ID);

            var dbRequestContext = new JsonObject();

            dbRequestContext.put(Constants.DB_OPERATION_TYPE,Constants.SELECT_OPERATION)
                    .put(Constants.QUERY,Constants.RUN_DISCOVERY_DATA_QUERY.replace(Constants.HASH_SEPARATOR,discoveryId));

            Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS, dbRequestContext, dbOperationReply ->
            {
                if (dbOperationReply.succeeded())
                {
                    var deviceData = dbOperationReply.result().body();

                    if (CommonUtil.isNonNull.test(deviceData) && !deviceData.isEmpty())
                    {
                        var deviceContext = deviceData.getJsonObject(0);

                        deviceContext.put(Constants.SSH_PASSWORD,
                                CipherUtil.decrypt(deviceContext.getString(Constants.SSH_PASSWORD)))
                                .put(Constants.DEVICE_TYPE,Constants.SSH)
                                .put(Constants.PLUGIN_CALL_CATEGORY,Constants.DISCOVERY)
                                .put(Constants.PORT,deviceContext.getString(Constants.PORT));

                        deviceData.set(0,deviceContext);

                        LOGGER.info(deviceData.encodePrettily());

                        Bootstrap.getVertx().eventBus().request(Constants.DISCOVERY_RUN_REQUEST,deviceData, reply->
                        {
                            if (reply.succeeded())
                            {
                                routingContext.response()
                                        .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                                        .end("Discovery Result : "+reply.result().body().toString());
                            }
                        });
                    }
                }
                else
                {
                    routingContext.response()
                            .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                            .end("No Such discovery device by this id");
                }
            });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }


    private void provision(RoutingContext routingContext)
    {
        try
        {
            var dbOperationContext = new JsonObject();

            var discoveryId = routingContext.request().getParam(Constants.ID);

            dbOperationContext.put(Constants.QUERY,Constants.DISCOVERY_PROVISION_SP.
                    replace(Constants.HASH_SEPARATOR,discoveryId));

            dbOperationContext.put(Constants.DB_OPERATION_TYPE,Constants.SP_CALL);

            Bootstrap.getVertx().eventBus().<Boolean>request(Constants.DB_REQUESTS,dbOperationContext,
                    dbOperationReply ->
                    {
                        var responseMessage = Boolean.TRUE.equals(dbOperationReply.result().body())
                                ? "Monitor provisioned successfully...." :
                                "Monitor not provisioned please try again ....";

                        routingContext.response()
                                .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                                .end(responseMessage);
                    });
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

    }
}
