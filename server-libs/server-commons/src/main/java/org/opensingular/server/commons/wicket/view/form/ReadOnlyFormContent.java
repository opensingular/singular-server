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

package org.opensingular.server.commons.wicket.view.form;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.opensingular.form.SType;
import org.opensingular.form.context.SFormConfig;
import org.opensingular.form.document.RefType;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.persistence.entity.FormVersionEntity;
import org.opensingular.form.service.IFormService;
import org.opensingular.form.wicket.enums.AnnotationMode;
import org.opensingular.form.wicket.enums.ViewMode;
import org.opensingular.form.wicket.panel.SingularFormPanel;
import org.opensingular.server.commons.wicket.view.template.Content;

import javax.inject.Inject;
import javax.inject.Named;


public class ReadOnlyFormContent extends Content {


    private final IModel<Long>        formVersionEntityPK;

    private SingularFormPanel singularFormPanel;

    @Inject
    private IFormService formService;

    @Inject
    @Named("formConfigWithDatabase")
    private SFormConfig<String> formConfig;

    public ReadOnlyFormContent(String id, IModel<Long> formVersionEntityPK, IModel<Boolean> showAnnotations) {
        super(id);
        this.formVersionEntityPK = formVersionEntityPK;

        final FormVersionEntity formVersionEntity = formService.loadFormVersionEntity(formVersionEntityPK.getObject());
        final FormKey           formKey           = formService.keyFromObject(formVersionEntity.getFormEntity().getCod());

        final RefType refType = new RefType() {
            @Override
            protected SType<?> retrieve() {
                return formConfig.getTypeLoader().loadTypeOrException(formVersionEntity.getFormEntity().getFormType().getAbbreviation());
            }
        };

        singularFormPanel = new SingularFormPanel("singularFormPanel");
        singularFormPanel.setInstanceCreator(() -> formService
                .loadSInstance(formKey, refType, formConfig.getDocumentFactory(), formVersionEntityPK.getObject()));

        singularFormPanel.setViewMode(ViewMode.READ_ONLY);
        singularFormPanel.setAnnotationMode(
                showAnnotations.getObject() ? AnnotationMode.READ_ONLY : AnnotationMode.NONE);

        add(new Form("form").add(singularFormPanel));
    }


    @Override
    protected IModel<?> getContentTitleModel() {
        return Model.of("");
    }

    @Override
    protected IModel<?> getContentSubtitleModel() {
        return Model.of("");
    }

}