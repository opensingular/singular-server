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
import org.opensingular.requirement.module.SingularModule;
import org.opensingular.requirement.module.workspace.WorkspaceRegistry;

import javax.inject.Inject;

public class ConfigTest extends SingularCommonsBaseTest {

    @Inject
    public SingularModule singularModule;

    @Inject
    public WorkspaceRegistry workspaceRegistry;

    @Test
    public void checkServletParams() {
        Assert.assertNotNull(singularModule);
        Assert.assertNotNull(workspaceRegistry);
        Assert.assertEquals(singularModule.abbreviation(), CommonsInitializerMock.TESTE);
        Assert.assertEquals(workspaceRegistry.getContexts().size(), 3);
    }
}
