package com.roy.football.match.jpa.configure;

import java.util.Properties;

import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mysema.query.jpa.impl.JPAQueryFactory;

@Configuration
@EnableJpaRepositories(basePackages = "com.roy.football.match.jpa.repositories")
@EnableTransactionManagement
public class MatchJpaConfiguration {
	@Autowired
    private Environment environment;

	@Bean(name="jpaDataSource")
    public DataSource jpaDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl(environment.getProperty("match.mysql.url"));
        dataSource.setUsername(environment.getProperty("match.mysql.user"));
        dataSource.setPassword(environment.getProperty("match.mysql.password"));
        
        Properties properties = new Properties();
        properties.setProperty("useSSL", "false");
        properties.setProperty("autoReconnect", "true");
        dataSource.setConnectionProperties(properties);
        return dataSource;
    }

	@Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(true);
        hibernateJpaVendorAdapter.setGenerateDdl(true);
        hibernateJpaVendorAdapter.setDatabase(Database.MYSQL);
        return hibernateJpaVendorAdapter;
    }
	
	@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
    		@Qualifier("jpaDataSource") DataSource dataSource,
    		JpaVendorAdapter jpaVendorAdapter) {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setPackagesToScan("com.roy.football.match.jpa.entities");
		factoryBean.setJpaVendorAdapter(jpaVendorAdapter);
		factoryBean.setPersistenceUnitName("match");
		Properties jpaProperties = new Properties();
		jpaProperties.setProperty("hibernate.id.new_generator_mappings", "false");
		jpaProperties.setProperty("hibernate.show_sql", "false");
		factoryBean.setJpaProperties(jpaProperties);
		return factoryBean;
    }
	
	@Bean
	public JpaTransactionManager transactionManager (
			@Qualifier(value = "entityManagerFactory")
			EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
	
	@Bean
	public JPAQueryFactory jpaQueryFactory(
			@Qualifier(value = "entityManagerFactory")
	        Provider<EntityManager> entityManager) {
	    return new JPAQueryFactory(entityManager);
	}

}
