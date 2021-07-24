package com.juvis.core.file.module.filter;

import java.io.File;
import java.nio.file.Paths;

/**
 * Created by guava on 07/11/2016.
 */
public abstract class AbstractFileFilter implements FiroFilter {
    public File createTempOutFile(File file) {
        return new File(file.getParentFile().getAbsolutePath(), "_filter_" + file.getName());
    }

    public void moveTempFileToOriginalFile(File tempFile, File originalFile) {
        String filename = originalFile.getName();
        originalFile.delete();
        tempFile.renameTo(Paths.get(tempFile.getParentFile().getAbsolutePath(), filename).toFile());
    }

}
