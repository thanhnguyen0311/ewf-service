package com.danny.ewf_service.configuration;

import jakarta.persistence.EntityManagerFactory;
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
        basePackages = "com.danny.ewf_service.wms.repository", // Replace with the package where WMS repositories are located
        entityManagerFactoryRef = "wmsEntityManagerFactory",
        transactionManagerRef = "wmsTransactionManager"
)

public class WmsDatabaseConfig {
    @Bean(name = "wmsDataSource")
    public DataSource wmsDataSource() {
        return DataSourceBuilder.create()
                .url("jdbc:mysql://ewf-db-do-user-19202114-0.k.db.ondigitalocean.com:25060/wms?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC")
                .username("doadmin")
                .password("AVNS_xa9U9s11Y893qTAmtph")
                .driverClassName("com.mysql.cj.jdbc.Driver")
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

