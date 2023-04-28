package io.gushizhao.rpc.common.utils;

import java.util.stream.IntStream;

/**
 * @Author huzhichao
 * @Description TODO
 * @Date 2023/4/23 15:34
 */
public class SerializationUtils {
    public static final String PADDING_STRING = "0";

    public static final int MAX_SERIALIZATION_TYPE_COUNT = 16;

    /**
     * 为长度不足 16 的字符串后面补 0
     * @param str
     * @return
     */
    public static String paddingString(String str) {
        str = transNullToEmpty(str);
        if (str.length() >= MAX_SERIALIZATION_TYPE_COUNT) {
            return str;
        }
        int paddingCount = MAX_SERIALIZATION_TYPE_COUNT - str.length();
        StringBuilder paddingString = new StringBuilder(str);
        IntStream.range(0, paddingCount).forEach(i -> {
            paddingString.append(PADDING_STRING);
        });
        return paddingString.toString();
    }

    /**
     * 字符串去零 操作
     * @param str
     * @return
     */
    public static String subString(String str) {
        str = transNullToEmpty(str);
        return str.replace(PADDING_STRING, "");
    }


    public static String transNullToEmpty(String str) {
        return str == null ? "" : str;
    }
}
