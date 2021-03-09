package com.unvus.firo.module.service.repository;

import com.unvus.firo.module.service.domain.FiroFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FiroRepositoryCustom {

    List<FiroFile> listAttach(Map<String, Object> params);
    long listAttachCnt(Map<String, Object> params);
    List<FiroFile> listAttachByIds(String domain, String category, Collection<Long> refTargetKeyList);
}
