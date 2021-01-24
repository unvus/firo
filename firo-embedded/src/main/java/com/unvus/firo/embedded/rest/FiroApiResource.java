package com.unvus.firo.embedded.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unvus.firo.core.FiroFilterRegistry;
import com.unvus.firo.core.FiroRoomRegistry;
import com.unvus.firo.core.filter.FiroFilter;
import com.unvus.firo.core.filter.FiroFilterChain;
import com.unvus.firo.embedded.domain.AttachBag;
import com.unvus.firo.embedded.domain.FiroFile;
import com.unvus.firo.embedded.service.FiroService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/firo")
public class FiroApiResource {

    private final ObjectMapper objectMapper;

    private final FiroService firoService;

    public FiroApiResource(ObjectMapper objectMapper, FiroService firoService) {
        this.objectMapper     = objectMapper;
        this.firoService = firoService;
    }

    @PostMapping(value = "/attach/tmp")
    public ResponseEntity<Map<String, Object>> uploadTemp(
        @RequestParam("refTarget") String refTarget,
        @RequestParam(value = "refTargetType", required = false) String refTargetType,
        @RequestParam(value = "filters", required = false) String filters,
        @RequestParam("file") MultipartFile file,
        HttpServletRequest request,
        RedirectAttributes redirectAttributes) throws Exception {


        FiroFilterChain filterChain = null;

        if (filters != null) {
            Map<String, Map<String, Object>> filterMap =
                objectMapper.readValue(filters, new TypeReference<Map<String, Map<String, Object>>>() {});

            filterChain = extractToFilterChain(filterMap);
        }

        List<Map<String, Object>> files = new ArrayList();
        Map<String, Object> item = new HashMap<>();

        try {
            String filename = firoService.uploadTemp(file, FiroRoomRegistry.get(refTarget), refTargetType, filterChain);

            item.put("name", filename);
            item.put("displayName", file.getOriginalFilename());
            item.put("size", file.getSize());
            item.put("type", file.getContentType());
            item.put("url", null);
            item.put("thumbnailUrl", null);
            item.put("deleteUrl", null);
            item.put("deleteType", "DELETE");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            item.put("error", e.getMessage());
        }

        files.add(item);

        Map<String, Object> result = new HashMap<>();
        result.put("files", files);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/attach/{roomName}/{roomKey}")
    public ResponseEntity<Map<String, Object>> uploadSave(
        @PathVariable("roomName") String refType,
        @PathVariable("roomKey") Long refKey,
        @RequestBody AttachBag attachBag) throws Exception {

        List<FiroFile>  attachList = firoService.save(refKey, attachBag);


        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("attachList", attachList);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value="/attach",
        produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> remove(@RequestBody List<FiroFile> attaches) throws Exception{

        firoService.deleteAttach(attaches);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PutMapping(value="/attach/",
        produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> modify(@RequestBody List<FiroFile> attaches) throws Exception{

        for(FiroFile attach : attaches) {
            firoService.updateAttach(attach);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * GET    /attach/{refType}/{refKey} : 첨부 리스트 조회
     */
    @GetMapping(value="/attach",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<FiroFile>> list(@RequestParam Map<String, Object> param,
                                               @RequestParam(value = "roomKeyList", required = false) List<Long> roomKeyList,
                                               @RequestParam(value = "cabinetList", required = false) List<String> cabinetList) throws Exception{
        param.put("roomKeyList", roomKeyList);
        param.put("cabinetList", cabinetList);
        List<FiroFile> list = firoService.listAttach(param);
        return new ResponseEntity<List<FiroFile>>(list, HttpStatus.OK);
    }


    /**
     * GET  /attach/{refType}/{refKey} : 첨부 상세 조회
     *
     * @param refType 제품 아이디 (시퀀스키값)
     * @return 제품 객체
     */
    @GetMapping(value = "/attach/{roomName}/{roomKey}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttachBag> getAttach(HttpServletRequest request,
                                                     @PathVariable("roomName") String refType,
                                                     @PathVariable("roomKey") Long refKey) {

        return getAttach(request, refType, refKey, null, null);
    }


    /**
     * GET  /attach/{refType}/{refKey} : 첨부 상세 조회
     *
     * @param refType 제품 아이디 (시퀀스키값)
     * @return 제품 객체
     */
    @GetMapping(value = "/attach/{roomName}/{roomKey}/{cabinetName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttachBag> getAttach(HttpServletRequest request,
                                                     @PathVariable("roomName") String refType,
                                                     @PathVariable("roomKey") Long refKey,
                                                     @PathVariable("cabinetName") String cabinetName,
                                                     @RequestParam(value="q", required = false) Map<String, Object> param) {
        if(param != null && param.containsKey("roomKeys")){
            refKey = null;
        }

        AttachBag bag = firoService.getAttachBagByRef(refType, refKey, cabinetName, param);

        return Optional.ofNullable(bag)
            .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
            .orElse(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
            );
    }


    @PostMapping("/attach/copy/{roomName}/{roomKey}/{cabinetName}")
    public ResponseEntity<Object> imageCopy(
        @PathVariable("roomName") String refType,
        @PathVariable("roomKey") Long refKey,
        @PathVariable("cabinetName") String cabinetName,
        @RequestBody List<Long> copyIds,
        HttpServletRequest request) throws Exception {

        // vr 기존 게시 해재
        if(cabinetName.equals("vrphoto")) {
            List<FiroFile> postedList = firoService.listAttachByRef(refType, refKey, cabinetName, "10");
            if(postedList.size() > 0) {
                for(FiroFile attach : postedList) {
                    attach.setExt("00");
                    firoService.updateAttach(attach);
                }
            }
        }

        firoService.copy(FiroRoomRegistry.get(refType), null, refKey, null, copyIds);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    private FiroFilterChain extractToFilterChain(Map<String, Map<String, Object>> filterMap) {
        FiroFilterChain filterChain = new FiroFilterChain();

        filterMap.forEach((k, config) -> {
            try {
                FiroFilter filter = FiroFilterRegistry.get(k);
                boolean created = false;
                if(filter == null) {
                    created = true;
                    filter = filter.getClass().newInstance();
                }
                if (config == null || config.isEmpty()) {
                    filterChain.addFilter(filter);
                } else {
                    if(!created) {
                        filter = filter.getClass().newInstance();
                    }
                    filterChain.addFilter(filter, config);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return filterChain;
    }

}
