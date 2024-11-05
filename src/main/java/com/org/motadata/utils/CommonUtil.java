package com.org.motadata.utils;

import com.org.motadata.Bootstrap;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Predicate;
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

    public static Future<JsonArray> handleSelectRequest(JsonObject queryBuildContext) {
        Promise<JsonArray> promise = Promise.promise();

        Bootstrap.getVertx().eventBus().<String>request(Constants.QUERY_BUILD_REQUEST, queryBuildContext, queryBuilderReply -> {
            if (queryBuilderReply.succeeded()) {
                queryBuildContext.put(Constants.QUERY, queryBuilderReply.result().body());

                Bootstrap.getVertx().eventBus().<JsonArray>request(Constants.DB_REQUESTS, queryBuildContext, dbOperationReply -> {
                    if (dbOperationReply.succeeded()) {
                        promise.complete(dbOperationReply.result().body());
                    } else {
                        promise.fail(dbOperationReply.cause());
                    }
                });
            } else {
                promise.fail(queryBuilderReply.cause());
            }
        });

        return promise.future();
    }



    public static JsonArray executePlugin(JsonArray deviceContext)
    {
        JsonArray batchResult = new JsonArray();

        try
        {
            String dataEncoder = Base64.getEncoder().encodeToString(deviceContext.toString()
                    .getBytes(StandardCharsets.UTF_8));

            LOGGER.info(dataEncoder);

            boolean categoryTypeCheck = deviceContext.getJsonObject(0)
                    .getString(Constants.PLUGIN_CALL_CATEGORY)
                    .equals(Constants.POLLING);

            Process process = startProcess(dataEncoder);

            if (process == null) return batchResult;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                processOutput(reader, batchResult, categoryTypeCheck);
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return batchResult;
    }

    private static Process startProcess(String dataEncoder)
    {
        Process process = null;

        try
        {
            ProcessBuilder processBuilder = new ProcessBuilder("/home/harsh/GolandProjects/AIOPS_Training_Project/AIOPS_Training_Project", dataEncoder);

            process = processBuilder.start();

            if (!process.waitFor(60, TimeUnit.SECONDS))
            {
                process.destroy();

                throw new InterruptedException("Process has been interrupted because of timeout (60 seconds).");
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return process;
    }

    private static void processOutput(BufferedReader reader, JsonArray batchResult, boolean categoryTypeCheck)
    {
        String line;

        try
        {
            while ((line = reader.readLine()) != null)
            {
                JsonObject singleDeviceData = new JsonObject(line);

                if (categoryTypeCheck)
                {
                    batchResult.add(singleDeviceData);
                }
                else
                {
                    batchResult.add(singleDeviceData);

                    return;
                }
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }
    }

    public static final Predicate<JsonArray> isValidResult = result -> result != null && !result.isEmpty();

    public static JsonArray getBatchedData(JsonArray context)
    {
        var batch = new JsonArray();

        int maxBatchSize = Math.min(2, context.size());

        for (int index = 0; index < maxBatchSize; index++)
        {
            batch.add(context.getJsonObject(0));

            context.remove(0);
        }

        return batch;
    }

    public static void updateCredentialContext(JsonArray context, Map<Integer,String> credentialContext)
    {
        try
        {
            for(int index=0;index<context.size();index++)
            {
                var deviceContext = context.getJsonObject(index);

                credentialContext.put(deviceContext.getInteger(Constants.ID),
                        buildString(deviceContext.getString(Constants.IP_ADDRESS),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.PORT),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.SSH_USERNAME),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.SSH_PASSWORD)));
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(), exception.getStackTrace());
        }
    }

    public static JsonArray getMetricPollingContext(Map<Integer,String> credentialContext)
    {
        var result = new JsonArray();

        for (Map.Entry<Integer, String> entry : credentialContext.entrySet())
        {
            var value = entry.getValue().split(Constants.VALUE_SEPARATOR_WITH_ESCAPE);

            result.add(new JsonObject().put(Constants.ID,entry.getKey()).put(Constants.IP_ADDRESS,value[0])
                    .put(Constants.PORT,value[1]).put(Constants.SSH_USERNAME,value[2])
                    .put(Constants.SSH_PASSWORD,decrypt(value[3])));
        }

        return result;
    }

}
