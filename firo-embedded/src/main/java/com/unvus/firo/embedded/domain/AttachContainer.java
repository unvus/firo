package com.unvus.firo.embedded.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class AttachContainer extends HashMap<String, AttachBag> implements Serializable {

    public AttachContainer() {
    }
}
