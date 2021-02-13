package com.unvus.firo.embedded.rest;

import com.unvus.firo.core.FiroRegistry;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.embedded.service.FiroService;
import com.unvus.firo.embedded.util.FiroWebUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Slf4j
@RestController
public class FiroCkEditorResource {

    private final FiroService firoService;


    private static final String CK_IMG_URL = "/assets/firo/editor/image";
    public static final String ROOM_CODE = "ckupload";

    public static final String CKEDITOR_RESULT =
        "<script type=\"text/javascript\">" +
            "window.parent.CKEDITOR.tools.callFunction(%s, '%s');" +
            "</script>";

    public FiroCkEditorResource(FiroService firoService) {
        this.firoService = firoService;
    }



    @RequestMapping(
        value="/api/firo/ckupload/dnd",
        produces    = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> ckUploadDrop(MultipartFile upload, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filename = firoService.uploadEditorImage(upload, ROOM_CODE);

        // drag & drop
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("fileName", "");
        resultMap.put("uploaded", 1);
        resultMap.put("url", CK_IMG_URL + filename);

        return new ResponseEntity<>(resultMap, HttpStatus.OK);

    }

    @RequestMapping(
        value = "/api/firo/ckupload/modal",
        produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String upload(MultipartFile upload, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filename = firoService.uploadEditorImage(upload, ROOM_CODE);

        // dialog
        response.setContentType("text/html;charset=UTF-8");
        return String.format(CKEDITOR_RESULT, request.getParameter("CKEditorFuncNum"), CK_IMG_URL + filename);

    }

    @RequestMapping(CK_IMG_URL + "/**")
    public void imageDown(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (!FiroWebUtil.needFreshResponse(request, dateFormat)) {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        String filePath = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        filePath = StringUtils.substringAfter(filePath, CK_IMG_URL);
        FiroCabinet cabinet = FiroRegistry.get(ROOM_CODE, "default");
        File f = cabinet.read(ROOM_CODE + filePath);

        if (f == null) {
            return;
        }

        FiroWebUtil.writeFileToClient(response, false, dateFormat, null, f, firoService.detectFile(f));
    }
}
