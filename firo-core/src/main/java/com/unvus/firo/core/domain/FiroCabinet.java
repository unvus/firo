package com.unvus.firo.core.domain;

import com.unvus.firo.core.adapter.Adapter;
import com.unvus.firo.core.filter.FiroFilterChain;
import com.unvus.firo.core.policy.DirectoryPathPolicy;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Builder(builderMethodName = "privateBuilder")
public class FiroCabinet {
    private FiroRoom room;

    private String cabinetCode;

    private FiroFilterChain filterChain;

    private Adapter adapter;

    private DirectoryPathPolicy directoryPathPolicy;

    public static FiroCabinetBuilder builder(FiroRoom room) {
        return privateBuilder().room(room);
    }

    public File readTemp(String path) throws Exception {
        return adapter.readTemp(this.directoryPathPolicy, path);
    }

    public File read(String path) throws Exception {
        return adapter.read(this.directoryPathPolicy, path);
    }

    public File writeTemp(String path, InputStream in) throws Exception {
        return adapter.writeTemp(this.directoryPathPolicy, path, in);
    }

    public void write(String path, InputStream in) throws Exception {
        adapter.write(this, path, in);
    }

    public void delete(String path) throws Exception {
        adapter.delete(this.directoryPathPolicy, path);
    }
}
