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
package org.opensingular.requirement.module.admin.healthsystem.panel;

import javax.inject.Inject;

import de.alpharogroup.wicket.js.addon.toastr.ToastrType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.opensingular.form.SInstance;
import org.opensingular.form.util.transformer.TransformPojoUtil;
import org.opensingular.form.wicket.component.SingularValidationButton;
import org.opensingular.form.wicket.panel.SingularFormPanel;
import org.opensingular.requirement.module.admin.healthsystem.stypes.SDbHealth;
import org.opensingular.requirement.module.persistence.dto.healthsystem.HealthInfoDTO;
import org.opensingular.requirement.module.service.HealthSystemDbService;
import org.opensingular.requirement.module.wicket.view.SingularToastrHelper;

public class DbPanel extends Panel {

	@Inject
    private HealthSystemDbService servicePanel;

	public DbPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		SingularFormPanel panelBD = new SingularFormPanel("panelDB", SDbHealth.class);
		panelBD.setInstanceInitializer(instance -> {
			HealthInfoDTO infoHealthTest = servicePanel.getAllDbMetaData();
			TransformPojoUtil.pojoToSInstance(infoHealthTest, instance, false);
		});

		SingularValidationButton checkButton = new SingularValidationButton("checkButtonDB", panelBD.getInstanceModel()){
			@Override
			protected void onValidationSuccess(AjaxRequestTarget target, Form<?> form,
					IModel<? extends SInstance> instanceModel) {
				new SingularToastrHelper(this).
				addToastrMessage(ToastrType.SUCCESS, "All tables are accessible as expected!");
			}

			@Override
			protected void onValidationError(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
				super.onValidationError(target, form, instanceModel);

			}
		};
		add(panelBD);
		add(checkButton);
	}
}
