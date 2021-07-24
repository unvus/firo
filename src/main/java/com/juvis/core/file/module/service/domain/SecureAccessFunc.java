package com.juvis.core.file.module.service.domain;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface SecureAccessFunc {
    boolean accept(HttpServletRequest request, FiroFile firoFile);
}
