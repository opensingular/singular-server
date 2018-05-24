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

package org.opensingular.requirement.commons.config;

import org.opensingular.flow.core.FlowDefinitionCache;
import org.opensingular.flow.core.SingularFlowConfigurationBean;
import org.opensingular.requirement.commons.flow.SingularServerFlowConfigurationBean;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;

public abstract class FlowInitializer {


    /**
     * TODO will be moved to bean factory in future releases
     * @return
     */
    @Deprecated
    public Class<? extends SingularFlowConfigurationBean> singularFlowConfiguration() {
        return SingularServerFlowConfigurationBean.class;
    }

    public void init(ServletContext ctx, AnnotationConfigWebApplicationContext applicationContext) {
        FlowDefinitionCache.invalidateAll();
        applicationContext.register(singularFlowConfiguration());
    }

    public abstract String moduleCod();

}
