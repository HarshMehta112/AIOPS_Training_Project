package com.org.motadata.utils;

import java.util.stream.Stream;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 10/30/24 1:19 PM
 */
public class CommonUtil
{
    private CommonUtil() {}

    public static String buildString(String ... variableStrings)
    {
        var stringBuilder = new StringBuilder();

        Stream.of(variableStrings).forEach(stringBuilder::append); // Append each string to the StringBuilder

        return stringBuilder.toString(); // Convert StringBuilder to String
    }
}
