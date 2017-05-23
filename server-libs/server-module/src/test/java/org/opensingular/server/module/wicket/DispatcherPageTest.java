package org.opensingular.server.module.wicket;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opensingular.form.wicket.helpers.SingularWicketTester;
import org.opensingular.server.commons.form.FormAction;
import org.opensingular.server.commons.spring.security.AuthorizationService;
import org.opensingular.server.commons.test.CommonsApplicationMock;
import org.opensingular.server.commons.test.SingularCommonsBaseTest;
import org.opensingular.server.commons.test.SingularServletContextTestExecutionListener;
import org.opensingular.server.commons.wicket.error.AccessDeniedPage;
import org.opensingular.server.commons.wicket.view.form.FormPage;
import org.opensingular.server.module.wicket.view.util.dispatcher.DispatcherPage;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestExecutionListeners;

import javax.inject.Inject;import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.opensingular.server.commons.wicket.view.util.ActionContext.ACTION;
import static org.opensingular.server.commons.wicket.view.util.ActionContext.FORM_NAME;

@TestExecutionListeners(listeners = {SingularServletContextTestExecutionListener.class}, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
public class DispatcherPageTest extends SingularCommonsBaseTest {


    @Inject
    CommonsApplicationMock singularApplication;

    private SingularWicketTester tester;

    @Inject
    private AuthorizationService authorizationService;

    @Before
    public void setUp() {
        reset(authorizationService);
    }

    @WithUserDetails("vinicius.nunes")
    @Test
    public void accessDenied() {
        tester = new SingularWicketTester(singularApplication);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(ACTION, FormAction.FORM_ANALYSIS.getId());
        pageParameters.add(FORM_NAME, "foooooo.StypeFoo");
        tester.startPage(DispatcherPage.class, pageParameters);
        tester.assertRenderedPage(AccessDeniedPage.class);
    }

    @WithUserDetails("vinicius.nunes")
    @Test
    @Ignore
    public void accessGranted() {
        when(authorizationService.hasPermission(any(), any(), any(), any())).thenReturn(true);
        tester = new SingularWicketTester(singularApplication);
        PageParameters pageParameters = new PageParameters();
        pageParameters.add(ACTION, FormAction.FORM_ANALYSIS.getId());
        pageParameters.add(FORM_NAME, "foooooo.StypeFoo");
        tester.startPage(DispatcherPage.class, pageParameters);
        tester.assertRenderedPage(FormPage.class);
    }


}
