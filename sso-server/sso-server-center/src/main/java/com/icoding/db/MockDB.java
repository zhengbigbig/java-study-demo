package com.icoding.db;

import com.icoding.vo.ClientInfoVo;
import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.*;

// 数据结构
public class MockDB {

    public static Set<String> T_TOKEN  = new HashSet<String>(); // token保存表

    // 用户登出地址保存表  token， Alist
    public static Map<String,List<ClientInfoVo>> T_CLIENT_INFO
            = new HashMap<String, List<ClientInfoVo>>();


}
