package com.org.motadata.service;

import com.org.motadata.Bootstrap;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.constant.Constants;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.AbstractVerticle;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/4/24 10:21 AM
 */
public class PollingTrigger extends AbstractVerticle
{
    private static final LoggerUtil LOGGER = new LoggerUtil(PollingTrigger.class);

    @Override
    public void start()
    {
        var scheduleTime = new HashMap<String,Long>();

        scheduleTime.put(Constants.AVAILIBILITY_POLLING_TIME, ConfigLoaderUtil.getAvailibilityPollTime());

        scheduleTime.put(Constants.METRIC_POLLING_TIME, ConfigLoaderUtil.getMetricPollTime());

        var updatedScheduleTime = new HashMap<>(scheduleTime);

        Bootstrap.getVertx().setPeriodic(6000, handler->
        {
            try
            {
                for(Map.Entry<String,Long> entry: updatedScheduleTime.entrySet())
                {
                    var time = entry.getValue();

                    time = time - 6000;

                    if(time<=0)
                    {
                        LOGGER.info("Polling request triggered for .... "+entry.getKey());

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
