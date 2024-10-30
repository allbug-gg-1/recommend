/*
 * @(#)StringUtils.java 创建于 2019-05-08 22:56:16
 *
 * 版权：版权所有 Typhoon 保留所有权力。
 */
package com.sofm.recommend.common.utils;

import java.nio.charset.Charset;

/**
 * @author C.
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 指定的字符串是否全部为空，如果参数为空，或者未给定也视为空。
     *
     * @param strings
     * @return
     */
    public static boolean isEmpty(String... strings) {
        if (strings == null) {
            return true;
        }
        for (String s : strings) {
            if (StringUtils.isNotEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断指定的字符串是否全部非空。
     *
     * @param strings
     * @return
     */
    public static boolean isNotEmpty(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        for (String s : strings) {
            if (StringUtils.isEmpty(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将一个字符串变成首字母大写。
     *
     * @param str
     * @return
     */
    public static String startWithUpper(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        char[] ch = str.toCharArray();
        ch[0] = Character.toUpperCase(ch[0]);
        return new String(ch);
    }

    /**
     * 将一个字符串变成首字母小写。
     *
     * @param str
     * @return
     */
    public static String startWithLower(String str) {
        if (str == null || str.trim().isEmpty()) {
            return str;
        }
        char[] ch = str.toCharArray();
        ch[0] = Character.toLowerCase(ch[0]);
        return new String(ch);
    }

    /**
     * 将byte数组转换成16进制字符串形式。
     *
     * @param bytes
     * @return
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        for (byte b : bytes) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (toByte(hexChars[pos]) << 4 | toByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 获取字符串指定位置上的字符，index可以为负数，负数表示从结尾向前数。
     *
     * @param s
     * @param index
     * @return
     */
    public static char charAt(String s, int index) {
        if (index < 0) {
            index += s.length();
        }
        return s.charAt(index);
    }
}
