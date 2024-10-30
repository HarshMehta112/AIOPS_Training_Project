package com.org.motadata.utils;

import java.nio.file.FileSystems;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/29/24 3:39 PM
 */
public class Constants
{
    public static final String USER_NAME = "username";

    public static final String PASSWORD = "password";

    public static final String JWT_TOKEN_ALGORITHM = "RS512";

    public static final String CURRENT_DIR = System.getProperty("user.dir");

    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    public static final String PROPERTIES_FILE = "config.properties";

    public static final String VALUE_SEPARATOR = "_|@#|_";

    public static final String VALUE_SEPARATOR_WITH_ESCAPE = "_\\|@#\\|_";

    public static final int HTTP_UNAUTHORIZED_STATUS_CODE = 401;

    public static final String CONTENT_TYPE = "Content-Type";

    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static final String ACCESS_TOKEN = "accessToken";

    public static final String REFRESH_TOKEN = "refreshToken";

    public static final String DEBUG_LOG_FLAG = "debugEnabled";

    public static final String NEW_LINE_SEPARATOR = "\n";

    public static final String SSL_KEYSTORE_PATH = "sslKeyStorePath";

    public static final String SSL_KEYSTORE_PASSWORD = "sslKeyStorePassword";



}
