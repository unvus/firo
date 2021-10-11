package com.unvus.firo.module.filter;


import org.apache.tika.Tika;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by guava on 06/11/2016.
 */
public class FiroFilterChain {

    private Tika tika = new Tika();

    private String contentType = null;

    public FiroFilterChain() {

    }

    private List<FiroFilter> filterList = new ArrayList<>();

    private Iterator<FiroFilter> iterator;

    public void addFilter(Class<? extends FiroFilter> klass, Map<String, Object> config) throws Exception {
        FiroFilter filter = klass.newInstance();
        filter.config(config);
        filterList.add(filter);
    }

    public void addFilter(FiroFilter filter, Map<String, Object> config) throws Exception {
        filter.config(config);
        filterList.add(filter);
    }

    public void addFilter(FiroFilter filter) throws Exception {
        filterList.add(filter);
    }

    public void addFilters(List<FiroFilter> filters) {
        filterList.addAll(filters);
    }

    public int size() {
        return filterList.size();
    }

    public void startFilter(File file) throws Exception {
        detectFile(file);
        iterator = filterList.iterator();
        doFilter(file);
    }

    public void doFilter(File file) throws Exception {
        if(iterator.hasNext()) {
            iterator.next().doFilter(this, file);
        }
    }

    /**
     * 이미지 구분
     * @param file
     * @return
     * @throws Exception
     */
    private String detectFile(File file) throws Exception {
        if(contentType == null) {
            try {
                synchronized (tika) {
                    contentType = tika.detect(file);
                }
            } catch (Exception e) {
                contentType = new MimetypesFileTypeMap().getContentType(file);
            }
        }
        return contentType;
    }

    public String getContentType() {
        return contentType;
    }

}
