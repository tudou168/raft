package com.tongbanjie.raft.core.snapshot;

import com.tongbanjie.raft.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/***
 * 快照
 * @author banxia
 * @date 2017-12-08 14:14:39
 */
public class Snapshot implements Serializable {


    private final static Logger log = LoggerFactory.getLogger(Snapshot.class);

    private String snapshotDir;

    private String snapshotDataDir;

    private SnapshotMetaData snapshotMetaData;

    private SnapshotMetaDataCodec codec = new SnapshotMetaDataCodec();


    private AtomicBoolean working = new AtomicBoolean(false);


    public Snapshot(String raftDir) {

        this.snapshotDir = raftDir + File.separator + "snapshot";
        this.snapshotDataDir = this.snapshotDir + File.separator + "data";
        File fileDir = new File(this.snapshotDataDir);

        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }

        //  加载快照元数据
        this.reload();


    }


    /**
     * 加载快照元数据
     */
    public void reload() {
        // 加载快照元数据
        this.snapshotMetaData = this.readSnapshotMetaData();
        if (snapshotMetaData == null) {
            snapshotMetaData = new SnapshotMetaData();
        }
    }

    /**
     * 加载快照元数据
     * 保存的在 snapshot/下的metadata
     */
    private SnapshotMetaData readSnapshotMetaData() {

        String fileName = this.snapshotDataDir + File.separator + "metadata";
        File file = new File(fileName);

        try {

            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            return this.codec.decode(randomAccessFile);

        } catch (IOException ex) {
            return null;
        }

    }


    /**
     * 打开所有的快照文件
     *
     * @return
     */
    public TreeMap<String, SnapshotFile> openSnapshotFileList() {


        TreeMap<String, SnapshotFile> snapshotFileTreeMap = new TreeMap<String, SnapshotFile>();


        try {


            List<String> fileNameList = FileUtil.getSortedFileNameList(this.snapshotDataDir);

            for (String fileName : fileNameList) {

                RandomAccessFile randomAccessFile = FileUtil.openFile(this.snapshotDataDir, fileName, "r");
                SnapshotFile snapshotFile = new SnapshotFile(fileName, randomAccessFile);
                snapshotFileTreeMap.put(fileName, snapshotFile);
            }
        } catch (Exception e) {

            throw new RuntimeException("openSnapshotFileList fail", e);
        }
        return snapshotFileTreeMap;

    }


    /**
     * 关闭所有的快照文件
     *
     * @param snapshotFileTreeMap
     */
    public void closeSnapshotFileList(TreeMap<String, SnapshotFile> snapshotFileTreeMap) {


        for (Map.Entry<String, SnapshotFile> entry : snapshotFileTreeMap.entrySet()) {

            FileUtil.closeFile(entry.getValue().getRandomAccessFile());
        }

    }


    /**
     * 更新快照元数据
     *
     * @param dir              快照元数据所在的目录
     * @param snapshotMetaData 快照元数据
     */
    public void updateSnapshotMetaData(String dir, SnapshotMetaData snapshotMetaData) {

        String snapshotMetaDataFile = dir + File.separator + "metadata";
        RandomAccessFile randomAccessFile = null;
        try {

            File fileDir = new File(dir);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            File file = new File(snapshotMetaDataFile);
            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();
            randomAccessFile = new RandomAccessFile(snapshotMetaDataFile, "rw");
            this.codec.encode(randomAccessFile, snapshotMetaData);
        } catch (IOException e) {
            log.warn("updateMetaData fail", e);
        } finally {

            FileUtil.closeFile(randomAccessFile);
        }

    }

    public SnapshotMetaData getSnapshotMetaData() {
        return snapshotMetaData;
    }

    public static void main(String[] args) {

        Snapshot snapshot = new Snapshot("./log");
        System.out.println(snapshot.snapshotMetaData);
    }

    public AtomicBoolean getWorking() {
        return working;
    }

    public String getSnapshotDir() {
        return snapshotDir;
    }

    public String getSnapshotDataDir() {
        return snapshotDataDir;
    }
}
