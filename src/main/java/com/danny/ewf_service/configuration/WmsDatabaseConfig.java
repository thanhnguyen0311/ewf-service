package com.danny.ewf_service.configuration;

import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.danny.ewf_service.wms.repository",
        entityManagerFactoryRef = "wmsEntityManagerFactory",
        transactionManagerRef = "wmsTransactionManager"
)


@AllArgsConstructor
public class WmsDatabaseConfig {

    @Autowired
    private final WmsDatasourceProperties wmsProperties;


    @Bean(name = "wmsDataSource")
    public DataSource wmsDataSource() {
        return DataSourceBuilder.create()
                .url(wmsProperties.getUrl())
                .username(wmsProperties.getUsername())
                .password(wmsProperties.getPassword())
                .driverClassName(wmsProperties.getDriverClassName())
                .build();
    }

    @Bean(name = "wmsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean wmsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("wmsDataSource") DataSource wmsDataSource) {
        return builder
                .dataSource(wmsDataSource)
                .packages("com.danny.ewf_service.wms.entity") // Replace with the package containing WMS entities like WmsLPN
                .persistenceUnit("wms")
                .build();
    }

    @Bean(name = "wmsTransactionManager")
    public PlatformTransactionManager wmsTransactionManager(
            @Qualifier("wmsEntityManagerFactory") EntityManagerFactory wmsEntityManagerFactory) {
        return new JpaTransactionManager(wmsEntityManagerFactory);
    }
}

