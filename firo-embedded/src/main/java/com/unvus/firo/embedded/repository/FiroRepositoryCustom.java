package com.unvus.firo.embedded.repository;

import com.unvus.firo.embedded.domain.FiroFile;

import java.util.List;
import java.util.Map;

public interface FiroRepositoryCustom {

    List<FiroFile> listAttach(Map<String, Object> params);
}
