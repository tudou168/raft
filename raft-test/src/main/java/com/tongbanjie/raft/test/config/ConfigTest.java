package com.tongbanjie.raft.test.config;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/***
 *
 * @author banxia
 * @date 2017-12-05 19:19:25
 */
public class ConfigTest {

    @Test
    public void test() throws FileNotFoundException {

        File file = new File("/Users/banxia/Desktop/wp/code/git/raft/conf/raft.cfg");
        Properties cfg = new Properties();
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            cfg.load(fileInputStream);
            String serverId = cfg.getProperty("serverId");
            String clientPort = cfg.getProperty("clientPort");
            System.err.println(serverId);
            System.err.println(clientPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
