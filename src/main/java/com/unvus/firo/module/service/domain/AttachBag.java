package com.unvus.firo.module.service.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class AttachBag extends HashMap<String, List<FiroFile>> implements Serializable {

    private String domainCode;


    public AttachBag() {
    }

    public AttachBag(String domainCode) {
        this.domainCode = domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public FiroFile one(String mapCode) {
        List<FiroFile> attachList = get(mapCode);
        if (attachList != null && attachList.size() > 0) {
            return attachList.get(0);
        }
        return null;
    }

    public FiroFile one() {
        List<FiroFile> attachList = getFirst();
        if (attachList != null && attachList.size() > 0) {
            return attachList.get(0);
        }
        return null;
    }

    public List<FiroFile> getFirst() {
        Optional<List<FiroFile>> firstElement = this.values().stream().findFirst();
        return firstElement.orElse(null);
    }
}
