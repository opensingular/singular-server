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

package org.opensingular.requirement.module.service.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.opensingular.lib.commons.ui.Icon;
import org.opensingular.requirement.module.jackson.IconJsonDeserializer;
import org.opensingular.requirement.module.jackson.IconJsonSerializer;

public class ItemBox implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private String helpText;
    private boolean quickFilter       = true;
    private boolean showDraft         = false;
    private boolean showHistoryAction = true;
    private Boolean              endedTasks;
    private Icon                 icon;
    private List<DatatableField> fieldsDatatable;

    public ItemBox() {
    }

    public boolean isShowHistoryAction() {
        return showHistoryAction;
    }

    public void setShowHistoryAction(boolean showHistoryAction) {
        this.showHistoryAction = showHistoryAction;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isQuickFilter() {
        return quickFilter;
    }

    public void setQuickFilter(boolean quickFilter) {
        this.quickFilter = quickFilter;
    }

    public List<DatatableField> getFieldsDatatable() {
        return fieldsDatatable;
    }

    public void setFieldsDatatable(List<DatatableField> fieldsDatatable) {
        this.fieldsDatatable = fieldsDatatable;
    }

    public boolean isShowDraft() {
        return showDraft;
    }

    public void setShowDraft(boolean showDraft) {
        this.showDraft = showDraft;
    }

    @JsonSerialize(using = IconJsonSerializer.class)
    public Icon getIcone() {
        return icon;
    }

    @JsonDeserialize(using = IconJsonDeserializer.class)
    public void setIcone(Icon icon) {
        this.icon = icon;
    }

    public Boolean getEndedTasks() {
        return endedTasks;
    }

    @Deprecated
    public void setEndedTasks(Boolean endedTasks) {
        this.endedTasks = endedTasks;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSearchEndpoint() {
        return "/search/" + id;
    }

    public String getCountEndpoint() {
        return "/count/" + id;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }
}