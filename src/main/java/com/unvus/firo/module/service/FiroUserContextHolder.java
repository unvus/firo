package com.unvus.firo.module.service;

public class FiroUserContextHolder {
    public static ThreadLocal<String> createdBy = new ThreadLocal<>();
}
