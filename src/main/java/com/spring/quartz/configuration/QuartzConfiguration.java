package com.spring.quartz.configuration;

import com.spring.quartz.factory.AutoWiringSpringBeanJobFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class QuartzConfiguration {

    private final DataSource dataSource;

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext) {
        //Quartz Documentation and Github Information
        //https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#scheduling-quartz
        //http://www.quartz-scheduler.org/documentation/
        //https://github.com/quartz-scheduler/quartz/blob/d42fb7770f287afbf91f6629d90e7698761ad7d8/quartz-core/src/main/resources/org/quartz/impl/jdbcjobstore/tables_postgres.sql
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        Properties properties = new Properties();
        //http://www.quartz-scheduler.org/documentation/quartz-2.1.7/configuration/ConfigJDBCJobStoreClustering.html
        properties.setProperty("org.quartz.jobStore.isClustered", "false");
        //Check in every 3 seconds
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "3000");
        //Can be any string, but must be unique for all schedulers working as if they are the same ‘logical’ Scheduler within a cluster.
        //You may use the value “AUTO” as the instanceId if you wish the Id to be generated for you.
        //Or the value “SYS_PROP” if you want the value to come from the system property “org.quartz.scheduler.instanceId”.
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        //JobStoreTX manages all transactions itself by calling commit() (or rollback()) on the database connection after every action
        //(such as the addition of a job). JDBCJobStore is appropriate if you are using Quartz in a stand-alone application,
        //or within a servlet container if the application is not using JTA transactions.
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");

        schedulerFactoryBean.setQuartzProperties(properties);
        schedulerFactoryBean.setDataSource(dataSource);
        schedulerFactoryBean.setJobFactory(jobFactory);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");

        return schedulerFactoryBean;
    }
}
