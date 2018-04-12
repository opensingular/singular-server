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

package org.opensingular.requirement.core.wicket.box;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.lib.commons.lambda.IBiFunction;
import org.opensingular.lib.commons.lambda.IFunction;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.lib.wicket.util.datatable.BSDataTableBuilder;
import org.opensingular.lib.wicket.util.datatable.IBSAction;
import org.opensingular.lib.wicket.util.datatable.column.BSActionColumn;
import org.opensingular.lib.wicket.util.datatable.column.BSActionPanel;
import org.opensingular.requirement.commons.box.BoxItemDataMap;
import org.opensingular.requirement.commons.box.action.ActionAtribuirRequest;
import org.opensingular.requirement.commons.box.action.ActionRequest;
import org.opensingular.requirement.commons.box.action.ActionResponse;
import org.opensingular.requirement.commons.connector.ModuleDriver;
import org.opensingular.requirement.commons.form.FormAction;
import org.opensingular.requirement.commons.persistence.filter.QuickFilter;
import org.opensingular.requirement.commons.service.dto.BoxDefinitionData;
import org.opensingular.requirement.commons.service.dto.BoxItemAction;
import org.opensingular.requirement.commons.service.dto.DatatableField;
import org.opensingular.requirement.commons.service.dto.FormDTO;
import org.opensingular.requirement.commons.service.dto.ItemActionType;
import org.opensingular.requirement.commons.service.dto.ItemBox;
import org.opensingular.requirement.commons.service.dto.RequirementData;
import org.opensingular.requirement.commons.service.dto.RequirementDefinitionDTO;
import org.opensingular.requirement.commons.wicket.buttons.NewRequirementLink;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.opensingular.lib.wicket.util.util.WicketUtils.*;

public class BoxContent extends AbstractBoxContent<BoxItemDataMap> implements Loggable {

    @Inject
    private ModuleDriver moduleDriver;

    private Pair<String, SortOrder>   sortProperty;
    private IModel<BoxDefinitionData> definitionModel;

    public BoxContent(String id, String moduleCod, String menu, BoxDefinitionData itemBox) {
        super(id, moduleCod, menu);
        this.definitionModel = new Model<>(itemBox);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        configureQuickFilter();
    }

    private void configureQuickFilter() {
        getFiltroRapido().setVisible(isShowQuickFilter());
        getPesquisarButton().setVisible(isShowQuickFilter());
    }

    @Override
    public Component buildNewRequirementButton(String id) {
        IModel<List<RequirementData>> requirementsModel = new PropertyModel<>(definitionModel, "requirements");
        if (!requirementsModel.getObject().isEmpty() && getMenu() != null) {
            return new NewRequirementLink(id, getBaseUrl(), getLinkParams(), requirementsModel);
        } else {
            return super.buildNewRequirementButton(id);
        }
    }

    @Override
    protected void appendPropertyColumns(BSDataTableBuilder<BoxItemDataMap, String, IColumn<BoxItemDataMap, String>> builder) {
        for (DatatableField entry : getFieldsDatatable()) {
            builder.appendPropertyColumn($m.ofValue(entry.getKey()), entry.getLabel(), entry.getLabel());
        }
    }

    @Override
    protected void appendActionColumns(BSDataTableBuilder<BoxItemDataMap, String, IColumn<BoxItemDataMap, String>> builder) {
        BSActionColumn<BoxItemDataMap, String> actionColumn = new BSActionColumn<BoxItemDataMap, String>(getMessage("label.table.column.actions")) {
            @Override
            protected void onPopulateActions(IModel<BoxItemDataMap> rowModel, BSActionPanel<BoxItemDataMap> actionPanel) {
                resetActions();
                Set<Map.Entry<String, BoxItemAction>> actions = Optional
                        .ofNullable(rowModel)
                        .map(IModel::getObject)
                        .map(BoxItemDataMap::getActionsMap)
                        .map(Map::entrySet)
                        .orElse(new HashSet<>(0));

                for (Map.Entry<String, BoxItemAction> entry : actions) {
                    BoxItemAction itemAction = entry.getValue();

                    if (itemAction.getType() == ItemActionType.URL_POPUP) {
                        appendStaticAction(
                                $m.ofValue(itemAction.getLabel()),
                                itemAction.getIcon(),
                                linkFunction(itemAction, getLinkParams()),
                                visibleFunction(itemAction),
                                c -> c.styleClasses($m.ofValue("worklist-action-btn")));
                    } else if (itemAction.getType() == ItemActionType.EXECUTE) {
                        appendAction(
                                $m.ofValue(itemAction.getLabel()),
                                itemAction.getIcon(),
                                dynamicLinkFunction(itemAction, getLinkParams()),
                                visibleFunction(itemAction),
                                c -> c.styleClasses($m.ofValue("worklist-action-btn")));
                    }
                }


                super.onPopulateActions(rowModel, actionPanel);
            }

        };

        builder.appendColumn(actionColumn);
    }


    public IBiFunction<String, IModel<BoxItemDataMap>, MarkupContainer> linkFunction(BoxItemAction itemAction, Map<String, String> additionalParams) {
        return (id, boxItemModel) -> {
            String             url  = moduleDriver.buildUrlToBeRedirected(boxItemModel.getObject(), itemAction, additionalParams, getBaseUrl());
            WebMarkupContainer link = new WebMarkupContainer(id);
            link.add($b.attr("target", String.format("_%s_%s", itemAction.getName(), boxItemModel.getObject().getCod())));
            link.add($b.attr("href", url));
            return link;
        };
    }

    private IBSAction<BoxItemDataMap> dynamicLinkFunction(BoxItemAction itemAction, Map<String, String> additionalParams) {
        if (itemAction.getConfirmation() != null) {
            return (target, model) -> {
                getDataModel().setObject(model.getObject());
                showConfirm(target, construirModalConfirmationBorder(itemAction, additionalParams));
            };
        } else {
            return (target, model) -> executeDynamicAction(itemAction, additionalParams, model.getObject(), target);
        }
    }

    protected void executeDynamicAction(BoxItemAction itemAction,
                                        Map<String, String> additionalParams,
                                        BoxItemDataMap boxItem, AjaxRequestTarget target) {
        final BoxItemAction boxAction = boxItem.getActionByName(itemAction.getName());
        try {
            callModule(boxAction, additionalParams, buildCallObject(boxAction, boxItem));
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            ((BoxPage) getPage()).addToastrErrorMessage("Não foi possível executar esta ação.");
        } finally {
            target.add(table);
        }
    }

    protected void relocate(BoxItemAction itemAction,
                            Map<String, String> additionalParams, BoxItemDataMap boxItem,
                            AjaxRequestTarget target, Actor actor) {
        final BoxItemAction boxAction = boxItem.getActionByName(itemAction.getName());
        try {
            callModule(itemAction, additionalParams, buildCallAtribuirObject(boxAction, boxItem, actor));
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
            ((BoxPage) getPage()).addToastrErrorMessage("Não foi possível executar esta ação.");
        } finally {
            target.add(table);
        }
    }

    protected ActionRequest buildCallAtribuirObject(BoxItemAction boxAction, BoxItemDataMap boxItem, Actor actor) {
        ActionAtribuirRequest actionRequest = new ActionAtribuirRequest();
        actionRequest.setIdUsuario(getBoxPage().getIdUsuario());
        if (actor == null) {
            actionRequest.setEndLastAllocation(true);
        } else {
            actionRequest.setIdUsuarioDestino(actor.getCodUsuario());
        }
        if (boxAction.isUseExecute()) {
            actionRequest.setAction(boxAction);
            actionRequest.setLastVersion(boxItem.getVersionStamp());
        }

        return actionRequest;
    }

    private void callModule(BoxItemAction itemAction, Map<String, String> params, ActionRequest actionRequest) {
        ActionResponse response = moduleDriver.executeAction(getModule(), itemAction, params, actionRequest);
        if (response.isSuccessful()) {
            ((BoxPage) getPage()).addToastrSuccessMessage(response.getResultMessage());
        } else {
            ((BoxPage) getPage()).addToastrErrorMessage(response.getResultMessage());
        }
    }

    private ActionRequest buildCallObject(BoxItemAction boxAction, BoxItemDataMap boxItem) {
        ActionRequest actionRequest = new ActionRequest();
        actionRequest.setIdUsuario(getBoxPage().getIdUsuario());
        if (boxAction.isUseExecute()) {
            actionRequest.setAction(boxAction);
            actionRequest.setLastVersion(boxItem.getVersionStamp());
        }

        return actionRequest;
    }

    protected BoxContentConfirmModal<BoxItemDataMap> construirModalConfirmationBorder(BoxItemAction itemAction,
                                                                                      Map<String, String> additionalParams) {
        if (StringUtils.isNotBlank(itemAction.getConfirmation().getSelectEndpoint())) {
            return new BoxContentAllocateModal(itemAction, getDataModel(), $m.ofValue(getModule())) {
                @Override
                protected void onDeallocate(AjaxRequestTarget target) {
                    relocate(itemAction, additionalParams, getDataModel().getObject(), target, null);
                    target.add(table);
                    atualizarContadores(target);
                }

                @Override
                protected void onConfirm(AjaxRequestTarget target) {
                    relocate(itemAction, additionalParams, getDataModel().getObject(), target, usersDropDownChoice.getModelObject());
                    target.add(table);
                    atualizarContadores(target);
                }
            };
        } else {
            return new BoxContentConfirmModal<BoxItemDataMap>(itemAction, getDataModel()) {
                @Override
                protected void onConfirm(AjaxRequestTarget target) {
                    executeDynamicAction(itemAction, additionalParams, getDataModel().getObject(), target);
                    target.add(table);
                    atualizarContadores(target);
                }
            };
        }
    }

    protected void atualizarContadores(AjaxRequestTarget target) {
        target.appendJavaScript("(function(){window.Singular.atualizarContadores();}())");
    }


    private IFunction<IModel<BoxItemDataMap>, Boolean> visibleFunction(BoxItemAction itemAction) {
        return (model) -> {
            BoxItemDataMap boxItemDataMap = model.getObject();
            boolean        visible        = boxItemDataMap.hasAction(itemAction);
            if (!visible) {
                getLogger().debug("Action {} não está disponível para o item ({}: código da petição) da listagem ", itemAction.getName(), boxItemDataMap.getCod());
            }

            return visible;
        };
    }

    @Override
    protected Pair<String, SortOrder> getSortProperty() {
        return sortProperty;
    }

    public void setSortProperty(Pair<String, SortOrder> sortProperty) {
        this.sortProperty = sortProperty;
    }

    @Override
    protected void onDelete(BoxItemDataMap peticao) {

    }

    @Override
    protected QuickFilter newFilterBasic() {
        BoxPage boxPage = getBoxPage();
        return boxPage.createFilter()
                .withFilter(getFiltroRapidoModelObject())
                .withProcessesAbbreviation(getProcessesNames())
                .withTypesNames(getFormNames())
                .withRascunho(isWithRascunho())
                .withEndedTasks(getItemBoxModelObject().getEndedTasks());
    }

    private BoxPage getBoxPage() {
        return (BoxPage) getPage();
    }

    private List<String> getProcessesNames() {
        if (getProcesses() == null) {
            return Collections.emptyList();
        } else {
            return getProcesses()
                    .stream()
                    .map(RequirementDefinitionDTO::getAbbreviation)
                    .collect(Collectors.toList());
        }
    }

    private List<String> getFormNames() {
        if (getForms() == null) {
            return Collections.emptyList();
        } else {
            return getForms()
                    .stream()
                    .map(FormDTO::getName)
                    .collect(Collectors.toList());
        }
    }

    @Override
    protected List<BoxItemDataMap> quickSearch(QuickFilter filter, List<String> flowDefinitionAbbreviation, List<String> formNames) {
        return moduleDriver.searchFiltered(getModule(), getItemBoxModelObject(), filter);
    }

    @Override
    protected WebMarkupContainer createLink(String id, IModel<BoxItemDataMap> requirementModel, FormAction formAction) {
        // Em virtude da sobrescrita do appendActionColumns, esse método não é utilizado aqui.
        throw new UnsupportedOperationException("Esse metodo não é utilizado no BoxContent");
    }

    private Map<String, String> getLinkParams() {
        final BoxPage page = getBoxPage();
        return page.createLinkParams();
    }

    @Override
    protected long countQuickSearch(QuickFilter filter, List<String> processesNames, List<String> formNames) {
        return moduleDriver.countFiltered(getModule(), getItemBoxModelObject(), filter);
    }

    public boolean isShowQuickFilter() {
        return getItemBoxModelObject().isQuickFilter();
    }

    public List<DatatableField> getFieldsDatatable() {
        return getItemBoxModelObject().getFieldsDatatable();
    }

    public String getSearchEndpoint() {
        return getItemBoxModelObject().getSearchEndpoint();
    }

    public String getCountEndpoint() {
        return getItemBoxModelObject().getCountEndpoint();
    }

    public boolean isWithRascunho() {
        return getItemBoxModelObject().isShowDraft();
    }

    private ItemBox getItemBoxModelObject() {
        return definitionModel.getObject().getItemBox();
    }
}