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

package org.opensingular.requirement.module.builder;

import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.requirement.commons.service.RequirementSender;

public class SingularRequirementDefinitionForms {

    private SingularRequirementBuilderContext builderContext;

    SingularRequirementDefinitionForms(SingularRequirementBuilderContext builderContext) {
        this.builderContext = builderContext;
    }

    public SingularRequirementDefinitionFlows allowedFlow(Class<? extends FlowDefinition> flowClass) {
        return new SingularRequirementDefinitionFlows(builderContext.addFlowClass(flowClass));
    }

    public SingularRequirementDefinitionForms requirementSenderBeanClass(Class<? extends RequirementSender> requirementSender) {
        builderContext.setRequirementSenderBeanClass(requirementSender);
        return this;
    }
}