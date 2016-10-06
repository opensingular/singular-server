package org.opensingular.singular.server.commons.spring;

import org.opensingular.form.context.SingularFormContext;
import org.opensingular.singular.form.spring.SpringServiceRegistry;
import org.opensingular.singular.form.wicket.SingularFormConfigWicket;
import org.opensingular.singular.form.wicket.SingularFormConfigWicketImpl;
import org.opensingular.singular.persistence.service.ProcessRetrieveService;
import org.opensingular.singular.support.spring.util.AutoScanDisabled;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@EnableWebMvc
@EnableWebSecurity
@Configuration
@ComponentScan(
        basePackages = {"org.opensingular.singular"},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        value = AutoScanDisabled.class)
        })

public class SingularServerSpringAppConfig  {

    @Bean
    public SpringServiceRegistry getSpringServiceRegistry() {
        return new SpringServiceRegistry();
    }

    @Bean
    public SingularFormConfigWicket getSingularFormConfig(SpringServiceRegistry springServiceRegistry) {
        SingularFormConfigWicket singularFormConfigWicket = new SingularFormConfigWicketImpl();
        singularFormConfigWicket.setServiceRegistry(springServiceRegistry);
        return singularFormConfigWicket;
    }

    @Bean
    public SingularFormContext getSingularFormContext(SingularFormConfigWicket singularFormConfigWicket) {
        return singularFormConfigWicket.createContext();
    }

    @Bean
    public ProcessRetrieveService getProcessRetrieveService(SessionFactory sessionFactory) {
        ProcessRetrieveService processRetrieveService = new ProcessRetrieveService();
        processRetrieveService.setSessionLocator(sessionFactory::getCurrentSession);
        return processRetrieveService;
    }

}
