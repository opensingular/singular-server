/*
 * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensingular.requirement.module.wicket;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RuntimeConfigurationType;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AbstractAuthenticatedWebSession;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.resource.caching.NoOpResourceCachingStrategy;
import org.apache.wicket.resource.CssUrlReplacer;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.time.Duration;
import org.opensingular.internal.lib.wicket.test.WicketSerializationDebugUtil;
import org.opensingular.lib.commons.base.SingularProperties;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.lib.wicket.util.application.SingularAnnotatedMountScanner;
import org.opensingular.lib.wicket.SingularWebResourcesFactory;
import org.opensingular.lib.wicket.util.template.admin.SingularAdminApp;
import org.opensingular.lib.wicket.util.template.admin.SingularAdminTemplate;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.spring.security.AbstractSingularSpringSecurityAdapter;
import org.opensingular.requirement.module.spring.security.config.LoginPage;
import org.opensingular.requirement.module.wicket.error.Page403;
import org.opensingular.requirement.module.wicket.error.Page410;
import org.opensingular.requirement.module.wicket.listener.SingularRequirementContextListener;
import org.opensingular.requirement.module.wicket.view.behavior.SingularJSBehavior;
import org.opensingular.requirement.module.wicket.view.template.Footer;
import org.opensingular.requirement.module.wicket.view.template.Header;
import org.opensingular.requirement.module.workspace.WorkspaceRegistry;
import org.springframework.context.ApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.opensingular.lib.wicket.util.template.SingularTemplate.JAVASCRIPT_DECORATOR;
import static org.opensingular.lib.wicket.util.util.WicketUtils.$b;

public abstract class SingularRequirementApplication extends AuthenticatedWebApplication
        implements SingularAdminApp {
    public static SingularRequirementApplication get() {
        return (SingularRequirementApplication) WebApplication.get();
    }

    @Override
    public void init() {
        super.init();

        createMountPageForLogin();

        setPageManagerProvider(new RequirementPageManagerProvider(this));
        getStoreSettings().setMaxSizePerSession(Bytes.megabytes(20));

        getRequestCycleSettings().setTimeout(Duration.minutes(5));
        getRequestCycleListeners().add(new SingularRequirementContextListener());

        Locale.setDefault(new Locale("pt", "BR"));//NOSONAR

        getApplicationSettings().setAccessDeniedPage(Page403.class);
        getApplicationSettings().setPageExpiredErrorPage(Page410.class);

        // Don't forget to check your Application server for this
        getApplicationSettings().setDefaultMaximumUploadSize(Bytes.megabytes(10));

        getMarkupSettings().setStripWicketTags(true);
        getMarkupSettings().setStripComments(true);
        getMarkupSettings().setDefaultMarkupEncoding(StandardCharsets.UTF_8.name());
        getComponentOnConfigureListeners().add(component -> {
            boolean outputId = !component.getRenderBodyOnly();
            component.setOutputMarkupId(outputId).setOutputMarkupPlaceholderTag(outputId);
        });


        getComponentInstantiationListeners().add(new SpringComponentInjector(this, getApplicationContext(), true));


        new SingularAnnotatedMountScanner().mountPages(this);
        if (RuntimeConfigurationType.DEVELOPMENT == getConfigurationType()) {
            getDebugSettings().setComponentPathAttributeName("wicketdebug");
            WicketSerializationDebugUtil.configurePageSerializationDebug(this, this.getClass());
        }

        setHeaderResponseDecorator(JAVASCRIPT_DECORATOR);

        final SingularWebResourcesFactory singularWebResourcesFactory
                = getApplicationContext().getBean(SingularWebResourcesFactory.class);
        getSharedResources().add("logo", singularWebResourcesFactory.getLogo());
        getSharedResources().add("favicon", singularWebResourcesFactory.getFavicon());
        getResourceSettings().setCssCompressor(new CssUrlReplacer());
        getResourceSettings().setCachingStrategy(new NoOpResourceCachingStrategy());
        getJavaScriptLibrarySettings().setJQueryReference(singularWebResourcesFactory.getJQuery());
    }

    /**
     * Method responsible for create the MountPage for LoginPage.
     * This will create a login for each Security config that extends <code>AbstractSingularSpringSecurityAdapter</code>
     *
     * @see AbstractSingularSpringSecurityAdapter
     */
    private void createMountPageForLogin() {
        ApplicationContextProvider.get().getBean(WorkspaceRegistry.class)
                .getContexts()
                .parallelStream()
                .filter(this::findLoginPageByContext)
                .forEach(c -> mountPage("/login", getLoginPage()));
    }

    /**
     * Method will return verify if IServerContext is the same of the current Context,
     * if the Spring Security Config is a <code> AbstractSingularSpringSecurityAdapter</code>,
     * and the Login page has been configurated.
     *
     * @param serverContext
     * @return True if find the login Page config.
     */
    private boolean findLoginPageByContext(IServerContext serverContext) {
        return isSingularSpringSecurity(serverContext)
                && equalsCurrentContext(serverContext)
                && getLoginPage() != null;
    }

    /**
     * Verify if the ServerContext is a <code>AbstractSingularSpringSecurityAdapter</code>
     *
     * @param serverContext
     * @return True if is AbstractSingularSpringSecurityAdapter, false if not.
     */
    private boolean isSingularSpringSecurity(IServerContext serverContext) {
        return serverContext.getSettings().getSpringSecurityConfigClass() != null
                && AbstractSingularSpringSecurityAdapter.class.isAssignableFrom(serverContext.getSettings().getSpringSecurityConfigClass());
    }

    /**
     * Verify if serverContext is the same of the currentContext.
     *
     * @param serverContext
     * @return True if the serverContext is the same of the current context.
     */
    private boolean equalsCurrentContext(IServerContext serverContext) {
        return getWicketFilter().getFilterPath() != null && serverContext.getSettings().getUrlPath() != null
                && serverContext.getSettings().getUrlPath().replaceAll("/", "").equals(getWicketFilter().getFilterPath().replaceAll("/", ""));
    }

    /**
     * Method to override the login page.
     * For login work's fine, is necessery to config others things.
     * For more info look the method #findLoginPageByContext.
     *
     * @return Login page.
     * @see #findLoginPageByContext
     */
    protected Class<? extends WebPage> getLoginPage() {
        return LoginPage.class;
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new SingularSession(request, response);
    }

    @Override
    protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass() {
        return SingularSession.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<? extends WebPage> getSignInPageClass() {
        return (Class<? extends WebPage>) getHomePage();
    }

    @Override
    public RuntimeConfigurationType getConfigurationType() {
        if (SingularProperties.get().isTrue(SingularProperties.SINGULAR_WICKET_DEBUG_ENABLED)) {
            return RuntimeConfigurationType.DEVELOPMENT;
        } else {
            return RuntimeConfigurationType.DEPLOYMENT;
        }
    }

    public ApplicationContext getApplicationContext() {
        return ApplicationContextProvider.get();
    }

    @Override
    public TransparentWebMarkupContainer buildPageBody(String id, boolean withMenu, SingularAdminTemplate adminTemplate) {
        TransparentWebMarkupContainer pageBody = new TransparentWebMarkupContainer(id);
        if (!withMenu) {
            pageBody.add($b.classAppender("page-full-width"));
        }
        pageBody.add(new SingularJSBehavior());
        return pageBody;
    }

    @Override
    public MarkupContainer buildPageFooter(String id) {
        return new Footer(id);
    }

    @Override
    public MarkupContainer buildPageHeader(String id, boolean withMenu, SingularAdminTemplate adminTemplate) {
        return new Header(id, withMenu);
    }
}
