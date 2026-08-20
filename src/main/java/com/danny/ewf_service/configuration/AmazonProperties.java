package com.danny.ewf_service.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.amz")
@Getter
@Setter
public class AmazonProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
