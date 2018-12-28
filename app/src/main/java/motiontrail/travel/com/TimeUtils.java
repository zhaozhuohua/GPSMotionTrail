package motiontrail.travel.com;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by 赵阳阳 on 2016/10/8.
 *
 * 时间日期格式转换类
 */
public class TimeUtils {

    public static final String HH_MM = "HH:mm";
    public static final String HH_MM_SS = "HH:mm:ss";
    public static final String HH_MM_SS_SSS = "HH:mm:ss:SSS";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String MMDD = "MMdd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String HH = "HH";

    /**
     * 时间格式缓存
     */
    private static List<ThreadLocal<SimpleDateFormat>> sLocalList;

    private static Object sLocalListChangeLock = new Object();  //时间格式缓存变动锁

    /**
     * 获取对应日期格式类
     * @param format
     * @return
     */
    private static DateFormat getDateFormat(String format) {
        synchronized (sLocalListChangeLock) {
            if (sLocalList == null || sLocalList.isEmpty()) {
                sLocalList = new ArrayList<>();
                return addDateFormat(format);
            } else {
                for (int i = 0; i < sLocalList.size(); i++) {
                    ThreadLocal<SimpleDateFormat> tl = sLocalList.get(i);
                    if (tl == null || tl.get() == null || tl.get().toPattern() == null) {
                        sLocalList.remove(i);
                    } else {
                        if (tl.get().toPattern().equals(format)) {
                            return tl.get();
                        }
                    }
                }

                return addDateFormat(format);
            }
        }
    }

    /**
     * 添加日期格式
     * @param format
     * @return
     */
    private static SimpleDateFormat addDateFormat(String format) {
        synchronized (sLocalListChangeLock) {
            SimpleDateFormat df = new SimpleDateFormat(format);
            //SimpleDateFormat不是线程安全的，不进行无限创建
            ThreadLocal<SimpleDateFormat> tl = new ThreadLocal<>();
            tl.set(df);
            if (sLocalList == null) {
                sLocalList = new ArrayList<>();
            }
            sLocalList.add(tl);
            return df;
        }
    }

    /**
     * 获取对应时间格式字符串
     * @param timeStamp  13位时间戳
     * @return
     */
    public static String timeStamp2Data(String timeStamp) {
        return timeStamp2Data(Long.valueOf(timeStamp));
    }

    /**
     * 获取对应时间格式字符串
     * @param timeStamp  13位时间戳
     * @return
     */
    public static String timeStamp2Data(long timeStamp) {
        return getDateFormat(YYYY_MM_DD_HH_MM_SS).format(new Date(timeStamp));
    }

    /**
     * 根据时间格式获取时间戳
     * @param timeStamp 13位时间戳
     * @param format  日期格式
     * @return
     */
    public static String timeStamp2Data(long timeStamp, String format) {
        return getDateFormat(format).format(new Date(timeStamp));
    }

    /**
     * 日期转时间戳
     * @param d  日期
     * @param format  日期格式
     * @return
     */
    public static long data2timestamp(String d, String format) {
        Date date = null;
        try {
            date = getDateFormat(format).parse(d);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * 清空所有时间格式缓存
     */
    public static void removeAllDateFormat() {
        synchronized (sLocalListChangeLock) {
            if (sLocalList != null && !sLocalList.isEmpty()) {
                sLocalList.clear();
            }
            sLocalList = null;
        }
    }

    /**
     * 初始化时间格式缓存
     */
    public static void initAllDateFormat() {
        synchronized (sLocalListChangeLock) {
            sLocalList = new ArrayList<>();
        }
    }

    /**
     * 得到几天前零点零分零秒的时间戳
     * @param day 几天前
     * @return
     */
    public static long getDaysAgoTimestamp(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -day);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    /**
     * 秒数 转为时间格式 added by ZhangYige at 2018/3/5 10:44
     * @param time
     * @return
     */
    public static String secToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
