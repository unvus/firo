package com.unvus.firo.config;

import com.unvus.firo.module.adapter.Adapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@EnablePluginRegistries({
    Adapter.class
})
@Configuration
public class PluginConfiguration {
}
