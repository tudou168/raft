package com.tongbanjie.raft.core.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/***
 * RandomAccessFile 工具类
 * @author banxia
 * @date 2017-12-08 16:16:05
 */
public class FileUtil {


    /**
     * open a random access file
     *
     * @param path     路径
     * @param fileName 文件名称
     * @param mode     模式
     * @return
     */
    public static RandomAccessFile openFile(String path, String fileName, String mode) {


        try {

            String fullFileName = path + File.separator + fileName;
            return new RandomAccessFile(fullFileName, mode);
        } catch (Exception e) {

            throw new RuntimeException("open fail err:", e);
        }
    }


    /**
     * 获取对应路径下有序的文件名列表
     *
     * @param path
     * @return
     */
    public static List<String> getSortedFileNameList(String path) {

        List<String> fileNameList = new ArrayList<String>();

        File fileDir = new File(path);
        if (!fileDir.exists()) {
            return fileNameList;
        }

        File[] files = fileDir.listFiles();
        if (files == null || files.length == 0) {
            return fileNameList;
        }


        for (File file : files) {

            fileNameList.add(file.getName());
        }


        Collections.sort(fileNameList);

        return fileNameList;

    }


    /**
     * 关闭文件
     *
     * @param randomAccessFile
     */
    public static void closeFile(RandomAccessFile randomAccessFile) {


        try {

            if (randomAccessFile == null) return;

            randomAccessFile.close();

        } catch (Exception e) {


        }
    }
}
