package com.unvus.firo.module.service;

import com.unvus.firo.module.filter.FiroFilter;

import java.util.HashMap;
import java.util.Map;

public class FiroFilterRegistry {

    protected static Map<String, FiroFilter> filterMap = new HashMap();

    public static FiroFilter get(String type) {
        return filterMap.get(type);
    }

    public static void add(String code, FiroFilter filter) {
        filterMap.put(code, filter);
    }

}
