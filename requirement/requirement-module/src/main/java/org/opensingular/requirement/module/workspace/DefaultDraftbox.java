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

package org.opensingular.requirement.module.workspace;

import java.util.ArrayList;
import java.util.List;

import org.opensingular.lib.wicket.util.resource.DefaultIcons;
import org.opensingular.requirement.commons.config.IServerContext;
import org.opensingular.requirement.commons.config.PServerContext;
import org.opensingular.requirement.commons.service.dto.DatatableField;
import org.opensingular.requirement.commons.service.dto.ItemBox;
import org.opensingular.requirement.module.ActionProviderBuilder;
import org.opensingular.requirement.module.BoxItemDataProvider;
import org.opensingular.requirement.module.provider.RequirementBoxItemDataProvider;

public class DefaultDraftbox implements BoxDefinition {

    @Override
    public boolean appliesTo(IServerContext context) {
        return PServerContext.REQUIREMENT.isSameContext(context);
    }

    @Override
    public ItemBox build(IServerContext context) {
        final ItemBox rascunho = new ItemBox();
        rascunho.setName("Rascunho");
        rascunho.setDescription("Requerimentos de rascunho");
        rascunho.setIcone(DefaultIcons.DOCS);
        rascunho.setShowHistoryAction(false);
        rascunho.setShowDraft(true);
        return rascunho;
    }

    @Override
    public BoxItemDataProvider getDataProvider() {
        return new RequirementBoxItemDataProvider(Boolean.FALSE, new ActionProviderBuilder()
                .addEditAction()
                .addViewAction()
                .addDeleteAction());
    }

    @Override
    public List<DatatableField> getDatatableFields() {
        List<DatatableField> fields = new ArrayList<>();
        fields.add(DatatableField.of("Descrição", "description"));
        fields.add(DatatableField.of("Dt. Edição", "editionDate"));
        fields.add(DatatableField.of("Data de Entrada", "creationDate"));
        return fields;
    }

}
