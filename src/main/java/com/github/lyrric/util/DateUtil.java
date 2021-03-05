package com.github.lyrric.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static final String yyyy_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";

    public static Date parseDate(String inputDate, String pattern) {
        SimpleDateFormat df = new SimpleDateFormat();
        try {
            df.applyPattern(pattern);
            // 设置解析日期格式是否严格解析日期
            df.setLenient(false);
            ParsePosition pos = new ParsePosition(0);
            Date date = df.parse(inputDate, pos);
            if (date != null) {
                return date;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
