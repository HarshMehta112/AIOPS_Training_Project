package com.org.motadata.engines;

import com.org.motadata.impl.CrudOperations;
import com.org.motadata.impl.InitializeRouter;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 1:54 PM
 */
public class DiscoveryEngine implements InitializeRouter, CrudOperations
{
    private static final LoggerUtil LOGGER = new LoggerUtil(DiscoveryEngine.class);

    @Override
    public void initRouter(Router router)
    {
        router.get("/" + "getAll").handler(this::getAll);

        router.post("/" + "create").handler(this::create);

        router.put("/" + "update" + "/:id").handler(this::update);

        router.delete("/" + "delete" + "/:id").handler(this::delete);

        router.post("/" + "run" + "/:id").handler(this::run);

        router.post("/" + "provision" + "/:id").handler(this::deviceProvision);
    }

    @Override
    public void create(RoutingContext routingContext)
    {
        try
        {
            // check name, ip, port, credential profile id if null then return
            var discoveryContext = routingContext.body().asJsonObject();

            if(discoveryContext != null && discoveryContext.containsKey(Constants.DISCOVERY_NAME)
                    && discoveryContext.containsKey(Constants.IP_ADDRESS)
                    && discoveryContext.containsKey(Constants.PORT) &&
                    discoveryContext.containsKey(Constants.CREDENTIAL_PROFILE_ID))
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.INSERT_OPERATION);

                queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE);

                queryBuildContext.put(Constants.DB_VALUES,discoveryContext);

                CommonUtil.handleModificationRequest(queryBuildContext,routingContext,
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

            CommonUtil.handleSelectRequest(queryBuildContext,routingContext);
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

            if(discoveryContext != null)
            {
                var queryBuildContext = new JsonObject();

                queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.UPDATE_OPERATION);

                queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE);

                queryBuildContext.put(Constants.DB_CONDITIONS,CommonUtil
                        .buildString(Constants.ID," = ", discoveryId));

                queryBuildContext.put(Constants.DB_VALUES,discoveryContext);

                CommonUtil.handleModificationRequest(queryBuildContext,routingContext,
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

            queryBuildContext.put(Constants.DB_OPERATION_TYPE,Constants.DELETE_OPERATION);

            queryBuildContext.put(Constants.DB_TABLE_NAME,Constants.DISCOVERY_PROFILE_TABLE);

            queryBuildContext.put(Constants.DB_CONDITIONS,CommonUtil
                    .buildString(Constants.ID," = " , discoveryId));

            CommonUtil.handleModificationRequest(queryBuildContext,routingContext,
                    "Discovery Profile deleted successfully...",
                    "Discovery Profile not deleted. Please try again...");
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    private void run(RoutingContext routingContext) {

    }


    private void deviceProvision(RoutingContext routingContext) {

    }
}
