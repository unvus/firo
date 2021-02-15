package com.unvus.firo.module.filter.impl;

import com.imageresize4j.ImageResizeProcessor;
import com.unvus.firo.module.filter.AbstractImageFilter;
import com.unvus.firo.module.filter.FiroFilterChain;
import com.unvus.firo.core.util.ImageResizeUtil;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;

/**
 * Created by guava on 06/11/2016.
 */
public class ResizeImageFilter extends AbstractImageFilter {
    public static final String PARAM_MAX_WITH = "maxWith";
    public static final String PARAM_MAX_HEIGHT = "maxHeight";
    public static final String PARAM_ENGINE = "engine";

    public enum ENGINE {
        IR4J,
        SCALR
    }

    private int maxWith = 0;
    private int maxHeight = 0;

    private ENGINE engine;


    public void config(Map<String, Object> context) {
        if(context.containsKey(PARAM_MAX_WITH)) {
            maxWith = (int)context.get(PARAM_MAX_WITH);
        }
        if(context.containsKey(PARAM_MAX_HEIGHT)) {
            maxHeight = (int)context.get(PARAM_MAX_HEIGHT);
        }
        if(context.containsKey(PARAM_ENGINE)) {
            if(context.get(PARAM_ENGINE) instanceof ENGINE) {
                engine = (ENGINE)context.get(PARAM_ENGINE);
            }else {
                engine = ENGINE.valueOf((String)context.get(PARAM_ENGINE));
            }
        }else {
            engine = ENGINE.SCALR;
        }
    }

    public void doFilter(FiroFilterChain chain, File file) throws Exception {
        BufferedImage bi = ImageIO.read(file);
        int actualWidth = bi.getWidth();
        int actualHeight = bi.getHeight();
        Dimension actualDimension = new Dimension(actualWidth, actualHeight);
        Dimension tobeDimension = getResizeDimension(actualDimension, maxWith, maxHeight);
        if(actualDimension.equals(tobeDimension)) {
            chain.doFilter(file);
            return;
        }

        File tempOutFile = createTempOutFile(file);

        BufferedImage resizedBi;
        if(engine == ENGINE.IR4J) {
            resizedBi = ImageResizeUtil.resizeIn2PhasesViaIR4J(
                bi,
                (int)tobeDimension.getWidth(),
                (int)tobeDimension.getHeight(),
                ImageResizeProcessor.TYPE_NEAREST_NEIGHBOR,
                ImageResizeProcessor.TYPE_SHARP_5
            );
        }else {
            resizedBi = ImageResizeUtil.resizeScalr(
                bi,
                Scalr.Method.ULTRA_QUALITY,
                (int)tobeDimension.getWidth(),
                (int)tobeDimension.getHeight()
            );
        }

        ImageResizeUtil.write(resizedBi, tempOutFile, chain.getContentType());

        moveTempFileToOriginalFile(tempOutFile, file);

        chain.doFilter(file);

    }

}
