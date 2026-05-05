package com.project01.skillineserver.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@ConfigurationProperties(prefix = "app.cdn")
public class CdnProperties {

    private String domain;
    @Getter
    private String keyPairId;
    @Getter
    private String privateKeyPath;
    @Getter
    private String cookieDomain;

    public String getDomain() {
        if (domain == null) return null;
        if (domain.startsWith("https://") || domain.startsWith("http://")) {
            return domain;
        }
        return "https://" + domain;
    }

}