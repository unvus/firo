package com.juvis.core.file.web.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.juvis.core.file.util.FiroUtil;
import com.juvis.core.file.module.service.FiroFilterRegistry;
import com.juvis.core.file.module.service.FiroRegistry;
import com.juvis.core.file.module.filter.FiroFilter;
import com.juvis.core.file.module.filter.FiroFilterChain;
import com.juvis.core.file.module.service.domain.AttachBag;
import com.juvis.core.file.module.service.domain.FiroCategory;
import com.juvis.core.file.module.service.domain.FiroFile;
import com.juvis.core.file.module.service.FiroService;
import com.juvis.core.file.module.service.domain.FiroDomain;
import com.unvus.util.FieldMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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


    /**
     * GET    /attach/{refType}/{refKey} : 첨부 리스트 조회
     */
    @GetMapping(value = "/config", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FieldMap> getConfig() throws Exception {
        FieldMap result = new FieldMap();
        result.put("directUrl", FiroRegistry.getDefaultDirectUrl());
//        result.put("secret", FiroRegistry.getSecret());

        Map<String, FiroDomain> firoDomainMap = FiroRegistry.getAllDomain();

        FieldMap domains = new FieldMap();

        for(FiroDomain domain: firoDomainMap.values()) {
            FieldMap domainMap = new FieldMap();
            domainMap.put("code", domain.getCode());
            domainMap.put("directUrl", domain.getDirectUrl());


            FieldMap firoCategoryMap = new FieldMap();
            domainMap.put("categoryMap", firoCategoryMap);
            for(FiroCategory category : domain.getAllCategory().values()) {
                FieldMap categoryMap = new FieldMap();
                categoryMap.put("code", category.getCode());
                categoryMap.put("directUrl", category.getDirectUrl());
                firoCategoryMap.put(category.getCode(), categoryMap);
            }
            domains.put(domain.getCode(), domainMap);
        }

        result.put("domainMap", domains);

        return new ResponseEntity<>(result, HttpStatus.OK);
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
            String filename = firoService.uploadTemp(file, FiroRegistry.get(refTarget, refTargetType), filterChain);

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

    @PostMapping(value = "/attach/{domainName}/{domainKey}")
    public ResponseEntity<Map<String, Object>> uploadSave(
        @PathVariable("domainName") String refType,
        @PathVariable("domainKey") Long refKey,
        @RequestBody AttachBag attachBag) throws Exception {

        List<FiroFile>  attachList = firoService.save(refKey, attachBag, LocalDateTime.now());


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
                                               @RequestParam(value = "domainKeyList", required = false) List<Long> domainKeyList,
                                               @RequestParam(value = "categoryList", required = false) List<String> categoryList) throws Exception{
        param.put("domainKeyList", domainKeyList);
        param.put("categoryList", categoryList);
        List<FiroFile> list = firoService.listAttach(param);
        return new ResponseEntity<List<FiroFile>>(list, HttpStatus.OK);
    }


    /**
     * GET  /attach/{refType}/{refKey} : 첨부 상세 조회
     *
     * @param refType 제품 아이디 (시퀀스키값)
     * @return 제품 객체
     */
    @GetMapping(value = "/attach/{domainName}/{domainKey}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttachBag> getAttach(HttpServletRequest request,
                                                     @PathVariable("domainName") String refType,
                                                     @PathVariable("domainKey") Long refKey) {

        return getAttach(request, refType, refKey, null, null);
    }


    /**
     * GET  /attach/{refType}/{refKey} : 첨부 상세 조회
     *
     * @param refType 제품 아이디 (시퀀스키값)
     * @return 제품 객체
     */
    @GetMapping(value = "/attach/{domainName}/{domainKey}/{categoryName}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AttachBag> getAttach(HttpServletRequest request,
                                                     @PathVariable("domainName") String refType,
                                                     @PathVariable("domainKey") Long refKey,
                                                     @PathVariable("categoryName") String categoryName,
                                                     @RequestParam(value="q", required = false) Map<String, Object> param) {
        if(param != null && param.containsKey("domainKeys")){
            refKey = null;
        }

        AttachBag bag = firoService.getAttachBagByRef(refType, refKey, categoryName, param);

        return Optional.ofNullable(bag)
            .map(product -> new ResponseEntity<>(product, HttpStatus.OK))
            .orElse(
                new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
            );
    }

    @PostMapping(value = "/direct-url",
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getAttach(@RequestBody DirectUrlParam param) throws Exception {

        return new ResponseEntity<>(FiroUtil.directUrl(param.getDomain(), param.getCategory(), param.getDomainId(), param.getCreatedDt(), param.getIndex()), HttpStatus.OK);
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

    @Data
    static class DirectUrlParam {
        String domain;
        String category;
        String domainId;
        LocalDateTime createdDt;
        int index;
    }

}
