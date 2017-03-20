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

package org.opensingular.server.module.rest;

import org.opensingular.flow.core.Flow;
import org.opensingular.flow.core.MTask;
import org.opensingular.flow.core.ProcessDefinition;
import org.opensingular.form.SFormUtil;
import org.opensingular.form.SInfoType;
import org.opensingular.form.SType;
import org.opensingular.form.context.SFormConfig;
import org.opensingular.server.commons.config.IServerContext;
import org.opensingular.server.commons.config.SingularServerConfiguration;
import org.opensingular.server.commons.flow.metadata.PetitionHistoryTaskMetaDataValue;
import org.opensingular.server.commons.service.IServerMetadataREST;
import org.opensingular.server.commons.service.dto.FormDTO;
import org.opensingular.server.commons.service.dto.MenuGroup;
import org.opensingular.server.commons.service.dto.ProcessDTO;
import org.opensingular.server.commons.spring.security.AuthorizationService;
import org.opensingular.server.commons.spring.security.PermissionResolverService;
import org.opensingular.server.module.SingularModuleConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@RequestMapping("/rest/flow")
@RestController
public class DefaultServerMetadataREST implements IServerMetadataREST {

    @Inject
    protected SingularServerConfiguration singularServerConfiguration;

    @Inject
    protected SingularModuleConfiguration singularModuleConfiguration;

    @Inject
    protected AuthorizationService authorizationService;

    @Inject
    protected PermissionResolverService permissionResolverService;

    @Inject
    @Named("formConfigWithDatabase")
    protected SFormConfig<String> singularFormConfig;

    @Override
    public List<MenuGroup> listMenu(IServerContext context, String user) {
        List<MenuGroup> groups = listMenuGroups();
        filterAccessRight(groups, user);
        customizeMenu(groups, context, user);
        return groups;
    }

    @RequestMapping(value = PATH_LIST_MENU, method = RequestMethod.GET)
    public List<MenuGroup> listMenu(@RequestParam(MENU_CONTEXT) String context, @RequestParam(USER) String user) {
        return listMenu(IServerContext.getContextFromName(context, singularServerConfiguration.getContexts()), user);
    }

    protected List<MenuGroup> listMenuGroups() {
        final List<MenuGroup> groups = new ArrayList<>();
        getDefinitionsMap().forEach((category, definitions) -> {
            MenuGroup menuGroup = new MenuGroup();
            menuGroup.setId(permissionResolverService.buildCategoryPermission(category).getSingularId());
            menuGroup.setLabel(category);
            menuGroup.setProcesses(new ArrayList<>());
            menuGroup.setForms(new ArrayList<>());
            definitions.forEach(d -> {
                        List<MTask<?>> tasks = d.getFlowMap().getTasksWithMetadata(PetitionHistoryTaskMetaDataValue.KEY);
                        List<String> allowedHistoryTasks = tasks.stream().map(MTask::getAbbreviation).collect(Collectors.toList());
                        menuGroup
                                .getProcesses()
                                .add(new ProcessDTO(d.getKey(), d.getName(), null, allowedHistoryTasks));
                    }
            );
            addForms(menuGroup);
            groups.add(menuGroup);
        });
        return groups;
    }

    protected Map<String, List<ProcessDefinition>> getDefinitionsMap() {
        final Map<String, List<ProcessDefinition>> definitionMap = new HashMap<>();
        Flow.getDefinitions().forEach(d -> {
            if (!definitionMap.containsKey(d.getCategory())) {
                definitionMap.put(d.getCategory(), new ArrayList<>());
            }
            definitionMap.get(d.getCategory()).add(d);
        });
        return definitionMap;
    }

    @SuppressWarnings("unchecked")
    protected void addForms(MenuGroup menuGroup) {
        for (Class<? extends SType<?>> formClass : singularServerConfiguration.getFormTypes()) {
            SInfoType annotation = formClass.getAnnotation(SInfoType.class);
            String name = SFormUtil.getTypeName(formClass);
            Optional<SType<?>> sTypeOptional = singularFormConfig.getTypeLoader().loadType(name);
            if (sTypeOptional.isPresent()) {
                SType<?> sType = sTypeOptional.get();
                Class<? extends SType<?>> sTypeClass = (Class<? extends SType<?>>) sType.getClass();
                String label = sType.asAtr().getLabel();
                menuGroup.getForms().add(new FormDTO(name, SFormUtil.getTypeSimpleName(sTypeClass), label, annotation.newable()));
            }
        }
    }

    protected void filterAccessRight(List<MenuGroup> groupDTOs, String user) {
        authorizationService.filterBoxWithPermissions(groupDTOs, user);
    }

    protected void customizeMenu(List<MenuGroup> groupDTOs, IServerContext menuContext, String user) {
        for (MenuGroup menuGroup : groupDTOs) {
            menuGroup.setItemBoxes(singularModuleConfiguration.buildItemBoxes(menuContext));
        }
    }


}