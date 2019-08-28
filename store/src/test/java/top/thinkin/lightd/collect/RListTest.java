package top.thinkin.lightd.collect;


import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.log4j.Log4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import top.thinkin.lightd.db.DB;
import top.thinkin.lightd.db.RList;

import java.util.ArrayList;
import java.util.List;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Log4j
public class RListTest {
    static DB db;

    @Before
    public void init() throws RocksDBException {
        if(db == null){
            RocksDB.loadLibrary();
            db = DB.build("D:\\temp\\db", false);
        }
    }

    @Test
    public void add() throws Exception {
        RList list = db.getList();
        try {
            for (int i = 0; i < 1000000; i++) {
                list.add("add", (i + "test").getBytes());
            }
            Assert.assertTrue(list.size("add") == 1000000);
        } finally {
            list.delete("add");
        }
    }

    @Test
    public void ttl() throws Exception {
        RList list = db.getList();
        try {
            for (int i = 0; i < 100000; i++) {
                list.add("ttl", (i + "test").getBytes());
            }
            list.ttl("ttl", 10);
            Thread.sleep(2 * 1000);
            Assert.assertTrue(list.isExist("ttl"));
            Thread.sleep((9 * 1000));
            Assert.assertTrue(!list.isExist("ttl"));
        } finally {
            db.clear();
        }
    }


    @Test
    public void pop() throws Exception {
        int k = 100*10000;
        List<byte[]> arrayList = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            arrayList.add((i+"test").getBytes());
        }
        RList list = db.getList();
        list.addAll("pop", arrayList);
        try {
            int i=0;
            while (true){
                List<byte[]> pops = list.blpop("pop", 100);
                if(CollectionUtil.isEmpty(pops)) break;
                for (byte[] v:pops) {
                    Assert.assertArrayEquals((i+"test").getBytes(),v);
                    i++;
                }
                Assert.assertEquals(list.size("pop"), k - i);
            }
        } finally {
            list.delete("pop");
        }
    }

    @Test
    public void range() throws Exception {
        RList list = db.getList();
        try {
            List<byte[]> arrayList = new ArrayList<>();
            for (int i = 0; i < 1000000; i++) {
                arrayList.add((i + "test").getBytes());
            }
            list.addAll("range", arrayList);
            List<byte[]> listrange = list.range("range", 50, 100);
            int i = 50;
            for (byte[] v:listrange){
                Assert.assertArrayEquals((i + "test").getBytes(), v);
                i++;
            }
        } finally {
            list.delete("range");
        }
    }

    @Test
    public void get() throws Exception {
        RList list = db.getList();
        try {
            List<byte[]> arrayList = new ArrayList<>();
            for (int i = 0; i < 1000000; i++) {
                arrayList.add((i + "test").getBytes());
            }
            list.addAll("get", arrayList);
            for (int i = 0; i < 100 * 10000; i++) {
                list.get("get", i);
                Assert.assertArrayEquals((i + "test").getBytes(), list.get("get", i));
            }
        } finally {
            list.delete("get");
        }
    }
}
