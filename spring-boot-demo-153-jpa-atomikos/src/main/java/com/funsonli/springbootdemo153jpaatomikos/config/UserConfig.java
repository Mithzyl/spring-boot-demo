package com.funsonli.springbootdemo153jpaatomikos.config;

import com.mysql.cj.jdbc.MysqlXADataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@DependsOn("transactionManager")
@EnableJpaRepositories(
        entityManagerFactoryRef = "userEntityManager",
        transactionManagerRef = "transactionManager",
        basePackages = {"com.funsonli.springbootdemo153jpaatomikos.dao.user"})

public class UserConfig {
    @Autowired
    private JpaVendorAdapter jpaVendorAdapter;

    //user库
    @Primary
    @Bean(name = "userDataSourceProperties")
    @Qualifier("userDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource.user")
    public DataSourceProperties userDataSourceProperties() {
        return new DataSourceProperties();
    }


    @Primary
    @Bean(name = "userDataSource", initMethod = "init", destroyMethod = "close")
    @ConfigurationProperties(prefix = "spring.datasource.user")
    public DataSource userDataSource() throws SQLException {
        MysqlXADataSource mysqlXaDataSource = new MysqlXADataSource();
        mysqlXaDataSource.setUrl(userDataSourceProperties().getUrl());
        mysqlXaDataSource.setPinGlobalTxToPhysicalConnection(true);
        mysqlXaDataSource.setPassword(userDataSourceProperties().getPassword());
        mysqlXaDataSource.setUser(userDataSourceProperties().getUsername());
        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(mysqlXaDataSource);
        xaDataSource.setUniqueResourceName("xadatabase1");
        xaDataSource.setBorrowConnectionTimeout(60);
        xaDataSource.setMaxPoolSize(20);
        return xaDataSource;

    }

    @Primary
    @Bean(name = "userEntityManager")
    @DependsOn("transactionManager")
    public LocalContainerEntityManagerFactoryBean userEntityManager() throws Throwable {

        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("hibernate.transaction.jta.platform", AtomikosJtaPlatform.class.getName());
        properties.put("javax.persistence.transactionType", "JTA");
        LocalContainerEntityManagerFactoryBean entityManager = new LocalContainerEntityManagerFactoryBean();
        entityManager.setJtaDataSource(userDataSource());
        entityManager.setJpaVendorAdapter(jpaVendorAdapter);
        entityManager.setPackagesToScan("com.funsonli.springbootdemo153jpaatomikos.entity.user");
        entityManager.setPersistenceUnitName("userPersistenceUnit");
        entityManager.setJpaPropertyMap(properties);
        return entityManager;
    }
}