package org.opensingular.server.commons.config;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.server.commons.test.SingularServerBaseTest;
import org.opensingular.server.commons.test.TestInitializer;

import javax.inject.Inject;
import javax.servlet.ServletException;

public class ConfigTest extends SingularServerBaseTest {

    @Inject
    public SingularServerConfiguration singularServerConfiguration;

    @Test
    public void checkServletParams() throws ServletException {

        Assert.assertEquals(singularServerConfiguration.getContexts().length, 3);
        Assert.assertNotNull(singularServerConfiguration.getDefaultPublicUrls());
        Assert.assertArrayEquals(singularServerConfiguration.getDefinitionsPackages(), TestInitializer.DEFINITIONS_PACKS_ARRAY);
        Assert.assertNotNull(singularServerConfiguration.getFormTypes());
        Assert.assertEquals(singularServerConfiguration.getProcessGroupCod(), TestInitializer.TESTE);
        Assert.assertEquals(singularServerConfiguration.getSpringMVCServletMapping(), TestInitializer.SPRING_MVC_SERVLET_MAPPING);
        singularServerConfiguration.setAttribute("teste", "teste");
        Assert.assertEquals(singularServerConfiguration.getAttribute("teste"), "teste");

    }
}
