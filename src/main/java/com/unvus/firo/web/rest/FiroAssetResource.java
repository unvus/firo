package com.unvus.firo.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.imageresize4j.ImageResizeProcessor;
import com.unvus.firo.util.FiroWebUtil;
import com.unvus.firo.util.ImageResizeUtil;
import com.unvus.firo.module.service.FiroRegistry;
import com.unvus.firo.module.service.domain.FiroCategory;
import com.unvus.firo.config.properties.FiroProperties;
import com.unvus.firo.module.service.domain.FiroFile;
import com.unvus.firo.module.service.FiroService;
import com.unvus.util.LfuCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

@Slf4j
@RestController
@RequestMapping("/assets/firo")
public class FiroAssetResource {

    private final FiroService firoService;

    private final FiroProperties firoProperties;

    private static LfuCache<Object, Object> nullCache = new LfuCache(10000l, 300l, true);


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

        FiroCategory category = FiroRegistry.get(attach.getRefTarget(), attach.getRefTargetType());

        File f = category.read(attach.getSavedDir() + attach.getSavedName());
//        File f = new File(firoProperties.getDirectory().getBaseDir() + attach.getSavedDir() + attach.getSavedName());

        if (f == null) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(category, width, height, attach, f, contentType);

        File result = tf;
        if(tf == null) {
            result = f;
        }

        FiroWebUtil.writeFileToClient(response, isDownload, dateFormat, attach.getDisplayName(), result, contentType);
    }

    @GetMapping(value = "/attachTemp/{action:view|download}/{domainCode}/{categoryCode}/{id}")
    public void viewTemp(@PathVariable("action") String action,
                         @PathVariable("domainCode") String domainCode,
                         @PathVariable("categoryCode") String categoryCode,
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

        FiroCategory category = FiroRegistry.get(domainCode, categoryCode);

        File f = category.readTemp(id);

        if (f == null) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(category, width, height, null, f, contentType);

        if(tf == null) {
            FiroWebUtil.writeFileToClient(response, isDownload, dateFormat, id + StringUtils.substringAfter(contentType, "/"), f, contentType);
        }else {
            FiroWebUtil.writeFileToClient(response, isDownload, dateFormat, id + StringUtils.substringAfter(contentType, "/"), tf, contentType);
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

        viewByRefTypeOrder(action, refType, refKey, mapCode, 0, extAlt, ext, fmetaValue, width, height, request, response);
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
        }else if (attachList.size() > idx) {
            // search by index

            attach = attachList.get(idx);
        }

        if(attach == null) {
            return;
        }

        FiroCategory category = FiroRegistry.get(refType, mapCode);

        if(category.getSecureAccessFunc() != null && !category.getSecureAccessFunc().accept(request, attach)) {
            return;
        }

        File f = category.read(attach.getSavedDir() + attach.getSavedName());

        if (!f.exists()) {
            return;
        }

        String contentType = firoService.detectFile(f);

        File tf = convertScaledImage(category, width, height, attach, f, contentType);

        File result = tf;
        if(tf == null) {
            result = f;
        }

        FiroWebUtil.writeFileToClient(response, isDownload, dateFormat, attach.getDisplayName(), result, contentType);

    }

    private File convertScaledImage(FiroCategory category, Integer width, Integer height, FiroFile attach, File f, String contentType) {
        if(width == null) {
            width = 0;
        }
        if(height == null) {
            height = 0;
        }

        File tf = null;
        if(width > 0 || height > 0) {
            boolean imageCreated = false;

            String fullPath = Paths.get(category.getDirectoryPathPolicy().getBaseDir(), attach.getSavedDir(), "scaled_" + attach.getId()).toString();

            String scaledName = width + "_" + height;


            try {
                // cache 해당 안되는 파일은 똑같은 로직 타지 않도록 map 에 담아 바로 null 을 넘길 수 있도록. map size 제한
                if (nullCache.containsKey(attach.getId() + "_" + scaledName)) {
                    return null;
                }
                try {
                    tf = category.read(Paths.get(fullPath, scaledName).toString());
                }catch (Exception ignore) {}

                if(tf != null) {
                    return tf;
                }

                tf = File.createTempFile("scaled_", ".tmp");

                BufferedImage bi = ImageIO.read(f);
                int actualWidth = bi.getWidth();
                int actualHeight = bi.getHeight();
                Dimension actualDimension = new Dimension(actualWidth, actualHeight);
                Dimension tobeDimension = ImageResizeUtil.getResizeDimension(actualDimension, width, height);

                if (actualDimension.getWidth() > tobeDimension.getWidth()) {
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

                category.write(fullPath, scaledName, new FileInputStream(tf), tf.length(), attach.getFileType());
            } catch (Exception ignore) {
                log.warn(ignore.getMessage(), ignore);
            }

            if(!imageCreated && tf != null) {
                tf.delete();
                tf = null;
                nullCache.put(attach.getId() + "_" + scaledName, true);
            }
        }

        return tf;
    }

    private SimpleDateFormat getCacheDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat;
    }
}
