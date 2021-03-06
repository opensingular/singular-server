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

package org.opensingular.requirement.connector.sei31.model;

/**
 * Classe NivelAcesso.
 */
public enum NivelAcesso {

    /** O campo publico. */
    PUBLICO ("0", "Público"),

    /** O campo restrito. */
    RESTRITO("1", "Restrito"),

    /** O campo sigiloso. */
    SIGILOSO("2", "Sigiloso");

    private String codigo;
    private String descricao;

    private NivelAcesso(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    /**
     * Recupera o valor de codigo.
     * 
     * @return o valor de codigo
     */
    public String getCodigo() {
        return codigo;
    }

    public String getDescricao() {
        return descricao;
    }
}
