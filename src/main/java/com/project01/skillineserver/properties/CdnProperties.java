package com.project01.skillineserver.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "app.cdn")
public class CdnProperties {

    private String domain;
    private String keyPairId;
    private String privateKeyPath;
    private String cookieDomain;
}
