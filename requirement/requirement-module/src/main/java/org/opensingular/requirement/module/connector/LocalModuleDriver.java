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

package org.opensingular.requirement.module.connector;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.request.Url;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.requirement.module.box.BoxItemDataMap;
import org.opensingular.requirement.module.box.action.ActionRequest;
import org.opensingular.requirement.module.box.action.ActionResponse;
import org.opensingular.requirement.module.persistence.filter.QuickFilter;
import org.opensingular.requirement.module.rest.ModuleBackstageService;
import org.opensingular.requirement.module.service.dto.BoxItemAction;
import org.opensingular.requirement.module.service.dto.ItemActionConfirmation;
import org.opensingular.requirement.module.service.dto.ItemBox;
import org.opensingular.requirement.module.wicket.SingularSession;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LocalModuleDriver implements ModuleDriver {

    @Inject
    private ModuleBackstageService moduleBackstageService;

    @Override
    public String countAll(ItemBox box, List<String> flowNames, String loggedUser) {
        QuickFilter filter = new QuickFilter()
                .withProcessesAbbreviation(flowNames)
                .withRascunho(box.isShowDraft())
                .withEndedTasks(box.getEndedTasks())
                .withIdUsuarioLogado(loggedUser)
                .withIdPessoa(SingularSession.get().getUserDetails().getApplicantId());
        return String.valueOf(moduleBackstageService.count(box.getId(), filter));
    }

    @Override
    public long countFiltered(ItemBox box, QuickFilter filter) {
        return moduleBackstageService.count(box.getId(), filter);
    }

    @Override
    public List<BoxItemDataMap> searchFiltered(ItemBox box, QuickFilter filter) {
        return moduleBackstageService.search(box.getId(), filter).getBoxItemDataList().stream().map(BoxItemDataMap::new).collect(Collectors.toList());
    }

    @Override
    public List<Actor> findEligibleUsers(BoxItemDataMap rowItemData, ItemActionConfirmation confirmAction) {
        return moduleBackstageService.listAllowedUsers(rowItemData);

    }

    @Override
    public ActionResponse executeAction(BoxItemAction rowAction, Map<String, String> params, ActionRequest actionRequest) {
        Url.QueryParameter idQueryParam = Url.parse(rowAction.getEndpoint()).getQueryParameter("id");
        Long action = null;
        if (idQueryParam != null) {
            action = Long.valueOf(idQueryParam.getValue());
        }
        return moduleBackstageService.executar(action, actionRequest);
    }

    @Override
    public String buildUrlToBeRedirected(BoxItemDataMap rowItemData, BoxItemAction rowAction, Map<String, String> params, String baseURI) {
        final BoxItemAction action = rowItemData.getActionByName(rowAction.getName());
        final String endpoint = StringUtils.trimToEmpty(action.getEndpoint());
        if (endpoint.startsWith("http")) {
            return endpoint;
        } else {
            return baseURI
                    + endpoint
                    + appendParameters(params);
        }
    }

    private String appendParameters(Map<String, String> additionalParams) {
        StringBuilder paramsValue = new StringBuilder();
        if (!additionalParams.isEmpty()) {
            for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
                paramsValue.append(String.format("&%s=%s", entry.getKey(), entry.getValue()));
            }
        }
        return paramsValue.toString();
    }
}
