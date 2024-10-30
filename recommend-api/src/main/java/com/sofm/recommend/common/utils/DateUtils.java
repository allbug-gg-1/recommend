package com.sofm.recommend.common.utils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    private static final String defaultFormat = "yyyy-MM-dd";

    public static String convertTimestampToDate(long timestamp) {
        // 将时间戳转换为 Instant 对象
        Instant instant = Instant.ofEpochMilli(timestamp);
        // 转换为 LocalDate（将时区设定为系统默认时区）
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        // 定义日期格式化器，格式为 yyyyMMdd
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        // 将 LocalDate 格式化为字符串
        return date.format(formatter);
    }

    /**
     * @param date
     * @return
     */
    public static String formatDate(Date date, String format) {
        if (StringUtils.isEmpty(format)) {
            format = defaultFormat;
        }
        return new SimpleDateFormat(format).format(date);
    }

    /**
     * 判断是否在上半个月。
     *
     * @return 如果日期在上半个月，返回 true；否则返回 false
     */
    public static boolean isFirstHalfOfMonth() {
        LocalDate date = LocalDate.now();
        int dayOfMonth = date.getDayOfMonth();
        return dayOfMonth <= 15;
    }


    public static int getMonth() {
        LocalDate date = LocalDate.now();
        return date.getMonth().getValue();
    }
}
