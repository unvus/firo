package com.juvis.core.file.module.filter;



import com.juvis.core.file.util.ImageResizeUtil;

import java.awt.*;

/**
 * Created by guava on 07/11/2016.
 */
public abstract class AbstractImageFilter extends AbstractFileFilter {


    public Dimension getResizeDimension(Dimension targetDimension, int maxWidth, int maxHeight) {
        return ImageResizeUtil.getResizeDimension(targetDimension, maxWidth, maxHeight);
    }

}