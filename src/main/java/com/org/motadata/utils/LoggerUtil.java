package com.org.motadata.utils;

import java.util.Arrays;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 4:54 PM
 */
public class LoggerUtil {
    private final Logger logger;

    private final String className;
    private static Level currentLogLevel = Level.INFO;
    private static boolean debugEnabled = false;

    // Constructor to create a logger for the specific class
    public LoggerUtil(Class<?> clazz)
    {
        this.logger = Logger.getLogger(clazz.getName());

        this.className = CommonUtil.buildString("[",clazz.getName(),"] ");
    }


    // Method to log messages at the specified level
    public void log(Level level, String message) {
        if (level.intValue() >= currentLogLevel.intValue()) {
            logger.log(level, message);
        }
        if (debugEnabled && level == Level.FINE) {
            logger.log(Level.FINE, message); // Log debug messages if enabled
        }
    }

    // Convenience methods for common log levels
    public void info(String message)
    {
        log(Level.INFO, CommonUtil.buildString(this.className,message));
    }

    public void debug(String message) {
        log(Level.FINE, CommonUtil.buildString(this.className,message));
    }

    public void warn(String message) {
        log(Level.WARNING, CommonUtil.buildString(this.className,message));
    }

    public void error(String message, StackTraceElement[] stackTraceElements) {
        log(Level.SEVERE, CommonUtil.buildString(this.className,getStackTrace(message, stackTraceElements)));
    }

    private String getStackTrace(String message, StackTraceElement[] traceElements) {
        var stackTrace = new StringBuilder();
        stackTrace.append(message);
        Arrays.stream(traceElements)
                .forEach(stackTraceElement ->
                        stackTrace.append("\tat ")
                                .append(stackTraceElement).append(System.lineSeparator()));
        return stackTrace.toString();
    }
}
