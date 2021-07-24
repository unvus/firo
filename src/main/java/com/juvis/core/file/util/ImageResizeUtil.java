package com.juvis.core.file.util;

import com.imageresize4j.ImageResizeProcessor;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Created by guava on 07/11/2016.
 */
public class ImageResizeUtil {
    private final static Logger log = LoggerFactory.getLogger(ImageResizeUtil.class);

    //scale limit to when apply 2-phase downscaling or not
    public static final float SCALE_LIMIT = 0.25f;

    public static BufferedImage resizeIn2PhasesViaIR4J(BufferedImage source,
                                                       int destWidth, int destHeight,
                                                       int firstInterpolation,
                                                       int secondInterpolation) {
        if (source == null)
            throw new NullPointerException("source image is NULL!");
        if (destWidth <= 0 && destHeight <= 0)
            throw new IllegalArgumentException("destination width & height are both <=0!");
        //calculate scale factors
        float scaleX = (float) destWidth / source.getWidth();
        float scaleY = (float) destHeight / source.getHeight();
        //check if we really need 2-phase schema
        if (scaleX < SCALE_LIMIT && scaleY < SCALE_LIMIT) {
            //calculate the most appropriate intermediate image size
            int sizeMultiplier = 2;
            //if scale factors are too small then we need a larger intermediate image
            if (scaleX < SCALE_LIMIT / 2 || scaleY < SCALE_LIMIT / 2)
                sizeMultiplier = 4;
            //create the processor for the 1-st phase
            ImageResizeProcessor preProcessor = new ImageResizeProcessor(firstInterpolation);
            //generate an intermediate image
            BufferedImage intermediate = preProcessor.resize(source, destWidth * sizeMultiplier, destHeight * sizeMultiplier);
            //create the processor for the final phase
            ImageResizeProcessor postProcessor = new ImageResizeProcessor(secondInterpolation);
            //generate the final result
            return postProcessor.resize(intermediate, destWidth, destHeight);
        } else {
            //just simple resize with the specified interpolation
            ImageResizeProcessor processor = new ImageResizeProcessor(secondInterpolation);
            return processor.resize(source, destWidth, destHeight);
        }
    }

    public static BufferedImage resizeScalr(BufferedImage source, Scalr.Method method, int destWidth, int destHeight) {
        return Scalr.resize(source, method, destWidth, destHeight);
    }

    public static BufferedImage resizeJava2DNative(BufferedImage source, int destWidth, int destHeight,
                                                   Object interpolation) {
        if (source == null)
            throw new NullPointerException("source image is NULL!");
        if (destWidth <= 0 && destHeight <= 0)
            throw new IllegalArgumentException("destination width & height are both <=0!");
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double xScale = ((double) destWidth) / (double) sourceWidth;
        double yScale = ((double) destHeight) / (double) sourceHeight;
        if (destWidth <= 0) {
            xScale = yScale;
            destWidth = (int) Math.rint(xScale * sourceWidth);
        }
        if (destHeight <= 0) {
            yScale = xScale;
            destHeight = (int) Math.rint(yScale * sourceHeight);
        }
        GraphicsConfiguration gc = getDefaultConfiguration();
        BufferedImage result = gc.createCompatibleImage(destWidth, destHeight, source.getColorModel().getTransparency());
        Graphics2D g2d = null;
        try {
            g2d = result.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
            AffineTransform at =
                    AffineTransform.getScaleInstance(xScale, yScale);
            g2d.drawRenderedImage(source, at);
        } finally {
            if (g2d != null)
                g2d.dispose();
        }
        return result;
    }

    public static GraphicsConfiguration getDefaultConfiguration() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        return gd.getDefaultConfiguration();
    }

    public static void write(BufferedImage input, File file, String mimeType) throws IOException {
        Iterator iter = ImageIO.getImageWritersByMIMEType(mimeType);

        if (iter.hasNext()) {
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp =  writer.getDefaultWriteParam();
            if (iwp.canWriteCompressed()) {
                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwp.setCompressionQuality(0.95f);
            }
            FileImageOutputStream output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage image = new IIOImage(input, null, null);
            writer.write(null, image, iwp);
            output.close();
        }else {
            throw new IOException("no writer found for mime-type : " + mimeType);
        }
    }

    public static Dimension getResizeDimension(Dimension targetDimension, int maxWidth, int maxHeight) {
        double width = 0;
        double height = 0;

        boolean fitToWidth = Boolean.FALSE;
        if(targetDimension.getWidth() > maxWidth || targetDimension.getHeight() > maxHeight) {
            if(maxWidth > 0 && maxHeight > 0) {
                double ratio = maxHeight / maxWidth;
                if(targetDimension.getWidth() * ratio < targetDimension.getHeight()) {
                    fitToWidth = Boolean.TRUE;
                }
            }else if(maxWidth > 0 && targetDimension.getWidth() > maxWidth) {
                fitToWidth = Boolean.TRUE;
            }else if(maxHeight > 0 && targetDimension.getHeight() > maxHeight) {
                fitToWidth = Boolean.FALSE;
            }else {
                return targetDimension;
            }
        }else {
            return targetDimension;
        }

        if(fitToWidth == Boolean.TRUE) {
            width = maxWidth;
            height = maxWidth * (targetDimension.getHeight() / targetDimension.getWidth());
        }else if(fitToWidth == Boolean.FALSE) {
            height = maxHeight;
            width = maxHeight * (targetDimension.getWidth() / targetDimension.getHeight());
        }

        return new Dimension((int)width, (int)height);
    }
}
