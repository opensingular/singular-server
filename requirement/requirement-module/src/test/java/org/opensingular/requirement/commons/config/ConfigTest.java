/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.requirement.commons.config;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.requirement.commons.CommonsInitializerMock;
import org.opensingular.requirement.commons.SingularCommonsBaseTest;
import org.opensingular.requirement.module.SingularModuleConfigurationBean;


import javax.inject.Inject;
import javax.servlet.ServletException;

public class ConfigTest extends SingularCommonsBaseTest {

    @Inject
    public SingularModuleConfigurationBean singularServerConfiguration;

    @Test
    public void checkServletParams() throws ServletException {

        Assert.assertEquals(singularServerConfiguration.getContexts().length, 3);
        Assert.assertNotNull(singularServerConfiguration.getPublicUrls());
        Assert.assertNotNull(singularServerConfiguration.getFormTypes());
        Assert.assertEquals(singularServerConfiguration.getModuleCod(), CommonsInitializerMock.TESTE);
        singularServerConfiguration.setAttribute("teste", "teste");
        Assert.assertEquals(singularServerConfiguration.getAttribute("teste"), "teste");

    }
}
