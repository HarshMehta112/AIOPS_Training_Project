package com.org.motadata.services;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/4/24 10:21 AM
 */
public class PollingTriggerServices extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(PollingTriggerServices.class);

    @Override
    public void start()
    {
        var scheduleTime = new HashMap<String,Long>();

        scheduleTime.put(Constants.AVAILIBILITY_POLLING_TIME, ConfigurationService.getAvailibilityPollTime());

        scheduleTime.put(Constants.METRIC_POLLING_TIME,ConfigurationService.getMetricPollTime());

        var updatedScheduleTime = new HashMap<>(scheduleTime);

        Bootstrap.getVertx().setPeriodic(6*1000, handler->
        {
            try
            {
                for(Map.Entry<String,Long> entry: updatedScheduleTime.entrySet())
                {
                    var time = entry.getValue();

                    time = time - 60000;

                    if(time<=0)
                    {
                        LOGGER.info("Polling Started .... "+entry.getKey());

                        // add case statement if another polling group added.
                        Bootstrap.getVertx().eventBus().send(Constants.POLLING_REQUESTS,
                                entry.getKey().equals(Constants.METRIC_POLLING_TIME) ?
                                        Constants.METRIC_POLLING_TIME :
                                        Constants.AVAILIBILITY_POLLING_TIME);

                        updatedScheduleTime.put(entry.getKey(),scheduleTime.get(entry.getKey()));
                    }
                    else
                    {
                        updatedScheduleTime.put(entry.getKey(),time);
                    }
                }
            }
            catch (Exception exception)
            {
               LOGGER.error(exception.getMessage(),exception.getStackTrace());
            }
        });
    }
}
