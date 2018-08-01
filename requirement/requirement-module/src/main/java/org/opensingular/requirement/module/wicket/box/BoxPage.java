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

package org.opensingular.requirement.module.wicket.box;

import org.apache.wicket.Component;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.opensingular.requirement.module.BoxInfo;
import org.opensingular.requirement.module.SingularModuleConfiguration;
import org.opensingular.requirement.module.config.IServerContext;
import org.opensingular.requirement.module.config.workspace.Workspace;
import org.opensingular.requirement.module.persistence.filter.BoxFilter;
import org.opensingular.requirement.module.persistence.filter.BoxFilterFactory;
import org.opensingular.requirement.module.service.BoxService;
import org.opensingular.requirement.module.service.dto.ItemBox;
import org.opensingular.requirement.module.spring.security.SingularRequirementUserDetails;
import org.opensingular.requirement.module.wicket.SingularSession;
import org.opensingular.requirement.module.wicket.error.Page403;
import org.opensingular.requirement.module.wicket.template.ServerBoxTemplate;
import org.opensingular.requirement.module.wicket.view.template.Menu;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.opensingular.requirement.module.wicket.view.util.ActionContext.ITEM_PARAM_NAME;

@MountPath("/box")
public class BoxPage extends ServerBoxTemplate {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BoxPage.class);

    @Inject
    private BoxFilterFactory boxFilterFactory;

    @Inject
    private SingularModuleConfiguration singularModuleConfiguration;

    @Inject
    private BoxService boxService;

    protected IModel<ItemBox> itemBox;

    protected IModel<IServerContext> serverContext;

    public BoxPage(PageParameters parameters) {
        this(parameters, null);
    }

    public BoxPage(PageParameters parameters, IServerContext serverContext) {
        super(parameters);
        if (serverContext != null) {
            this.serverContext = new Model<>(serverContext);
        } else {
            this.serverContext = new Model<>(IServerContext.getContextFromRequest(getRequest(), singularModuleConfiguration.getContexts()));
        }
        addBox();
    }

    public void addBox() {
        String item = getPageParameters().get(ITEM_PARAM_NAME).toOptionalString();

        if (serverContext == null) {
            LOGGER.error("Não foi possivel determinal o contexto atual");
            throw new RestartResponseException(Page403.class);
        }

        Workspace workspace = serverContext.getObject().getWorkspace();

        if (item == null) {
            PageParameters pageParameters = new PageParameters();
            addItemParam(workspace, pageParameters);
            throw new RestartResponseException(getPageClass(), pageParameters);
        }

        itemBox = workspace.getBoxInfos().stream()
                .map(boxService::loadItemBox)
                .filter(i -> i.getName().equals(item)).findFirst()
                .map(Model::new)
                .orElse(null);

        if (itemBox != null) {
            add(newBoxContent("box"));
        } else {
            LOGGER.error("As configurações de caixas não foram encontradas. Verfique se as permissões estão configuradas corretamente");
            throw new RestartResponseException(Page403.class);
        }
    }

    private void addItemParam(Workspace workspace, PageParameters pageParameters) {
        Optional<BoxInfo> box = workspace.getBoxInfos().stream().findFirst();
        box.ifPresent(boxInfo -> pageParameters.add(ITEM_PARAM_NAME, boxService.loadItemBox(boxInfo).getName()));
    }

    protected Component newBoxContent(String id) {
        return new BoxContent(id, itemBox);
    }

    protected Map<String, String> createLinkParams() {
        return new HashMap<>();
    }

    protected BoxFilter createFilter() {
        return boxFilterFactory.create(getItemBoxObject());
    }

    protected ItemBox getItemBoxObject() {
        return itemBox.getObject();
    }

    @Override
    protected IModel<String> getContentSubtitle() {
        return new Model<String>() {
            @Override
            public String getObject() {
                if (getItemBoxObject() != null) {
                    return getItemBoxObject().getDescription();
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
                if (getItemBoxObject() != null) {
                    return getItemBoxObject().getName();
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
                if (getItemBoxObject() != null) {
                    return getItemBoxObject().getHelpText();
                }
                return null;
            }
        };
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
    protected @Nonnull
    WebMarkupContainer buildPageMenu(String id) {
        return new Menu(id, BoxPage.class, serverContext);
    }

}