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

package org.opensingular.requirement.module.wicket.template;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.opensingular.requirement.module.wicket.view.template.ServerTemplate;

public abstract class ServerBoxTemplate extends ServerTemplate {

    public ServerBoxTemplate() {
    }

    public ServerBoxTemplate(PageParameters parameters) {
        super(parameters);
    }

    @Override
    protected boolean isWithMenu() {
        return true;
    }
}