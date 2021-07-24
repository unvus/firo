package com.juvis.core.file.config;

import com.juvis.core.file.module.adapter.Adapter;
import org.springframework.context.annotation.Configuration;
import org.springframework.plugin.core.config.EnablePluginRegistries;

@EnablePluginRegistries({
    Adapter.class
})
@Configuration
public class PluginConfiguration {
}
