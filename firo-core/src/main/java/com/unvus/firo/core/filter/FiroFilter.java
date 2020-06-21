package com.unvus.firo.core.filter;

import java.io.File;
import java.util.Map;

/**
 * Created by guava on 06/11/2016.
 */
public interface FiroFilter {
    void config(Map<String, Object> context);

    void doFilter(FiroFilterChain chain, File file) throws Exception;
}
