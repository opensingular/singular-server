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

package org.opensingular.requirement.module.config;

import org.opensingular.lib.commons.base.SingularProperties;

import java.util.Objects;

/**
 * Utilitário para prover a configuração de contexto atual e os métodos utilitários
 * relacionados.
 */
public abstract class ServerContext implements IServerContext {

    private final String contextPath;
    private final String propertiesBaseKey;
    private final String name;

    public ServerContext(String name, String defaultPath, String propertiesBaseKey) {
        this.name = name;
        this.propertiesBaseKey = propertiesBaseKey;
        String key = propertiesBaseKey + ".context";
        String path = SingularProperties.getOpt(key).orElse(null);
        if (path == null || path.length() <= 0) {
            path = defaultPath;
        }
        if (!path.endsWith("/*")) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 2) + "/*";
            } else if (path.endsWith("/")) {
                path += "*";
            } else {
                path += "/*";
            }
        }
        this.contextPath = path;
    }

    @Override
    public String getPropertiesBaseKey() {
        return propertiesBaseKey;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * O contexto no formato aceito por servlets e filtros
     *
     * @return
     */

    @Override
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para java regex
     *
     * @return
     */
    @Override
    public String getPathRegex() {
        return getContextPath().replaceAll("\\*", ".*");
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para um formato de url
     * sem a / ao final.
     *
     * @return
     */
    @Override
    public String getUrlPath() {
        String path = getContextPath().replace("*", "").replace(".", "").trim();
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerContext that = (ServerContext) o;
        return Objects.equals(contextPath, that.contextPath) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextPath, name);
    }
}