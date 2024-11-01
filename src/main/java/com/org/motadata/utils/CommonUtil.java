package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.stream.Stream;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:19 PM
 */
public class CommonUtil
{
    private CommonUtil() {}

    private static final LoggerUtil LOGGER = new LoggerUtil(CommonUtil.class);

    public static String buildString(String ... variableStrings)
    {
        var stringBuilder = new StringBuilder();

        Stream.of(variableStrings).forEach(stringBuilder::append); // Append each string to the StringBuilder

        return stringBuilder.toString(); // Convert StringBuilder to String
    }

    public static String encrypt(String data)
    {
        try
        {
            SecretKeySpec secretKey = new SecretKeySpec(Constants.ENCRYPTION_KEY.getBytes(), Constants.ENCRYPTION_ALGORITHM);

            Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedData = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encryptedData);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return null;
    }

    public static String decrypt(String encryptedData)
    {
        try
        {
            SecretKeySpec secretKey = new SecretKeySpec(Constants.ENCRYPTION_KEY.getBytes(), Constants.ENCRYPTION_ALGORITHM);

            Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

            return new String(decryptedData);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return null;
    }

    public static void handleModificationRequest(JsonObject queryBuildContext, RoutingContext routingContext,
                                           String successMessage, String failureMessage)
    {
        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST, queryBuildContext, queryBuilderReply ->
        {
            if (queryBuilderReply.succeeded())
            {
                queryBuildContext.put(Constants.QUERY, queryBuilderReply.result().body());

                Bootstrap.getVertx().eventBus().<Boolean>request(Constants.DB_REQUESTS, queryBuildContext, dbOperationReply ->
                {
                    var responseMessage = Boolean.TRUE.equals(dbOperationReply.result().body())
                            ? successMessage : failureMessage;

                    routingContext.response()
                            .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_TEXT_PLAIN)
                            .end(responseMessage);
                });
            }
        });
    }

    public static void handleSelectRequest(JsonObject queryBuildContext, RoutingContext routingContext)
    {
        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST,queryBuildContext,
                queryBuilderReply->
                {
                    if(queryBuilderReply.succeeded())
                    {
                        queryBuildContext.put(Constants.QUERY,queryBuilderReply.result().body());

                        Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS,queryBuildContext,
                                dbOperationReply ->
                                {
                                    if(dbOperationReply.succeeded())
                                    {
                                        routingContext.response()
                                                .putHeader(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_APPLICATION_JSON)
                                                .end(dbOperationReply.result().body().encodePrettily());
                                    }
                                });
                    }
                });
    }
}
