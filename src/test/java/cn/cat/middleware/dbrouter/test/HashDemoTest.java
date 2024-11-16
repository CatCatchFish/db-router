package cn.cat.middleware.dbrouter.test;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class HashDemoTest {
    @Test
    public void hashTest() {
        // 初始化一组字符串
        List<String> list = new ArrayList<>();
        list.add("jlkk");
        list.add("lopi");
        list.add("cat");
        list.add("e4we");
        list.add("alpo");
        list.add("yhjk");
        list.add("plop");

        // 定义要存放的数组
        String[] tab = new String[8];

        // 循环存放
        for (String key : list) {
            // 计算索引
            int idx = key.hashCode() & (tab.length - 1);
            if (tab[idx] == null) {
                tab[idx] = key;
                continue;
            }
            tab[idx] = tab[idx] + "->" + key;
        }
        // 输出测试结果
        System.out.println(JSON.toJSONString(tab));
    }

    @Test
    public void hashDisturbTest() {
        List<String> list = new ArrayList<>();
        list.add("jlkk");
        list.add("lopi");
        list.add("jmdw");
        list.add("e4we");
        list.add("io98");
        list.add("nmhg");
        list.add("vfg6");
        list.add("gfrt");
        list.add("alpo");
        list.add("vfbh");
        list.add("bnhj");
        list.add("zuio");
        list.add("iu8e");
        list.add("yhjk");
        list.add("plop");
        list.add("dd0p");

        // 定义要存放的数组
        String[] tab = new String[8];

        for (String key : list) {
            int hash = key.hashCode() ^ (key.hashCode() >>> 16);
            int idx = hash & (tab.length - 1);
            if (tab[idx] == null) {
                tab[idx] = key;
                continue;
            }
            tab[idx] = tab[idx] + "->" + key;
        }
        System.out.println(JSON.toJSONString(tab));
    }
}
