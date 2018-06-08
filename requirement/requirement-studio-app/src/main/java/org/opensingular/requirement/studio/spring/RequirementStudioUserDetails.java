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

package org.opensingular.requirement.studio.spring;

import org.apache.wicket.request.cycle.RequestCycle;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.requirement.commons.config.IServerContext;
import org.opensingular.requirement.commons.config.SingularServerConfiguration;
import org.opensingular.requirement.commons.spring.security.DefaultUserDetails;
import org.opensingular.requirement.commons.spring.security.SingularPermission;

import java.util.List;

/**
 * Implementation of UserDetails that keep the login through the contexts,
 * also return the ServerContext from request every time that the metod is called
 */
public class RequirementStudioUserDetails extends DefaultUserDetails {

    public RequirementStudioUserDetails(String username, List<SingularPermission> roles, String displayName) {
        super(username, roles, displayName, null);
    }

    @Override
    public IServerContext getServerContext() {
        RequestCycle requestCycle = RequestCycle.get();
        if (requestCycle != null) {
            return IServerContext.getContextFromRequest(requestCycle.getRequest(), singularServerConfiguration().getContexts());
        }
        return null;
    }

    @Override
    public boolean keepLoginThroughContexts() {
        return true;
    }

    protected SingularServerConfiguration singularServerConfiguration() {
        return ApplicationContextProvider.get().getBean(SingularServerConfiguration.class);
    }
}