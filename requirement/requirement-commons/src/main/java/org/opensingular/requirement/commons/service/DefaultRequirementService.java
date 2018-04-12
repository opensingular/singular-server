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

package org.opensingular.requirement.commons.service;

import org.opensingular.requirement.commons.persistence.entity.form.RequirementDefinitionEntity;
import org.opensingular.requirement.commons.persistence.entity.form.RequirementEntity;

import javax.annotation.Nonnull;

/**
 * Implementação padrão de {@link RequirementService}. Não acrescenta nenhuma funcionaldiade ao serviço.
 *
 * @author Daniel C. Bordin on 08/03/2017.
 */
public class DefaultRequirementService extends RequirementService<RequirementEntity, RequirementInstance> {

    @Override
    @Nonnull
    protected RequirementInstance newRequirementInstance(@Nonnull RequirementEntity requirementEntity) {
        return new RequirementInstance(requirementEntity);
    }

    @Override
    @Nonnull
    protected RequirementEntity newRequirementEntityFor(RequirementDefinitionEntity requirementDefinitionEntity) {
        RequirementEntity requirementEntity = new RequirementEntity();
        requirementEntity.setRequirementDefinitionEntity(requirementDefinitionEntity);
        return requirementEntity;
    }
}
