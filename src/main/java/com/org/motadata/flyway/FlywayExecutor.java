package com.org.motadata.flyway;

import com.org.motadata.constant.Constants;
import com.org.motadata.utils.CommonUtil;
import com.org.motadata.utils.ConfigLoaderUtil;
import com.org.motadata.utils.LoggerUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.flywaydb.core.Flyway;


/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/6/24 12:11 PM
 */
public class FlywayExecutor
{
    private static final LoggerUtil LOGGER = new LoggerUtil(FlywayExecutor.class);

    public static Future<Boolean> executeDbMigration()
    {
        Promise<Boolean> promise = Promise.promise();

        try
        {
            var flyway = Flyway.configure()
                    .dataSource(CommonUtil.buildString("jdbc:postgresql://",ConfigLoaderUtil.getDbHost(),":",
                                    String.valueOf(ConfigLoaderUtil.getDbPort()),"/",ConfigLoaderUtil.getDbName()),
                            ConfigLoaderUtil.getDbUsername(), ConfigLoaderUtil.getDbPassword())
                    .locations("filesystem:"+ Constants.RESOURCES_PATH +"/db.migration/")
                    .schemas("public")
                    .load();

            //create schema history table if deleted.
            flyway.baseline();

            // Migrate the database
            flyway.migrate();

            LOGGER.info("Flyway migrations applied successfully!");

            promise.complete(true);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());

            promise.fail(exception);
        }

        return promise.future();
    }
}
