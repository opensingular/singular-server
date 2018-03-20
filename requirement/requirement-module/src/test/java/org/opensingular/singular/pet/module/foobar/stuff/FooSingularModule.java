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

package org.opensingular.singular.pet.module.foobar.stuff;

import org.opensingular.server.module.RequirementConfiguration;
import org.opensingular.server.module.SingularModule;
import org.opensingular.server.module.WorkspaceConfiguration;
import org.opensingular.server.commons.requirement.SingularRequirement;
import org.opensingular.server.module.requirement.builder.SingularRequirementBuilder;
import org.opensingular.server.module.workspace.DefaultDonebox;
import org.opensingular.server.module.workspace.DefaultInbox;

public class FooSingularModule implements SingularModule {

    private FooRequirement fooRequirement = new FooRequirement();

    @Override
    public String abbreviation() {
        return "GRUPO_TESTE";
    }

    @Override
    public String name() {
        return "Grupo Processo Teste";
    }

    @Override
    public void requirements(RequirementConfiguration config) {
        config
                .addRequirement(fooRequirement)
                .addRequirement(this::barRequirement);
    }

    @Override
    public void workspace(WorkspaceConfiguration config) {
        config
                .addBox(new DefaultInbox()).newFor(this::barRequirement)
                .addBox(new DefaultDonebox()).newFor(fooRequirement);
    }


    public SingularRequirement barRequirement(SingularRequirementBuilder builder) {
        return builder
                .name("Bar Requirement")
                .mainForm(STypeFoo.class)
                .allowedFlow(FooFlow.class)
                .build();
    }
}