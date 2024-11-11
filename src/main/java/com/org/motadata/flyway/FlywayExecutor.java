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
        var promise = Promise.<Boolean>promise();

        try
        {
            var flyway = Flyway.configure()
                    .dataSource(CommonUtil.buildString("jdbc:postgresql://"
                                    ,ConfigLoaderUtil.getConfigs().getJsonObject(Constants.DB_CONFIG).getString(Constants.HOST),":",
                                    ConfigLoaderUtil.getConfigs().getJsonObject(Constants.DB_CONFIG).getString(Constants.PORT)
                                    ,"/",ConfigLoaderUtil.getConfigs().getJsonObject(Constants.DB_CONFIG).getString(Constants.DATABASE)),
                            ConfigLoaderUtil.getConfigs().getJsonObject(Constants.DB_CONFIG).getString(Constants.USER_NAME),
                            ConfigLoaderUtil.getConfigs().getJsonObject(Constants.DB_CONFIG).getString(Constants.PASSWORD))
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
