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

package org.opensingular.requirement.module.persistence.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.opensingular.flow.core.TaskType;
import org.opensingular.lib.support.persistence.enums.SimNao;
import org.opensingular.requirement.module.persistence.context.RequirementSearchContext;
import org.opensingular.requirement.module.persistence.filter.QuickFilter;

import javax.annotation.Nonnull;

import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.COD_REQUIREMENT;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.COD_USUARIO_ALOCADO;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.CREATION_DATE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.DESCRIPTION;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.EDITION_DATE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.FLOW_INSTANCE_ID;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.MODULE_COD;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.MODULE_CONTEXT;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.NOME_USUARIO_ALOCADO;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.PARENT_REQUIREMENT;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.PROCESS_BEGIN_DATE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.PROCESS_NAME;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.PROCESS_TYPE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.REQUIREMENT_DEFINITION_ID;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.ROOT_REQUIREMENT;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.SITUATION;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.SITUATION_BEGIN_DATE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.SOLICITANTE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.TASK_ID;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.TASK_INSTANCE_ID;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.TASK_NAME;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.TASK_TYPE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.TYPE;
import static org.opensingular.requirement.module.persistence.query.RequirementSearchAliases.VERSION_STAMP;

public class RequirementSearchQueryFactory {


    private final RequirementSearchContext ctx;
    private final RequirementSearchAliases $;

    private RequirementSearchQuery query;

    private static final String TO_CHAR_DATE = "TO_CHAR({0}, 'DD/MM/YYYY HH24:MI')";
    private static final String TO_CHAR_DATE_SHORT = "TO_CHAR({0}, 'DD/MM/YY HH24:MI')";

    public RequirementSearchQueryFactory(RequirementSearchContext ctx) {
        this.ctx = ctx;
        this.$ = ctx.getAliases();
    }

    public RequirementSearchQuery build(Session session) {
        query = new RequirementSearchQuery(session);
        ctx.setQuery(query);
        appendSelect();
        appendWhere();
        applyDefaultOrderBy();
        applyExtenders();
        applySortPropertyOrderBy();
        applyQuickFilter();
        if (Boolean.FALSE.equals(ctx.getCount())) {
            applyPagination();
        }
        return query;
    }

    private void applyPagination() {
        if (ctx.getQuickFilter().getCount() > 0) {
            query.offset(ctx.getQuickFilter().getFirst())
                    .limit(ctx.getQuickFilter().getCount());
        }
    }

    private void applyQuickFilter() {
        QuickFilter quickFilter = ctx.getQuickFilter();
        if (quickFilter != null && quickFilter.hasFilter()) {
            query.applyQuickFilter(quickFilter.listFilterTokens());
        }
    }

    private void applySortPropertyOrderBy() {
        QuickFilter quickFilter = ctx.getQuickFilter();
        if (quickFilter.getSortProperty() != null) {
            query.getMetadata().clearOrderBy();
            Order order = quickFilter.isAscending() ? Order.ASC : Order.DESC;
            query.orderBy(new OrderSpecifier<>(order, Expressions.stringPath(quickFilter.getSortProperty())));
        }
    }

    private void applyExtenders() {
        ctx.getExtenders().forEach(i -> i.extend(ctx));
    }

    private void applyDefaultOrderBy() {
        if (ctx.getQuickFilter().isRascunho()) {
            query.orderBy(new OrderSpecifier<>(Order.ASC, Expressions.stringPath(CREATION_DATE)));
        } else {
            query.orderBy(new OrderSpecifier<>(Order.ASC, Expressions.stringPath(PROCESS_BEGIN_DATE)));
        }
    }

    private void appendSelect() {
        query
                .addToSelect($.requirement.cod.as(COD_REQUIREMENT))
                .addToSelect($.requirement.description.as(DESCRIPTION))
                .addToSelect($.taskVersion.name.as(SITUATION))
                .addToSelect($.applicantEntity.name.as(SOLICITANTE))
                .addToSelect($.taskVersion.name.as(TASK_NAME))
                .addToSelect($.taskVersion.type.as(TASK_TYPE))
                .addToSelect($.flowDefinitionEntity.name.as(PROCESS_NAME))
                .addCaseToSelect($case -> $case
                        .when($.currentFormDraftVersionEntity.isNull())
                        .then($.currentFormVersion.inclusionDate)
                        .otherwise($.currentFormDraftVersionEntity.inclusionDate)
                        .as(CREATION_DATE))
                .addCaseToSelect($case -> $case
                        .when($.formType.abbreviation.isNull())
                        .then($.formDraftType.abbreviation)
                        .otherwise($.formType.abbreviation)
                        .as(TYPE))
                .addToSelect($.flowDefinitionEntity.key.as(PROCESS_TYPE))
                .addToSelect($.task.beginDate.as(SITUATION_BEGIN_DATE))
                .addToSelect($.task.cod.as(TASK_INSTANCE_ID))
                .addToSelect($.flowInstance.beginDate.as(PROCESS_BEGIN_DATE))
                .addToSelect($.currentDraftEntity.editionDate.as(EDITION_DATE))
                .addToSelect($.flowInstance.cod.as(FLOW_INSTANCE_ID))
                .addToSelect($.requirement.rootRequirement.cod.as(ROOT_REQUIREMENT))
                .addToSelect($.requirement.parentRequirement.cod.as(PARENT_REQUIREMENT))
                .addToSelect($.taskDefinition.cod.as(TASK_ID))
                .addToSelect($.task.versionStamp.as(VERSION_STAMP))
                .addToSelect($.allocatedUser.codUsuario.as(COD_USUARIO_ALOCADO))
                .addToSelect($.allocatedUser.nome.as(NOME_USUARIO_ALOCADO))
                .addToSelect($.module.cod.as(MODULE_COD))
                .addToSelect($.module.connectionURL.as(MODULE_CONTEXT))
                .addToSelect($.requirementDefinition.cod.as(REQUIREMENT_DEFINITION_ID));

        query
                .from($.requirement)
                .leftJoin($.requirement.applicant, $.applicantEntity)
                .leftJoin($.requirement.flowInstanceEntity, $.flowInstance)
                .leftJoin($.requirement.formRequirementEntities, $.formRequirementEntity).on($.formRequirementEntity.mainForm.eq(SimNao.SIM))
                .leftJoin($.formRequirementEntity.form, $.formEntity)
                .leftJoin($.formRequirementEntity.currentDraftEntity, $.currentDraftEntity)
                .leftJoin($.currentDraftEntity.form, $.formDraftEntity)
                .leftJoin($.formDraftEntity.currentFormVersionEntity, $.currentFormDraftVersionEntity)
                .leftJoin($.formEntity.currentFormVersionEntity, $.currentFormVersion)
                .leftJoin($.requirement.flowDefinitionEntity, $.flowDefinitionEntity)
                .leftJoin($.formEntity.formType, $.formType)
                .leftJoin($.formDraftEntity.formType, $.formDraftType)
                .leftJoin($.flowInstance.tasks, $.task)
                .leftJoin($.task.task, $.taskVersion)
                .leftJoin($.taskVersion.taskDefinition, $.taskDefinition)
                .leftJoin($.task.allocatedUser, $.allocatedUser)
                .leftJoin($.requirement.requirementDefinitionEntity, $.requirementDefinition)
                .leftJoin($.requirementDefinition.module, $.module);
    }

    private void appendWhere() {
        appendFilterByApplicant();
        appendFilterByFlowDefinitionAbbreviation();
        appendFilterByTasks();
        if (ctx.getQuickFilter().isRascunho()) {
            appendFilterByRequirementsWithoutFlowInstance();
        } else {
            appendFilterByRequirementsWithFlowInstance();
            appendFilterByCurrentTask();
        }
        appendFilterByQickFilter();
    }

    private void appendFilterByCurrentTask() {
        if (ctx.getQuickFilter().getEndedTasks() == null) {
            query.where($.taskVersion.type.eq(TaskType.END).or($.taskVersion.type.ne(TaskType.END).and($.task.endDate.isNull())));
        } else if (Boolean.TRUE.equals(ctx.getQuickFilter().getEndedTasks())) {
            query.where($.taskVersion.type.eq(TaskType.END));
        } else {
            query.where($.task.endDate.isNull());
        }
    }

    private void appendFilterByRequirementsWithFlowInstance() {
        query.where($.requirement.flowInstanceEntity.isNotNull());
    }

    private void appendFilterByRequirementsWithoutFlowInstance() {
        query.where($.requirement.flowInstanceEntity.isNull());
    }

    private void appendFilterByTasks() {
        if (!CollectionUtils.isEmpty(ctx.getQuickFilter().getTasks())) {
            query.where($.taskVersion.name.in(ctx.getQuickFilter().getTasks()));
        }
    }

    private void appendFilterByApplicant() {
        QuickFilter quickFilter = ctx.getQuickFilter();
        if (quickFilter.getIdPessoa() != null) {
            query.where($.applicantEntity.idPessoa.eq(quickFilter.getIdPessoa()));
        }
    }

    private void appendFilterByFlowDefinitionAbbreviation() {
        QuickFilter quickFilter = ctx.getQuickFilter();
        if (!quickFilter.isRascunho()
                && quickFilter.getProcessesAbbreviation() != null
                && !quickFilter.getProcessesAbbreviation().isEmpty()) {
            BooleanExpression expr = $.flowDefinitionEntity.key.in(quickFilter.getProcessesAbbreviation());
            if (quickFilter.getTypesNames() != null && !quickFilter.getTypesNames().isEmpty()) {
                expr = expr.or($.formType.abbreviation.in(quickFilter.getTypesNames()));
            }
            query.where(expr);
        }
    }

    private void appendFilterByQickFilter() {
        query.addConditionToQuickFilter(filter -> new BooleanBuilder()
                .or($.allocatedUser.nome.likeIgnoreCase(filter))
                .or($.requirement.description.likeIgnoreCase(filter))
                .or($.flowDefinitionEntity.name.likeIgnoreCase(filter))
                .or($.taskVersion.name.likeIgnoreCase(filter))
                .or($.requirement.cod.like(filter))
                .or(toCharDate($.currentFormVersion.inclusionDate, filter))
                .or(toCharDate($.currentFormDraftVersionEntity.inclusionDate, filter))
                .or(toCharDate($.currentDraftEntity.editionDate, filter))
                .or(toCharDate($.task.beginDate, filter))
                .or(toCharDate($.flowInstance.beginDate, filter)));
    }

    @Nonnull
    private BooleanExpression toCharDate(Path<?> path, String filter) {
        return Expressions.stringTemplate(TO_CHAR_DATE, path).like(filter)
                .or(toCharDateShort(path, filter));
    }

    @Nonnull
    private BooleanExpression toCharDateShort(Path<?> path, String filter) {
        return Expressions.stringTemplate(TO_CHAR_DATE_SHORT, path).like(filter);
    }

}