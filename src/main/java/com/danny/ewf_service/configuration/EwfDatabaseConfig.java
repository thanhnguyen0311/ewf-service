package com.danny.ewf_service.configuration;


import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.danny.ewf_service.repository", // EWF repositories package
        entityManagerFactoryRef = "ewfEntityManagerFactory",
        transactionManagerRef = "ewfTransactionManager"
)

public class EwfDatabaseConfig {
    @Primary  // Marks this as the default datasource
    @Bean(name = "ewfDataSource")
    public DataSource ewfDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://ewf-db-do-user-19202114-0.k.db.ondigitalocean.com:25060/ewf?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC")
                .username("doadmin")
                .password("AVNS_xa9U9s11Y893qTAmtph")
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Primary
    @Bean(name = "ewfEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean ewfEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("ewfDataSource") DataSource ewfDataSource) {
        return builder
                .dataSource(ewfDataSource)
                .packages("com.danny.ewf_service.entity")  // EWF entities package
                .persistenceUnit("ewf")
                .build();
    }

    @Primary
    @Bean(name = "ewfTransactionManager")
    public PlatformTransactionManager ewfTransactionManager(
            @Qualifier("ewfEntityManagerFactory") EntityManagerFactory ewfEntityManagerFactory) {
        return new JpaTransactionManager(ewfEntityManagerFactory);
    }
}
