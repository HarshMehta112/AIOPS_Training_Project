package com.org.motadata.utils;

import com.org.motadata.constant.Constants;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Description:
 * Author: Harsh Mehta
 * Date: 11/6/24 3:22 PM
 */
public class CipherUtil
{
    private static final LoggerUtil LOGGER = new LoggerUtil(CipherUtil.class);

    public static String encrypt(String data)
    {
        try
        {
            var secretKey = new SecretKeySpec(Constants.ENCRYPTION_KEY.getBytes(), Constants.ENCRYPTION_ALGORITHM);

            var cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            var encryptedData = cipher.doFinal(data.getBytes());

            return Base64.getEncoder().encodeToString(encryptedData);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return null;
    }

    public static String decrypt(String encryptedData)
    {
        try
        {
            var secretKey = new SecretKeySpec(Constants.ENCRYPTION_KEY.getBytes(), Constants.ENCRYPTION_ALGORITHM);

            var cipher = Cipher.getInstance(Constants.ENCRYPTION_ALGORITHM);

            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            var decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

            return new String(decryptedData);
        }
        catch (Exception exception)
        {
            LOGGER.error(exception.getMessage(),exception.getStackTrace());
        }

        return null;
    }
}
