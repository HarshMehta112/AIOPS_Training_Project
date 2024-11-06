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
            while (CommonUtil.isNonNull.test((line = reader.readLine())))
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

}
