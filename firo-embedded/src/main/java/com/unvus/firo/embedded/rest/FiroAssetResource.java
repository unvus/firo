package com.unvus.firo.embedded.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageresize4j.ImageResizeProcessor;
import com.unvus.firo.core.util.ImageResizeUtil;

import com.unvus.firo.embedded.config.properties.FiroProperties;
import com.unvus.firo.embedded.domain.FiroFile;
import com.unvus.firo.embedded.service.FiroService;
import com.unvus.firo.embedded.util.FiroWebUtil;
import com.unvus.util.LfuCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.unvus.firo.embedded.util.FiroWebUtil.FILE_SEP;

@Slf4j
@RestController
@RequestMapping("/assets/firo")
public class FiroAssetResource {

    private final FiroService firoService;

    private final FiroProperties firoProperties;

    private static LfuCache<Object, Object> nullCache = new LfuCache(10000l, 300l, true);

    public static final String CKEDITOR_RESULT =
        "<script type=\"text/javascript\">" +
            "window.parent.CKEDITOR.tools.callFunction(%s, '%s');" +
            "</script>";

    public FiroAssetResource(ObjectMapper objectMapper, FiroService firoService, FiroProperties firoProperties) {
        this.firoService = firoService;
        this.firoProperties = firoProperties;
    }



    @GetMapping(value = {
        "/attach/{action:view|download}/{id}"  /** Action Log 안 남길때(assets URL은 sso filter 실행 안됨 **/
//                , "/attach/{action:view|download}/{id}" /** Action Log를 남길때 **/
    }
    )
    public void view(@PathVariable("action") String action,
                     @PathVariable("id") Long id,
                     @RequestParam(value="w", required = false) Integer width,
                     @RequestParam(value="h", required = false) Integer height,
                     HttpServletRequest request,
                     HttpServletResponse response) throws Exception {

        boolean isDownload = "download".equals(action);

        SimpleDateFormat dateFormat = getCacheDateFormat();

        if (!FiroWebUtil.needFreshResponse(request, dateFormat) && !isDownload) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        FiroFile attach = firoService.getAttach(id);

        if (attach == null) {
            return;
        }

        File f = new File(firoProperties.getDirectory().getBaseDir() + attach.getSavedDir() + attach.getSavedName());

        if (!f.exists()) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(width, height, attach.getId(), f, contentType);

        File result = tf;
        if(tf == null) {
            result = f;
        }

        writeFileToClient(response, isDownload, dateFormat, attach.getDisplayName(), result, contentType);
    }

    @GetMapping(value = "/attachTemp/{action:view|download}/{id}")
    public void viewTemp(@PathVariable("action") String action,
                         @PathVariable("id") String id,
                         @RequestParam(value="w", required = false) Integer width,
                         @RequestParam(value="h", required = false) Integer height,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {

        boolean isDownload = "download".equals(action);

        SimpleDateFormat dateFormat = getCacheDateFormat();

        if (!FiroWebUtil.needFreshResponse(request, dateFormat) && !isDownload) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }


        File f = new File(firoProperties.getDirectory().getTmpDir(), id);

        if (!f.exists()) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(width, height, null, f, contentType);

        if(tf == null) {
            writeFileToClient(response, isDownload, dateFormat, id + StringUtils.substringAfter(contentType, "/"), f, contentType);
        }else {
            writeFileToClient(response, isDownload, dateFormat, id + StringUtils.substringAfter(contentType, "/"), tf, contentType);
        }

    }

    /**
     * 이미지 보기
     * <p>
     * /view/attach/product/271/thumbnail
     *
     * @param refType  참조타입코드 - 예) 상품 = product
     * @param refKey   참조키 - 예) 상품 아이디 : 271
     * @param mapCode  세부타입코드 - 예) 섬네일 이미지 : thumbnail
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping(value = "/attach/{action:view|download}/{refType}/{refKey}/{mapCode}")
    public void viewByRefType(
        @PathVariable("action") String action,
        @PathVariable("refType") String refType,
        @PathVariable("refKey") Long refKey,
        @PathVariable("mapCode") String mapCode,
        @RequestParam(value="extAlt", required = false) String extAlt,
        @RequestParam(value="ext", required = false) String ext,
        @RequestParam(value="fmetaValue", required = false) String fmetaValue,
        @RequestParam(value="w", required = false) Integer width,
        @RequestParam(value="h", required = false) Integer height,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        viewByRefTypeOrder(action, refType, refKey, mapCode, 1, extAlt, ext, fmetaValue, width, height, request, response);
    }


    /**
     * 이미지 보기 - 여러개가 봔환되는 경우 특정 순서의 이미지 얻기 <br/>
     * 첫번째 이미지가 "1" 로 시작
     * <p>
     * /view/attach/product/271/thumbnail/2
     *
     * @param refType  참조타입코드 - 예) 상품 = product
     * @param refKey   참조키 - 예) 상품 아이디 : 271
     * @param mapCode  세부타입코드 - 예) 섬네일 이미지 : thumbnail
     * @param idx      획득하고자 하는 이미지 순서 인덱스 : "1" 로 시작
     * @param request
     * @param response
     * @throws Exception
     */
    @GetMapping(value = "/attach/{action:view|download}/{refType}/{refKey}/{mapCode}/{idx}")
    public void viewByRefTypeOrder(
        @PathVariable("action") String action,
        @PathVariable("refType") String refType,
        @PathVariable("refKey") Long refKey,
        @PathVariable("mapCode") String mapCode,
        @PathVariable("idx") Integer idx,
        @RequestParam(value="extAlt", required = false) String extAlt,
        @RequestParam(value="ext", required = false) String ext,
        @RequestParam(value="fmetaValue", required = false) String fmetaValue,
        @RequestParam(value="w", required = false) Integer width,
        @RequestParam(value="h", required = false) Integer height,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        boolean isDownload = "download".equals(action);

        SimpleDateFormat dateFormat = getCacheDateFormat();

        if (!FiroWebUtil.needFreshResponse(request, dateFormat) && !isDownload) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        List<String> altList = null;

        if(StringUtils.isNotBlank(extAlt)) {
            ext = null;
            altList = Arrays.asList(StringUtils.split(extAlt, ":"));

        }

        List<FiroFile> attachList = firoService.listAttachByRef(refType, refKey, mapCode, StringUtils.isNotBlank(ext)?ext:null, fmetaValue);

        if(CollectionUtils.isEmpty(attachList)) {
            return;
        }

        FiroFile attach = null;

        if(altList != null) {
            // search by alt list

            for(String alt : altList) {
                for(FiroFile att : attachList) {
                    if(StringUtils.equals(alt, att.getExt())) {
                        attach = att;
                        break;
                    }
                }
                if(attach != null) {
                    break;
                }
            }

            if(attach == null) {
                attach = attachList.get(0);
            }
        }else if (attachList.size() >= idx) {
            // search by index

            attach = attachList.get(idx - 1);
        }

        if(attach == null) {
            return;
        }

        File f = new File(firoProperties.getDirectory().getBaseDir() + attach.getSavedDir() + attach.getSavedName());

        if (!f.exists()) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(width, height, attach.getId(), f, contentType);

        File result = tf;
        if(tf == null) {
            result = f;
        }

        writeFileToClient(response, isDownload, dateFormat, attach.getDisplayName(), result, contentType);

    }

    private File convertScaledImage(Integer width, Integer height, Long key, File f, String contentType) {
        if(width == null) {
            width = 0;
        }
        if(height == null) {
            height = 0;
        }

        File tf = null;
        if(width > 0 || height > 0) {
            boolean imageCreated = false;
            // get path
            String base = firoProperties.getDirectory().getBaseDir();
            String filePath = StringUtils.substringAfter(f.getAbsolutePath(), base);
            String fullPath = base + File.separator + "scaled" + StringUtils.substringBeforeLast(filePath, "/");
            String scaledName = "scaled_" + key + "_" + width + "_" + height;


            try {
                if(key != null) {
                    // cache 해당 안되는 애들은 똑같은 로직 타지 않도록 map 에 담아 바로 null 을 넘길 수 있도록. map size 제한
                    if(nullCache.containsKey(scaledName)) {
                        return null;
                    }
                    tf = new File(fullPath, scaledName);
                    if (tf.exists()) {
//                        log.info("CACHED_IMAGE_FROM:" + fullPath + File.separator + scaledName);
                        return tf;
                    }
                }else {
                    tf = File.createTempFile("scaled_", "_tmp", new File(firoProperties.getDirectory().getTmpDir()));
                }

                BufferedImage bi = ImageIO.read(f);
                int actualWidth = bi.getWidth();
                int actualHeight = bi.getHeight();
                Dimension actualDimension = new Dimension(actualWidth, actualHeight);
                Dimension tobeDimension = ImageResizeUtil.getResizeDimension(actualDimension, width, height);

                if (actualDimension.getWidth() > tobeDimension.getWidth()) {
                    if(key != null) {
                        Path path = Paths.get(fullPath);

                        if (!Files.exists(path)) {
                            Files.createDirectories(path);
                        }
                    }

                    BufferedImage resizedBi = ImageResizeUtil.resizeIn2PhasesViaIR4J(
                        bi,
                        (int) tobeDimension.getWidth(),
                        (int) tobeDimension.getHeight(),
                        ImageResizeProcessor.TYPE_NEAREST_NEIGHBOR,
                        ImageResizeProcessor.TYPE_SHARP_5
                    );

                    ImageResizeUtil.write(resizedBi, tf, contentType);
                    imageCreated = true;
                }
            } catch (Exception ignore) {
                log.warn(ignore.getMessage(), ignore);
            }

            if(!imageCreated && tf != null) {
                tf.delete();
                tf = null;
                if(key != null) {
                    nullCache.put(scaledName, true);
                }
            }
        }

        return tf;
    }


    @RequestMapping(
        value="/ckupload/modal",
        produces    = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String upload(MultipartFile upload, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filename = firoService.uploadEditorImage(upload, null, "ckupload");

        // dialog
        response.setContentType("text/html;charset=UTF-8");
        return String.format(CKEDITOR_RESULT, request.getParameter("CKEditorFuncNum"), "/assets/editor/image" + filename);

    }

    @RequestMapping("/editor/image/**")
    public void imageDown(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (!FiroWebUtil.needFreshResponse(request, dateFormat)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        String filePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        filePath = StringUtils.substringAfter(filePath, "/assets/editor/image");
        String baseDir = firoService.getBaseDir() + FILE_SEP + "ckeditor";

        File f = new File(baseDir + filePath);

        if(!f.exists()) {
            return;
        }

        writeFileToClient(response, false, dateFormat, null, f, firoService.detectFile(f));
    }

    private void writeFileToClient(HttpServletResponse response, boolean isDownload, SimpleDateFormat dateFormat, String displayName, File f, String contentType) throws Exception {
        writeFileToClient(response, isDownload, dateFormat, displayName, f, contentType, null);
    }

    private void writeFileToClient(HttpServletResponse response, boolean isDownload, SimpleDateFormat dateFormat, String displayName, File f, String contentType, BufferedImage waterMarkImage) throws Exception {
        response.setContentType(contentType);

        if (isDownload) {
            response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(displayName, "UTF-8") + "\"");
        } else {
            FiroWebUtil.setCacheHeader(response, dateFormat);
        }

        String[] contentTypeArr = StringUtils.split(contentType, "/");
        if(waterMarkImage != null && contentTypeArr.length == 2 && "image".equals(contentTypeArr[0])) {
            File waterMarkedFile = File.createTempFile("watermark_", "_tmp", new File(firoProperties.getDirectory().getTmpDir()));
            FiroWebUtil.writeFileWithWatermark(f, waterMarkImage, waterMarkedFile, contentTypeArr[1]);

            response.setContentLength((int) waterMarkedFile.length());
            FiroWebUtil.writeFile(response, waterMarkedFile);
        }else {
            response.setContentLength((int) f.length());
            FiroWebUtil.writeFile(response, f);
        }
    }

    private SimpleDateFormat getCacheDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat;
    }
}
