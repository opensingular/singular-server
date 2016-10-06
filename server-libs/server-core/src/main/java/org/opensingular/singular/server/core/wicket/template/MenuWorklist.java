/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.opensingular.singular.server.core.wicket.template;


import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.wicket.Component;

import org.opensingular.singular.commons.lambda.ISupplier;
import org.opensingular.singular.server.commons.service.PetitionService;
import org.opensingular.singular.server.commons.wicket.SingularSession;
import org.opensingular.singular.server.core.wicket.concluida.ConcluidaPage;
import org.opensingular.singular.server.core.wicket.inicio.InicioPage;
import org.opensingular.singular.util.wicket.menu.MetronicMenu;
import org.opensingular.singular.util.wicket.menu.MetronicMenuGroup;
import org.opensingular.singular.util.wicket.menu.MetronicMenuItem;
import org.opensingular.singular.util.wicket.resource.Icone;


@SuppressWarnings("serial")
public class MenuWorklist extends MenuAnalise {

    @SuppressWarnings("rawtypes")
    @Inject
    private PetitionService petitionService;

    public MenuWorklist(String id) {
        super(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected MetronicMenu buildMenu() {
        loadMenuGroups();

        final MetronicMenu menu = new MetronicMenu("menu");
        //TODO prover solução melhor para todos os contextos de aplicação
        final MetronicMenuGroup group = new MetronicMenuGroup(Icone.LAYERS, "Worklist");
        final MetronicMenuItem entrada = new MetronicMenuItem(Icone.DOCS, "Inicio", InicioPage.class);
        final MetronicMenuItem concluidas = new MetronicMenuItem(Icone.DOCS, "Concluídas", ConcluidaPage.class);

        menu.addItem(group);
        group.addItem(entrada);
        group.addItem(concluidas);


        final List<Pair<Component, ISupplier<String>>> itens = new ArrayList<>();

        itens.add(Pair.of(entrada.getHelper(), () -> String.valueOf(petitionService.countTasks(null, SingularSession.get().getUserDetails().getPermissions(), null, false))));
        itens.add(Pair.of(concluidas.getHelper(), () -> String.valueOf(petitionService.countTasks(null, SingularSession.get().getUserDetails().getPermissions(), null, true))));
        menu.add(new AddContadoresBehaviour(itens));

        return menu;
    }

}