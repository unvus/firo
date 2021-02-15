package com.unvus.firo.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.imageio.ImageIO;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;

public class FiroWebUtil {


    public static final String FILE_SEP = System.getProperty("file.separator");

    private static int CACHE_PERIOD_UNIT = Calendar.MONTH;
    private static int CACHE_PERIOD_VALUE = 1;

    public static boolean needFreshResponse(HttpServletRequest request, SimpleDateFormat dateFormat) {
        boolean needFresh = true;
        String modifiedSince = request.getHeader("if-modified-since");
        if(modifiedSince == null) {
            Enumeration<String> sinceHeaders = request.getHeaders("if-modified-since");
            if(sinceHeaders.hasMoreElements()) {
                modifiedSince = sinceHeaders.nextElement();
            }
        }

        if(modifiedSince != null) {
            try {
                Date lastAccess = dateFormat.parse(modifiedSince);

                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                cal.add(CACHE_PERIOD_UNIT, CACHE_PERIOD_VALUE * -1);
                if(cal.getTime().compareTo(lastAccess) < 0) {
                    needFresh = false;
                }
            } catch (Exception ignore) {}
        }

        return needFresh;

    }

    public static void setCacheHeader(HttpServletResponse response, SimpleDateFormat dateFormat) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        response.setHeader("Last-Modified", dateFormat.format(cal.getTime()));

        cal.add(CACHE_PERIOD_UNIT, CACHE_PERIOD_VALUE);

        String maxAgeDirective = "max-age=" + (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000L;

        response.setHeader("Cache-Control",  maxAgeDirective);
        response.setHeader("Expires", dateFormat.format(cal.getTime()));
    }

    public static void writeFile(HttpServletResponse response, File f) throws IOException {
        FileInputStream fin = null;
        FileChannel inputChannel = null;
        WritableByteChannel outputChannel = null;
        try{
            fin = new FileInputStream(f);
            inputChannel = fin.getChannel();
            outputChannel = Channels.newChannel(response.getOutputStream());

            inputChannel.transferTo(0, fin.available(), outputChannel);
        }catch(Exception e){
            throw e;
        }finally{
            IOUtils.closeQuietly(fin);
            IOUtils.closeQuietly(inputChannel);
            IOUtils.closeQuietly(outputChannel);
        }
    }


    public static void writeFileWithWatermark(File f, BufferedImage watermarkImage, File output, String fileType) throws IOException {
        try{
            writeWatermarkImage(f, watermarkImage, output, fileType);
        }catch(Exception e){
            throw e;
        }
    }



    private static void writeWatermarkImage(File sourceImageFile, BufferedImage watermarkImage, File output, String fileType) throws IOException {
        BufferedImage sourceImage = ImageIO.read(sourceImageFile);

        // initializes necessary graphic properties
        Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);
        g2d.setComposite(alphaChannel);

        // calculates the coordinate where the image is painted
        int topLeftX = (sourceImage.getWidth() - watermarkImage.getWidth()) / 2;
        int topLeftY = (sourceImage.getHeight() - watermarkImage.getHeight()) / 2;

        // paints the image watermark
        g2d.drawImage(watermarkImage, topLeftX, topLeftY, null);

        ImageIO.write(sourceImage, fileType, output);
        g2d.dispose();
    }

    public static HttpServletRequest request() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    public static HttpServletResponse response() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    public static HttpServletRequest extractRequest(Class<? extends HttpServletRequest> requestClass, ServletRequest request) {

        if(requestClass.isInstance(request)) {
            return (HttpServletRequest)request;
        }else if(request instanceof HttpServletRequestWrapper) {
            return extractRequest(requestClass, ((HttpServletRequestWrapper)request).getRequest());
        }else {
            return null;
        }

    }


    public static void writeFileToClient(HttpServletResponse response, boolean isDownload, SimpleDateFormat dateFormat, String displayName, File f, String contentType) throws Exception {
        writeFileToClient(response, isDownload, dateFormat, displayName, f, contentType, null, null);
    }

    public static void writeFileToClient(HttpServletResponse response, boolean isDownload, SimpleDateFormat dateFormat, String displayName, File f, String contentType, BufferedImage waterMarkImage, String tmpDir) throws Exception {
        response.setContentType(contentType);

        if (isDownload) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(displayName, "UTF-8") + "\"");
        } else {
            FiroWebUtil.setCacheHeader(response, dateFormat);
        }

        String[] contentTypeArr = StringUtils.split(contentType, "/");
        if (waterMarkImage != null && contentTypeArr.length == 2 && "image".equals(contentTypeArr[0])) {
            File waterMarkedFile = File.createTempFile("watermark_", "_tmp", new File(tmpDir));
            FiroWebUtil.writeFileWithWatermark(f, waterMarkImage, waterMarkedFile, contentTypeArr[1]);

            response.setContentLength((int) waterMarkedFile.length());
            FiroWebUtil.writeFile(response, waterMarkedFile);
        } else {
            response.setContentLength((int) f.length());
            FiroWebUtil.writeFile(response, f);
        }
    }
}
