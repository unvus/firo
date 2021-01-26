package com.unvus.firo.embedded.channel.adapter;

import com.unvus.firo.embedded.channel.endpoint.AdapterProps;
import com.unvus.firo.embedded.channel.endpoint.enums.EndpointType;
import org.springframework.plugin.core.Plugin;

import java.io.File;

public interface Adapter extends Plugin<EndpointType>  {


    void upload(AdapterProps props, File input, String toPath) throws Exception;
}
