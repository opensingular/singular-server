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

package org.opensingular.requirement.connector.sei30.model;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public class UnidadeSei implements Serializable {

    private final String id;
    private final String nome; // Essa variavel não é utilizada pelo serviço do SEI.

    public UnidadeSei(String id, @Nullable String nome) {
        this.id = id;
        this.nome = nome;

    }

    @Nullable
    public String getNome() {
        return nome;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnidadeSei)) return false;
        UnidadeSei that = (UnidadeSei) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getId());
    }
}
