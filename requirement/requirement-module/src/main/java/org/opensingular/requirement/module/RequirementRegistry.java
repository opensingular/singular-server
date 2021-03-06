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

package org.opensingular.requirement.module;

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * Configuration object for module {@link RequirementDefinition} registration.
 */
public class RequirementRegistry {
    private final AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext;

    public RequirementRegistry(AnnotationConfigWebApplicationContext annotationConfigWebApplicationContext) {
        this.annotationConfigWebApplicationContext = annotationConfigWebApplicationContext;
    }

    /**
     * Register a  {@link RequirementDefinition}
     *
     * @param requirement the {@link RequirementDefinition} instance.
     * @return
     */
    public RequirementRegistry add(Class<? extends RequirementDefinition> requirement) {
        annotationConfigWebApplicationContext.register(requirement);
        return this;
    }
}