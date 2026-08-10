package com.danny.ewf_service.configuration;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.datasource")
@Getter
@Setter
public class EwfDatasourceProperties {

    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
