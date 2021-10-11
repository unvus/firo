package com.unvus.firo.module.service.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class AttachContainer extends HashMap<String, AttachBag> implements Serializable {

    public AttachContainer() {
    }
}
