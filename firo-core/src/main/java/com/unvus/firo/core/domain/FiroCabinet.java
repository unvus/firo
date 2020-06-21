package com.unvus.firo.core.domain;

import com.unvus.firo.core.filter.FiroFilterChain;
import lombok.Data;

@Data
public class FiroCabinet {
    private String roomCode;

    private String cabinetCode;

    private FiroFilterChain filterChain;
}
