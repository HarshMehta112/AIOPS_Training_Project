package com.org.motadata.service;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/31/24 1:15 AM
 */
public class QueryBuilder extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(QueryBuilder.class);

    @Override
    public void start()
    {
        Bootstrap.getVertx().eventBus().<JsonObject>localConsumer(Constants.QUERY_BUILD_REQUEST, handler ->

                Bootstrap.getVertx().executeBlocking(promise ->
                {
                    try
                    {
                        var queryBuildContext = handler.body();

                        var query = buildQuery(queryBuildContext.getString(Constants.DB_OPERATION_TYPE),
                                queryBuildContext.getString(Constants.DB_TABLE_NAME),
                                queryBuildContext.containsKey(Constants.DB_VALUES)?queryBuildContext.getJsonObject(Constants.DB_VALUES):null,
                                queryBuildContext.containsKey(Constants.DB_CONDITIONS)?queryBuildContext.getString(Constants.DB_CONDITIONS):null);

                        LOGGER.info("Built Query : " + query);

                        promise.complete(query);
                    }
                    catch (Exception exception)
                    {

                        LOGGER.error(exception.getMessage(),exception.getStackTrace());

                        promise.fail(exception);
                    }

                },false, asyncResult ->
                {
                    if(asyncResult.succeeded())
                    {
                        handler.reply(asyncResult.result());
                    }
                    else
                    {
                        LOGGER.error("Some issue in building query .." + asyncResult.cause().getMessage()
                                , asyncResult.cause().getStackTrace());
                    }
                })).exceptionHandler(exceptionHandler->LOGGER.error(exceptionHandler.getMessage(),exceptionHandler.getStackTrace()));
    }

    public String buildQuery(String operation, String table, JsonObject values, String conditions)
    {
        return switch (operation.toUpperCase()) {
            case Constants.SELECT_OPERATION -> buildSelect(table, conditions);
            case Constants.INSERT_OPERATION -> buildInsert(table, values);
            case Constants.UPDATE_OPERATION -> buildUpdate(table, values, conditions);
            case Constants.DELETE_OPERATION -> buildDelete(table, conditions);
            default -> throw new IllegalArgumentException("Invalid operation: " + operation);
        };
    }

    public String buildSelect(String tableName, String condition)
    {
        return CommonUtil.buildString("SELECT * FROM " , tableName ,
                (condition != null && !condition.isEmpty() ? " WHERE " + condition : ""));
    }

    public String buildInsert(String tableName, JsonObject params)
        {
        List<String> fields = new ArrayList<>(params.fieldNames());

        String columns = String.join(", ", fields);

        String values = fields.stream()
                .map(key -> "$" + (fields.indexOf(key) + 1))
                .collect(Collectors.joining(", "));

        return CommonUtil.buildString("INSERT INTO " , tableName , " (" , columns , ") VALUES (" , values , ")");
    }

    public String buildUpdate(String tableName, JsonObject params, String condition)
    {
        List<String> fields = params.fieldNames().stream().toList();

        String setClause = fields.stream()
                .map(key -> key + " = $" + (fields.indexOf(key) + 1))
                .collect(Collectors.joining(", "));

        return CommonUtil.buildString("UPDATE ",tableName," SET ",setClause,
                (condition != null && !condition.isEmpty() ? " WHERE " + condition : ""));
    }

    public String buildDelete(String tableName, String condition)
    {
        return CommonUtil.buildString("DELETE FROM ",tableName,
                (condition != null && !condition.isEmpty() ? " WHERE " + condition : ""));
    }
}
