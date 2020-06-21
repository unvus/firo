package com.unvus.firo.core;

import com.unvus.firo.core.domain.FiroRoom;
import com.unvus.firo.core.domain.RoomService;
import com.unvus.firo.core.filter.FiroFilter;

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
