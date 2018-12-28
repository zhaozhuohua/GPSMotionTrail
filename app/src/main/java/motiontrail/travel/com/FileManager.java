package motiontrail.travel.com;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * Created by ZhaoYangyang on 2017/3/29.
 * <p>
 * 存储空间管理
 */
public class FileManager {
    private static final int BUFF_SIZE = 1024 * 1024; // 1M Byte


    /**
     * 判断存储空间是否大于200MB
     *
     * @return
     */
    public static boolean isCanSave() {
        return isCanSave(200);
    }

    /**
     * 判断存储空间是否大于minDiskSize
     *
     * @param minDiskSize 最小可用磁盘打印小
     * @return
     */
    public static boolean isCanSave(int minDiskSize) {
        try {
            long size = FileUtil.getDiskAvailableSize();
            return size / 1024 / 1024 > minDiskSize;
        } catch (Exception e) {
            LogManager.newInstance().printAndSaveLog("获取存储空间失败！error msg: " + e.toString());
        }

        return false;
    }

    /**
     * 可用存储容量是否大于 20%
     */
    public static boolean getStoragePercentage() {
        return getStoragePercentage(20);
    }

    /**
     * 可用存储容量是否大于设置值
     *
     * @param percentage 设置的百分比
     * @return
     */
    public static boolean getStoragePercentage(int percentage) {
        long availableSize = 0;
        long storageSize = 0;
        try {
            availableSize = FileUtil.getDiskAvailableSize();
            storageSize = getStorageSize();
        } catch (Exception e) {
            LogManager.newInstance().printAndSaveLog("获取存储空间失败！error msg: " + e.toString());
        }
        if (availableSize != 0 && storageSize != 0) {
            return (availableSize / 1024 / 1024 * 1.0 / storageSize * 100 > percentage);
        }

        return false;
    }

    /**
     * 存储已用空间  单位MB
     *
     * @return
     */
    public static long getStorageBlockSize() {
        if (!FileUtil.existsSdcard()) return 0;
        File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs sf = new StatFs(path.getAbsolutePath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();

        return blockSize * blockCount / 1024 / 1024;
    }

    /**
     * 存储空间总大小  单位 MB
     *
     * @return
     */
    public static long getStorageSize() {
        if (!FileUtil.existsSdcard()) return 0;
        File path = Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        StatFs sf = new StatFs(path.getAbsolutePath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();

        return blockSize * blockCount / 1024 / 1024;
    }

    /**
     * 创建文件夹
     *
     * @param dirPath
     * @return
     */
    public static boolean mkdirs(String dirPath) {
        try {
            File dir = new File(dirPath);
            if (!dir.isDirectory()) {
                return dir.mkdirs();
            }
            return true;
        } catch (NullPointerException e) {
            LogManager.newInstance().printAndSaveLog("创建文件夹失败。error msg: " + e.getMessage() + ", file path: " + dirPath);
        }

        return false;
    }

    /**
     * 创建文件
     *
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.isFile()) {
                if (mkdirs(file.getParent())) {
                    try {
                        return file.createNewFile();
                    } catch (IOException e) {
                        LogManager.newInstance().printAndSaveLog("创建文件失败。error msg: " + e.getMessage() + ", file path: " + filePath);
                    }
                }
            } else {
                return true;
            }
        } catch (NullPointerException e) {
            LogManager.newInstance().printAndSaveLog("创建文件失败。error msg: " + e.getMessage() + ", file path: " + filePath);
        }

        return false;
    }

    /**
     * 删除文件
     *
     * @param files
     */
    public static boolean deleteFiles(LinkedList<File> files) {
        for (File file : files) {
            if (file.isFile() && file != null) {
                boolean isDel = file.delete();
                if (!isDel) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 创建文件夹
     *
     * @param filePath
     * @return
     */
    public static boolean makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists() || file.isFile()) {
                return file.mkdir();
            }
            return true;
        } catch (Exception e) {
            LogManager.newInstance().printAndSaveLog("创建文件夹失败。error msg: " + e.getMessage() + ", file path: " + filePath);
        }
        return false;
    }

    /**
     * 创建文件
     *
     * @param filePath
     * @param fileName
     * @return
     */
    public static File getFilePath(String filePath, String fileName) {
        File file = null;
        boolean isMake = makeRootDirectory(filePath);
        if (isMake) {
            try {
                file = new File(filePath + "/" + fileName);
            } catch (Exception e) {
                LogManager.newInstance().printAndSaveLog("创建文件失败。error msg: " + e.getMessage());
            }
        } else {
            LogManager.newInstance().printAndSaveLog("创建文件失败。file path: " + filePath);
        }
        return file;
    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }

    /**
     * 获取指定文件大小
     *
     * @param file
     * @return
     */
    public static long getFileSize(File file) {
        try {
            long size = 0;
            if (file != null && file.exists()) {
                FileInputStream fis = null;
                fis = new FileInputStream(file);
                size = fis.available();
            } else {
                LogManager.newInstance().printAndSaveLog("获取文件大小", "文件不存在! file: " + file);
            }
            return size;
        } catch (Exception e) {
            LogManager.newInstance().printAndSaveLog("获取文件大小失败。error msg: " + e.getMessage() + ", file: " + file);
            return 0;
        }
    }
}
