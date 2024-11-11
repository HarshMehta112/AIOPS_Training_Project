package com.org.motadata.utils;

import com.org.motadata.constant.Constants;
import com.org.motadata.engines.MetricPollingEngine;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:19 PM
 */
public class CommonUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(CommonUtil.class);

    public static String buildString(String ... variableStrings)
    {
        var stringBuilder = new StringBuilder();

        Stream.of(variableStrings).forEach(stringBuilder::append); // Append each string to the StringBuilder

        return stringBuilder.toString(); // Convert StringBuilder to String
    }


    public static final Predicate<Object> isNonNull = Objects::nonNull;

    public static JsonArray getBatchedData(JsonArray context, int batchSize)
    {
        var batch = new JsonArray();

        var maxBatchSize = Math.min(batchSize, context.size());

        for (var index = 0; index < maxBatchSize; index++)
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
            for(var index=0;index<context.size();index++)
            {
                var deviceContext = context.getJsonObject(index);

                credentialContext.put(deviceContext.getInteger(Constants.ID),
                        buildString(deviceContext.getString(Constants.IP_ADDRESS),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.PORT),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.SSH_USERNAME),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.SSH_PASSWORD),Constants.VALUE_SEPARATOR,
                                deviceContext.getString(Constants.CREDENTIAL_ID)));
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

        for (var entry : credentialContext.entrySet())
        {
            var value = entry.getValue().split(Constants.VALUE_SEPARATOR_WITH_ESCAPE);

            result.add(new JsonObject().put(Constants.ID,entry.getKey()).put(Constants.IP_ADDRESS,value[0])
                    .put(Constants.PORT,value[1]).put(Constants.SSH_USERNAME,value[2])
                    .put(Constants.SSH_PASSWORD,CipherUtil.decrypt(value[3])));
        }

        return result;
    }

    public static void updateInMemoryCredentials(String credentialId)
    {
        for (var entry : MetricPollingEngine.getCredentialContext().entrySet())
        {
            if(entry.getValue().split(Constants.VALUE_SEPARATOR_WITH_ESCAPE)[4].equals(credentialId))
            {
                MetricPollingEngine.getCredentialContext().remove(entry.getKey());
            }
        }
    }

}
