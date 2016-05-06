package br.net.mirante.singular.server.commons.config;

import br.net.mirante.singular.server.commons.spring.SingularDefaultBeanFactory;
import br.net.mirante.singular.server.commons.spring.SingularDefaultPersistenceConfiguration;
import br.net.mirante.singular.server.commons.spring.SingularServerSpringAppConfig;
import br.net.mirante.singular.server.commons.spring.SpringFoxSwaggerConfig;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Optional;

public class SpringHibernateInitializer {

    protected static final String SPRING_MVC_DISPATCHER_SERVLET = "Spring MVC Dispatcher Servlet";

    public SpringHibernateInitializer() {

    }

    public AnnotationConfigWebApplicationContext init(ServletContext ctx) {
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(springConfigurationClass());
        Optional.ofNullable(swaggerConfig()).ifPresent(applicationContext::register);
        applicationContext.register(beanFactory());
        Optional.ofNullable(persistenceConfiguration()).ifPresent(applicationContext::register);
        addSpringContextListener(ctx, applicationContext);
        addSpringRequestContextListener(ctx, applicationContext);
        addSpringMVCServlet(ctx, applicationContext);
        return applicationContext;
    }

    protected Class<? extends SpringFoxSwaggerConfig> swaggerConfig() {
        return SpringFoxSwaggerConfig.class;
    }

    protected void addSpringContextListener(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        ctx.addListener(new ContextLoaderListener(applicationContext));
    }

    protected void addSpringRequestContextListener(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        ctx.addListener(RequestContextListener.class);
    }

    protected void addSpringMVCServlet(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        //Servlet
        ServletRegistration.Dynamic dispatcher = ctx.addServlet(SPRING_MVC_DISPATCHER_SERVLET, new DispatcherServlet(applicationContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping(springMVCServletMapping());
    }

    /**
     * Fornece a classe que será utilizada como configuração java do Spring.
     * A classe fornecida deve herdar de {@link SingularServerSpringAppConfig} e deve
     * ser anotada com {@link org.springframework.context.annotation.Configuration}.
     * As principais configurações do pet server são feitas pela superclasse bastando declarar
     * na classe informada apenas as configurações e beans do spring específicos da aplicação
     *
     * @return Uma classe concreta que herda de {@link SingularServerSpringAppConfig} e anotada com {@link org.springframework.context.annotation.Configuration}
     */

    protected Class<? extends SingularServerSpringAppConfig> springConfigurationClass() {
        return SingularServerSpringAppConfig.class;
    }


    protected Class<? extends SingularDefaultBeanFactory> beanFactory() {
        return SingularDefaultBeanFactory.class;
    }

    protected Class<? extends SingularDefaultPersistenceConfiguration> persistenceConfiguration() {
        return SingularDefaultPersistenceConfiguration.class;
    }

    protected String springMVCServletMapping() {
        return "/*";
    }

}
