package com.org.motadata.utils;

import com.org.motadata.constant.Constants;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/6/24 3:20 PM
 */
public class PluginExecutorUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(PluginExecutorUtil.class);

    public static JsonArray executePlugin(JsonArray deviceContext)
    {
        var batchResult = new JsonArray();

        try
        {
            var dataEncoder = Base64.getEncoder().encodeToString(deviceContext.toString()
                    .getBytes(StandardCharsets.UTF_8));

            LOGGER.info(dataEncoder);

            var categoryTypeCheck = deviceContext.getJsonObject(0)
                    .getString(Constants.PLUGIN_CALL_CATEGORY)
                    .equals(Constants.POLLING);

            var process = startProcess(dataEncoder);

            if (process == null) return batchResult;

            try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                processOutput(reader, batchResult, categoryTypeCheck);
            }
            finally
            {
                process.destroy();
            }
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        LOGGER.info("Go collect result "+batchResult.toString());

        return batchResult;
    }

    private static Process startProcess(String dataEncoder)
    {
        Process process = null;

        try
        {
            var processBuilder = new ProcessBuilder(Constants.PLUGIN_PATH, dataEncoder);

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
            while (CommonUtil.isNonNull.test((line = reader.readLine())))
            {
                var singleDeviceData = new JsonObject(line);

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

}
