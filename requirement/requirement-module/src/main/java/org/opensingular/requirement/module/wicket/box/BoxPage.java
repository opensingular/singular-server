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

package org.opensingular.requirement.module.wicket.box;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.opensingular.requirement.module.WorkspaceConfigurationMetadata;
import org.opensingular.requirement.module.persistence.filter.QuickFilter;
import org.opensingular.requirement.module.service.dto.BoxConfigurationData;
import org.opensingular.requirement.module.service.dto.BoxDefinitionData;
import org.opensingular.requirement.module.spring.security.SingularRequirementUserDetails;
import org.opensingular.requirement.module.wicket.SingularSession;
import org.opensingular.requirement.module.wicket.error.Page403;
import org.opensingular.requirement.module.wicket.template.ServerBoxTemplate;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.opensingular.requirement.module.wicket.view.util.ActionContext.ITEM_PARAM_NAME;
import static org.opensingular.requirement.module.wicket.view.util.ActionContext.MENU_PARAM_NAME;

@MountPath("/box")
public class BoxPage extends ServerBoxTemplate {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BoxPage.class);

    private BoxDefinitionData boxDefinitionData;

    @Inject
    @SpringBean(required = false)
    private WorkspaceConfigurationMetadata workspaceConfigurationMetadata;

    public BoxPage(PageParameters parameters) {
        super(parameters);
        addBox();
    }

    public void addBox() {
        String menu = getPageParameters().get(MENU_PARAM_NAME).toOptionalString();
        String item = getPageParameters().get(ITEM_PARAM_NAME).toOptionalString();

        if (isAccessWithoutParams(menu, item)) {
            for (BoxConfigurationData box : workspaceConfigurationMetadata.getBoxesConfiguration()) {
                menu = box.getLabel();
                PageParameters pageParameters = new PageParameters();

                addItemParam(box, pageParameters);

                pageParameters.add(MENU_PARAM_NAME, menu);
                throw new RestartResponseException(getPageClass(), pageParameters);
            }
        }

        BoxConfigurationData boxConfigurationMetadata = null;
        if (workspaceConfigurationMetadata != null) {
            boxConfigurationMetadata = workspaceConfigurationMetadata.getMenuByLabel(menu).orElse(null);
        }
        if (boxConfigurationMetadata != null) {
            boxDefinitionData = boxConfigurationMetadata.getItemPorLabel(item);
            //itemBoxDTO pode ser nulo quando nenhum item está selecionado.
            if (boxDefinitionData != null) {
                add(newBoxContent("box", boxConfigurationMetadata, boxDefinitionData));
                return;
            }
        }

        if (boxConfigurationMetadata == null) {
            LOGGER.error("As configurações de caixas não foram encontradas. Verfique se as permissões estão configuradas corretamente");
        }
        LOGGER.error("Não existe caixa correspondente para {}", String.valueOf(item));
        throw new RestartResponseException(Page403.class);
    }

    private boolean isAccessWithoutParams(String menu, String item) {
        return menu == null
                && item == null
                && workspaceConfigurationMetadata != null;
    }

    private void addItemParam(BoxConfigurationData mg, PageParameters pageParameters) {
        if (!mg.getBoxesDefinition().isEmpty()) {
            String item = mg.getItemBoxes().get(0).getName();
            pageParameters.add(ITEM_PARAM_NAME, item);
        }
    }

    protected Component newBoxContent(String id, BoxConfigurationData boxConfigurationMetadata, BoxDefinitionData boxDefinitionData) {
        return new BoxContent(id, boxConfigurationMetadata.getLabel(), boxDefinitionData);
    }

    protected Map<String, String> createLinkParams() {
        return new HashMap<>();
    }

    protected QuickFilter createFilter() {
        return new QuickFilter()
                .withIdUsuarioLogado(getIdUsuario())
                .withIdPessoa(getIdPessoa());
    }

    protected String getIdUsuario() {
        SingularRequirementUserDetails userDetails = SingularSession.get().getUserDetails();
        return Optional.ofNullable(userDetails)
                .map(SingularRequirementUserDetails::getUsername)
                .orElse(null);
    }

    protected String getIdPessoa() {
        SingularRequirementUserDetails userDetails = SingularSession.get().getUserDetails();
        return Optional.ofNullable(userDetails)
                .map(SingularRequirementUserDetails::getApplicantId)
                .orElse(null);
    }

    @Override
    protected IModel<String> getContentSubtitle() {
        return new Model<String>() {
            @Override
            public String getObject() {
                if (boxDefinitionData != null) {
                    return boxDefinitionData.getItemBox().getDescription();
                }
                return null;
            }
        };
    }

    @Override
    protected IModel<String> getContentTitle() {
        return new Model<String>() {
            @Override
            public String getObject() {
                if (boxDefinitionData != null) {
                    return boxDefinitionData.getItemBox().getName();
                }
                return null;
            }
        };
    }

    @Override
    public IModel<String> getHelpText() {
        return new Model<String>() {
            @Override
            public String getObject() {
                if (boxDefinitionData != null) {
                    return boxDefinitionData.getItemBox().getHelpText();
                }
                return null;
            }
        };
    }
}