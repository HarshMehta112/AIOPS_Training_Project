package com.org.motadata.engines;

import com.org.motadata.impl.CrudOperations;
import com.org.motadata.impl.InitializeRouter;
import com.org.motadata.utils.Constants;
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
public class DiscoveryEngine extends AbstractVerticle implements InitializeRouter, CrudOperations
{
    @Override
    public void start()
    {


    }

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
        // check name, ip, port, credential profile id if null then return
        var deviceContext = routingContext.body().asJsonObject();

        if(deviceContext != null && deviceContext.containsKey(Constants.DISCOVERY_NAME)
                && deviceContext.containsKey(Constants.IP_ADDRESS)
                && deviceContext.containsKey(Constants.PORT) &&
                deviceContext.containsKey(Constants.CREDENTIAL_PROFILE_ID))
        {

        }
        else
        {
            deviceContext = new JsonObject();

            deviceContext.put(Constants.DISCOVERY_NAME, "").put(Constants.CREDENTIAL_PROFILE_ID,"")
                    .put(Constants.PORT,"").put(Constants.IP_ADDRESS,"");

            routingContext.response()
                    .putHeader("Content-Type", "text/plain")
                    .end("You must enter the following details\n" + deviceContext.encodePrettily());
        }

    }

    @Override
    public void getAll(RoutingContext routingContext) {
    }

    @Override
    public void update(RoutingContext routingContext) {

    }

    @Override
    public void delete(RoutingContext routingContext) {

    }

    private void run(RoutingContext routingContext) {

    }


    private void deviceProvision(RoutingContext routingContext) {

    }
}
