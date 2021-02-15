package com.unvus.firo.module.filter.impl;


import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.unvus.firo.module.filter.AbstractImageFilter;
import com.unvus.firo.module.filter.FiroFilterChain;
import com.unvus.firo.core.util.ImageOrientationUtil;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

public class AutoFixOrientationImageFilter extends AbstractImageFilter {

    @Override
    public void config(Map<String, Object> context) {

    }

    @Override
    public void doFilter(FiroFilterChain chain, File file) throws Exception {

        Metadata metadata = ImageOrientationUtil.getMetadata(file);
        if (metadata == null || ((!metadata.containsDirectoryOfType(ExifIFD0Directory.class) || !metadata.containsDirectoryOfType(JpegDirectory.class)))) {
            chain.doFilter(file);
            return;
        }


        ImageOrientationUtil.ImageOrientation imageOrientation = ImageOrientationUtil.readImageOrientation(file);

        if(imageOrientation.orientation == 1) {
            chain.doFilter(file);
            return;
        }

        AffineTransform affineTransform = ImageOrientationUtil.getExifTransformation(imageOrientation);
        BufferedImage bufferedImage = ImageIO.read(file);
        BufferedImage destBufferedImage = ImageOrientationUtil.transformImage(bufferedImage, affineTransform);

        // FIXME extension
//        String ext = FilenameUtils.getExtension(file.getName());
        File tempOutFile = createTempOutFile(file);
        ImageIO.write(destBufferedImage, "jpg", tempOutFile);
        moveTempFileToOriginalFile(tempOutFile, file);

        chain.doFilter(file);
    }
}
