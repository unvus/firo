package com.unvus.firo.embedded.service;

import com.unvus.firo.core.FiroRegistry;
import com.unvus.firo.core.domain.FiroCabinet;
import com.unvus.firo.core.domain.FiroRoom;
import com.unvus.firo.core.filter.FiroFilterChain;
import com.unvus.firo.embedded.domain.AttachBag;
import com.unvus.firo.embedded.domain.FiroFile;
import com.unvus.firo.embedded.repository.FiroRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FiroService {

    public final static String CACHE_LIST_ATTACH_BY_REF = "AttachService.listAttachByRef";

//    private final PluginRegistry<Adapter, EndpointType> adapterPluginRegistry;

    public final static String DELETED_MAP_CODE = "_deleted";

    private final FiroRepository firoRepository;

    private static Tika tika = new Tika();

    public FiroService(FiroRepository attachRepository) {
        this.firoRepository = attachRepository;
    }


    /**
     * 파일 업로드 프로세스 중 첫번째 단계인 <em>"임시파일 업로드"</em> 처리<br/>
     * 대략의 파일 업로드 프로세스
     * <ol>
     *     <li>임시 파일로 업로드</li>
     *     <li>uuid 반환</li>
     *     <li>form submit 시 uuid 전송</li>
     * </ol>
     *
     * @param file  첨부파일 객체
     * @param cabinet <code>ReferenceTypeRegistry</code> 으로 부터 얻은 firoCabinet 객체
     * @param filterChain 적용할 filterChain. 없으면 최초 설정(attach.yml)에 저장된 filterChain 사용
     * @return uuid
     * @throws Exception
     */
    public String uploadTemp(MultipartFile file, FiroCabinet cabinet, FiroFilterChain filterChain) throws Exception {

        if (!file.isEmpty()) {
            InputStream is = file.getInputStream();

            try {
                String uuid = UUID.randomUUID().toString();

                File temp = cabinet.writeTemp(uuid, is);

                try {
                    if(filterChain == null || filterChain.size() == 0) {
                        if (cabinet.getFilterChain() != null && cabinet.getFilterChain().size() > 0) {
                            cabinet.getFilterChain().startFilter(temp);
                        }
                    }else {
                        filterChain.startFilter(temp);
                    }
                }catch(Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e.getMessage());
                }

                return uuid;
            } catch (IOException |RuntimeException e) {
                throw new Exception("Failed to upload " + file.getOriginalFilename() + " => " + e.getMessage());
            }finally {
                IOUtils.closeQuietly(is);
            }
        } else {
            throw new FileNotFoundException("Failed to upload " + file.getOriginalFilename() + " because it was empty");
        }
    }

    @Transactional
    public List<FiroFile> save(Long roomKey, AttachBag bag) throws Exception {
        return save(roomKey, bag, false);
    }

    @Transactional
    public List<FiroFile> save(Long roomKey, AttachBag bag, boolean cleanRoom) throws Exception {
        List<FiroFile> newAttachList = new ArrayList();

        if(bag == null || bag.isEmpty()) {
            return newAttachList;
        }

        boolean cleared = false;

        for(Map.Entry<String, List<FiroFile>> entry: bag.entrySet()) {
            String cabinetCode = entry.getKey();

            FiroCabinet cabinet = FiroRegistry.get(bag.getRoomCode(), cabinetCode);

            if(StringUtils.equals(cabinetCode, DELETED_MAP_CODE)) {
                deleteAttachPermanently(entry.getValue());
            }else {
                if(entry.getValue() == null ) {
                    continue;
                }

                int index = 1;
                for(FiroFile attach : entry.getValue()) {
                    if(attach.getId() != null || attach.getSavedName() == null) {
                        continue;
                    }

                    if(cleanRoom && !cleared) {
                        clearAttachByRoom(bag.getRoomCode(), roomKey);
                        cleared = true;
                    }

                    File inputFile = cabinet.readTemp(attach.getSavedName());

                    FiroFile newAttach = persistFile(cabinet, roomKey, index++, attach.getDisplayName(), inputFile, attach.getExt());

                    newAttachList.add(newAttach);
                }
            }
        }

        return newAttachList;
    }

    @Transactional
    public void copy(FiroRoom fileRoom, Long roomKey, Long newRoomKey, AttachBag bag, List<Long> ids) throws Exception {

        Map<String, Object> param = new HashMap<>();

        if(CollectionUtils.isNotEmpty(ids)) {
            param.put("ids", ids);
        }else {
            param.put("refTarget", fileRoom.getCode());
            param.put("refTargetKey", roomKey);
        }

        List<FiroFile> attachList = firoRepository.listAttach(param);

        List<Long> deletedIdList = new ArrayList();

        if(bag != null) {
            List<FiroFile> deletedList = bag.get(DELETED_MAP_CODE);

            if(deletedList != null) {
                deletedIdList = deletedList.stream()
                    .map(FiroFile::getId).collect(Collectors.toList());
            }

            bag.remove(DELETED_MAP_CODE);
        }

        int index = 1;
        for(FiroFile attach : attachList) {
            if(deletedIdList.contains(attach.getId())) {
                continue;
            }
            try {
                String cabinetCode = attach.getRefTargetType();
                FiroCabinet cabinet = FiroRegistry.get(bag.getRoomCode(), cabinetCode);
                File inputFile = Paths.get(cabinet.getDirectoryPathPolicy().getBaseDir() + attach.getSavedDir(), attach.getSavedName()).toFile();
//                InputStream is = Files.newInputStream(Paths.get(directoryPathPolicy.getBaseDir() + attach.getSavedDir(), attach.getSavedName()));

                FiroFile newAttach = persistFile(cabinet, newRoomKey, index++, attach.getDisplayName(), inputFile, "10");
            }catch(Exception e) {
                log.error(e.getMessage(), e);
            }

        }

    }


    public FiroFile persistFile(FiroCabinet cabinet, Long refTargetKey, int index, String displayName, File inputFile, String ext) throws Exception {
        String saveDir = cabinet.getDirectoryPathPolicy().getFullDir(cabinet.getRoom().getCode(), cabinet.getCabinetCode());

        String extension = StringUtils.substringAfterLast(displayName, ".");

        String fileName = refTargetKey + "_" + cabinet.getCabinetCode() + "_" + index + "_"
            + RandomStringUtils.randomAlphanumeric(10)
            + "."
            + extension;

        String fileType = detectFile(inputFile);
        Long size = inputFile.length();

        cabinet.write(fileName, new FileInputStream(inputFile));

//        getAdapterInstance(null).upload();
        FiroFile attach = new FiroFile();
        attach.setRefTarget(cabinet.getRoom().getCode());
        attach.setRefTargetKey(refTargetKey);
        attach.setRefTargetType(cabinet.getCabinetCode());
        attach.setDisplayName(displayName);
        attach.setSavedName(fileName);
        attach.setSavedDir(StringUtils.removeStart(saveDir, cabinet.getDirectoryPathPolicy().getBaseDir()));
        attach.setFileSize(size);
        attach.setFileType(fileType);
        attach.setAccessCnt(0);
        attach.setExt(ext);
        attach.setCreatedDt(LocalDateTime.now());

        firoRepository.save(attach);

        return attach;
    }

    public String uploadEditorImage(MultipartFile upload, String roomCode) throws Exception {

        if(upload != null && upload.getSize() != 0) {

            String orgName = upload.getOriginalFilename();

            String extension = StringUtils.substringAfterLast(orgName, ".");

            if(StringUtils.isEmpty(extension)) {
                extension = getFormatName(detectFile(upload.getInputStream()));
            }

            String fileName = UUID.randomUUID().toString() + "." + extension;

            FiroCabinet cabinet = FiroRegistry.get(roomCode, FiroRegistry._DEFAULT_CABINET_NAME);
            cabinet.write(fileName, upload.getInputStream());

            return "/" + cabinet.getDirectoryPathPolicy().getSubDir() + fileName;
        }
        return null;
    }

    public void deleteAttach(List<FiroFile> attaches) throws Exception {
        deleteAttachPermanently(attaches);
        if(CollectionUtils.isNotEmpty(attaches)) {
            FiroFile attach = attaches.get(0);
        }
    }

    public FiroFile updateAttach(FiroFile attach) {
        firoRepository.save(attach);

        return attach;
    }


    private void deleteAttachPermanently(List<FiroFile> attaches) throws Exception {
        for(FiroFile attach : attaches) {
            FiroFile savedFiroFile = firoRepository.getOne(attach.getId());
            if(savedFiroFile != null) {
                if(StringUtils.isNoneBlank(savedFiroFile.getSavedDir()) && StringUtils.isNoneBlank(savedFiroFile.getSavedName())) {
                    FiroCabinet cabinet = FiroRegistry.get(savedFiroFile.getRefTarget(), savedFiroFile.getRefTargetType());
                    cabinet.delete(savedFiroFile.getSavedDir() + savedFiroFile.getSavedName());

                    firoRepository.delete(attach);
                }else {
                    throw new InvalidPathException("", "No path specified");
                }
            }
        }
    }


    /**
     *  상세 조회
     * @param id
     * @return Attach
     */
    @Transactional(readOnly = true)
    public FiroFile getAttach(Long id) {
        return firoRepository.getOne(id);
    }

    /**
     *  목록 조회
     * @param param
     * @return
     */
    @Transactional(readOnly = true)
    public List<FiroFile> listAttach(Map<String, Object> param) {
        List<FiroFile> result = firoRepository.listAttach(param);

        return result;
    }

    @Transactional(readOnly = true)
    public long listAttachCntByRef(String refTarget, Long refTargetKey, String refTargetType) {

        Map<String, Object> fieldMap = new HashMap<>();
        return firoRepository.listAttachCnt(fieldMap);
    }

    public List<FiroFile> listAttachByRef(String refTarget, Long refTargetKey, String refTargetType) {
        return listAttachByRef(refTarget, refTargetKey, refTargetType, null);
    }

    public List<FiroFile> listAttachByRef(String refTarget, Long refTargetKey, String refTargetType, String ext, String fmetaValue) {
        Map<String, Object> param = new HashMap();
        param.put("refTarget", refTarget);
        param.put("refTargetKey", refTargetKey);
        param.put("refTargetType", refTargetType);
        param.put("ext", ext);
        param.put("fmetaValue", fmetaValue);
        List<FiroFile> list = firoRepository.listAttach(param);

        return list;
    }
    public List<FiroFile> listAttachByRef(String refTarget, Long refTargetKey, String refTargetType, String ext) {
        return listAttachByRef(refTarget, refTargetKey, refTargetType, ext, null);
    }

    public AttachBag getAttachBagByRef(String refTarget, Long refTargetKey, String refTargetType) {
        return getAttachBagByRef(refTarget, refTargetKey, refTargetType, new HashMap<>());
    }

    public AttachBag getAttachBagByRef(String refTarget, Long refTargetKey, String refTargetType, Map<String, Object> param) {
        if(param == null) {
            param = new HashMap<>();
        }
        param.put("refTarget", refTarget);
        param.put("refTargetKey", refTargetKey);
        if(StringUtils.isNotBlank(refTargetType)) {
            param.put("refTargetType", refTargetType);
        }
        List<FiroFile> list = firoRepository.listAttach(param);

        AttachBag bag = new AttachBag(refTarget);

        if(CollectionUtils.isNotEmpty(list)) {
            for(FiroFile attach : list) {
                if(!bag.containsKey(attach.getRefTargetType())) {
                    bag.put(attach.getRefTargetType(), new ArrayList<>());
                }
                bag.get(attach.getRefTargetType()).add(attach);
            }
        }

        return bag;
    }

    public Map<Long, AttachBag> getAttachBagMapByRef(String refTarget, List<Long> refTargetKeyList) {
        Map<String, Object> param = new HashMap();
        param.put("refTarget", refTarget);
        param.put("roomKeyList", refTargetKeyList);
        List<FiroFile> list = firoRepository.listAttach(param);

        Map<Long, AttachBag> result = new HashMap<>();

        for(FiroFile attach : list) {
            if(!result.containsKey(attach.getRefTargetKey())) {
                result.put(attach.getRefTargetKey(), new AttachBag(refTarget));
            }
            AttachBag bag = result.get(attach.getRefTargetKey());
            if(!bag.containsKey(attach.getRefTargetType())) {
                bag.put(attach.getRefTargetType(), new ArrayList<>());
            }
            bag.get(attach.getRefTargetType()).add(attach);
        }

        return result;
    }

    public int clearAttachByRoom(String refTarget, Long refTargetKey) throws Exception {
        List<FiroFile> attachList = listAttachByRef(refTarget, refTargetKey, null);
        deleteAttachPermanently(attachList);
        return 1;
    }


    private String getUniqueFileName(String path, String fileName) {
        File tmpFile = new File(path + fileName);
        File parentDir = tmpFile.getParentFile();
        int count = 1;
        String extension = FilenameUtils.getExtension(tmpFile.getName());
        String baseName = FilenameUtils.getBaseName(tmpFile.getName());
        String uniqueName = baseName + "_" + count++ + "_." + extension;
        while(tmpFile.exists()) {
            tmpFile = new File(parentDir, uniqueName);
        }
        return uniqueName;
    }

    /**
     * 이미지 구분
     * @param stream
     * @return
     * @throws Exception
     */
    public static String detectFile(InputStream stream) {

        String contentType = null;
        try {
            synchronized (tika) {
                contentType = tika.detect(stream);
            }
        }catch (Exception ignore){}
        return contentType;
    }

    /**
     * 이미지 구분
     * @param filename
     * @return
     * @throws Exception
     */
    public static String detectFile(String filename)  {

        String contentType = null;
        try {
            synchronized (tika) {
                contentType = tika.detect(filename);
            }
        }catch (Exception ignore){}
        return contentType;
    }

    public static String detectFile(File file)  {

        String contentType = null;
        try {
            synchronized (tika) {
                contentType = tika.detect(file);
            }
        }catch (Exception ignore){}
        return contentType;
    }

    private String getFormatName(String contentType) {
        if(StringUtils.indexOf(contentType, "jpeg") >= 0) {
            return "jpg";
        }else if(StringUtils.indexOf(contentType, "jpg") >= 0) {
            return "jpg";
        }else if(StringUtils.indexOf(contentType, "gif") >= 0) {
            return "gif";
        }else if(StringUtils.indexOf(contentType, "png") >= 0) {
            return "png";
        }
        return "jpg";
    }

//    private Adapter getAdapterInstance(EndpointType endpointType) {
//        Adapter adapter = adapterPluginRegistry.getPluginFor(endpointType).get();
//        return adapter;
//    }

}
