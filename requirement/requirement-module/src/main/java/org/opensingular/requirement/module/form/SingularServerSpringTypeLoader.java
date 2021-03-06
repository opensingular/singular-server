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

package org.opensingular.requirement.module.form;

import org.opensingular.form.SFormUtil;
import org.opensingular.form.SType;
import org.opensingular.form.SingularFormException;
import org.opensingular.form.document.RefDictionary;
import org.opensingular.form.spring.SpringTypeLoader;
import org.opensingular.requirement.module.service.RequirementUtil;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class SingularServerSpringTypeLoader extends SpringTypeLoader<String> {

    private final Map<String, Supplier<SType<?>>> entries = new LinkedHashMap<>();

    @Inject
    private FormTypesProvider formTypesProvider;

    public SingularServerSpringTypeLoader() {
    }

    @PostConstruct
    private void init() {
        formTypesProvider.get().forEach(this::add);
    }

    private void add(@Nonnull Class<? extends SType<?>> type) {
        String typeName = RequirementUtil.getTypeName(type);
        add(typeName, () -> RefDictionary.newBlank().get().getType(type));
    }

    private void add(@Nonnull String typeName, @Nonnull Supplier<SType<?>> typeSupplier) {
        entries.put(typeName, typeSupplier);
    }

    @Override
    @Nonnull
    protected Optional<SType<?>> loadTypeImpl(@Nonnull String typeId) {
        return Optional.ofNullable(entries.get(typeId)).map(Supplier::get);
    }

    @Nonnull
    public Optional<SType<?>> loadType(@Nonnull Class<? extends SType> typeClass) {
        String typeId = SFormUtil.getTypeName(typeClass);
        return loadTypeImpl(typeId);
    }

    @Nonnull
    public SType<?> loadTypeOrException(@Nonnull Class<? extends SType> typeClass) {
        Objects.requireNonNull(typeClass);
        return loadType(typeClass).orElseThrow(() -> new SingularFormException("Não foi encontrado o tipo para a classe=" + typeClass));
    }

}
