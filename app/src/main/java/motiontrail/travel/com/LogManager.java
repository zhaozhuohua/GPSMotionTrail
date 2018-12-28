package motiontrail.travel.com;

import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 赵阳阳 on 2017/3/29.
 *
 * 管理日志打、保存和上传
 */
public class LogManager {

    public static final String LOG_DIR = "log";
    public static final String LOG_TAG = "log_tag";
    public static final String MOTION_TRAIL_DIR_PATH = Environment.getExternalStorageDirectory() + "/motionTrail";

    //Log信息缓存文件夹
    public static final String LOG_DIR_PATH = MOTION_TRAIL_DIR_PATH + "/" + LOG_DIR;

    public static final String MSG_STORAGE_ERROR_1 = "[10501]";  //SD卡空间小于200MB, 不再保存日志到本地。

    private String mLogInfoFilePath = null;
    private String mLogFileName = null;
    private String mData = null; //当前日期

    private static LogManager sInstance = null;

    /**
     * 保存日志信息线程管理
     */
    private ExecutorService mSaveLogManager = null;

    public static LogManager newInstance() {
        if (sInstance == null) {
            synchronized (LogManager.class) {
                if (sInstance == null) {
                    sInstance = new LogManager();
                }
            }
        }
        return sInstance;
    }

    private LogManager(){
        mSaveLogManager = Executors.newSingleThreadExecutor();
    }

    /**
     * 关闭log管理线程服务
     */
    public void close() {
        mSaveLogManager.shutdown();
        sInstance = null;
    }

    /**
     * 初始化保存日志文件信息
     */
    public void init() {
        if (TextUtils.isEmpty(mLogInfoFilePath)) {
            mData = TimeUtils.timeStamp2Data(System.currentTimeMillis(), TimeUtils.YYYY_MM_DD);
            mLogFileName = mData + ".txt";
            mLogInfoFilePath = LOG_DIR_PATH + "/" + mLogFileName;
        }
    }

    /**
     * 打印和保存日志信息
     * @param logInfo  日志
     */
    public void printAndSaveLog(String logInfo) {
        printAndSaveLog(LOG_TAG, logInfo);
    }

    /**
     * 打印和保存日志信息
     * @param logTag  日志TAG
     * @param logInfo  日志
     */
    public void printAndSaveLog(String logTag, String logInfo) {
        init();
        saveLogInfo(logInfo);
    }

    /**
     * 执行保存日志信息操作
     * @param logInfo
     */
    private void saveLogInfo(String logInfo) {
        if (mSaveLogManager != null && !mSaveLogManager.isShutdown()) {
            mSaveLogManager.execute(new SaveLogRunnable(logInfo, mLogInfoFilePath));
        }
    }

    /**
     * 日志信息保存线程
     */
    class SaveLogRunnable implements Runnable {

        private String mLogInfo = null;  //日志信息
        private String mLogInfoFilePath = null;  //日志文件路径

        public SaveLogRunnable(String logInfo, String logInfoFilePath) {
            mLogInfo = logInfo;
            mLogInfoFilePath = logInfoFilePath;
        }

        @Override
        public void run() {
            try {
                saveLogInfo(mLogInfo);
            } catch (IOException e) {
            }
        }

        /**
         * 保存日志信息
         * @param logInfo  日志
         * @throws java.io.IOException
         */
        private void saveLogInfo(String logInfo) throws IOException {
            if (FileManager.isCanSave()) {
                if (FileManager.createFile(mLogInfoFilePath)) {
                    writeLogInfo(logInfo, mLogInfoFilePath);
                } else {
                }
            } else {
                if (isContainString(mLogInfoFilePath, MSG_STORAGE_ERROR_1)) {
                    writeLogInfo(MSG_STORAGE_ERROR_1, mLogInfoFilePath);
                }
            }
        }
    }

    /**
     * 写入日志信息
     * @param logInfo  日志
     * @param filePath  文件路径
     * @throws java.io.IOException
     */
    private void writeLogInfo(String logInfo, String filePath) throws IOException {
        /**
         * model各个参数详解
         * r 代表以只读方式打开指定文件
         * rw 以读写方式打开指定文件
         * rws 读写方式打开，并对内容或元数据都同步写入底层存储设备
         * rwd 读写方式打开，对文件内容的更新同步更新至底层存储设备
         **/
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

        //将记录指针移动到文件最后
        raf.seek(raf.length());
        raf.write((logInfo + " \r\n").getBytes(Charset.forName("UTF-8")));
        raf.close();
    }

    /**
     * 文件是否包含此字符串。默认编码UTF-8
     * @param filePath  文件路径
     * @param text  字符串信息
     * @return
     */
    public boolean isContainString(String filePath, String text) {
        return isContainString(filePath, text, Charset.forName("UTF-8").displayName());
    }

    /**
     * 文件是否包含此字符串
     * @param filePath  文件路径
     * @param text  字符串信息
     * @param encode  编码格式
     * @return
     */
    public boolean isContainString(String filePath, String text, String encode) {
        File file;

        //获取文件数据流
        FileInputStream fis;
        try {
            file = new File(filePath);
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return false;
        } catch (NullPointerException e) {
            LogManager.newInstance().printAndSaveLog("文件路径异常 file path：" + filePath + ", error msg: " + e.toString());
            return false;
        }

        //生成对应编码的文件流
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(fis, encode);
        } catch (UnsupportedEncodingException e1) {
            return false;
        }

        //判断是否包含此字符串
        BufferedReader reader;
        try {
            reader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.indexOf(text) > -1) {
                    return true;
                }
            }
        } catch (IOException e) {
        }

        return false;
    }

    /**
     * 打印厨打单对应日志信息
     * @param log
     */
    public void printKitchenLog(String log) {
        // debug版本，灰度版本或内测环境,不关闭Log,方便开发与测试.
        if (BuildConfig.DEBUG || BuildConfig.FLAVOR.toLowerCase().contains("dev")
                || BuildConfig.FLAVOR.toLowerCase().contains("test") || BuildConfig.FLAVOR.toLowerCase().contains("ptest")
                || BuildConfig.FLAVOR.toLowerCase().contains("gray")) {
            printAndSaveLog("print_order_info", log);
        }
    }
}
