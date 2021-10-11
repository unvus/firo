package com.unvus.firo.module.filter.impl;


import com.unvus.firo.module.filter.FiroFilter;
import com.unvus.firo.module.filter.FiroFilterChain;

import java.io.File;
import java.util.Map;

/**
 * Created by guava on 07/11/2016.
 */
public class MaxSizeExceptionFilter implements FiroFilter {
    public static final String PARAM_MAX_SIZE = "maxSize";

    public long maxSize = 0;

    public void config(Map<String, Object> context) {
        if(context.containsKey(PARAM_MAX_SIZE)) {
            int maxSizeInMB = (int)context.get(PARAM_MAX_SIZE);

            maxSize = maxSizeInMB * 1024 * 1024;
        }

    }

    public void doFilter(FiroFilterChain chain, File file) throws Exception {
        if(maxSize > 0 && file.length() > maxSize) {
            throw new MaxSizeExceedException("max size exceeded");
        }

        chain.doFilter(file);
    }

    public class MaxSizeExceedException extends Exception {
        /**
         * Constructor for MaxSizeExceedException.
         *
         * @param message exception message
         */
        public MaxSizeExceedException(final String message) {
            super(message);
        }
    }
}
