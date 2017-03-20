package org.opensingular.server.module.provider;

import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.service.PetitionService;
import org.opensingular.server.module.ItemBoxData;
import org.opensingular.server.module.ItemBoxDataProvider;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Named
public class PetitionItemBoxDataProvider implements ItemBoxDataProvider {

    private final PetitionService<?, ?> petitionService;

    @Inject
    public PetitionItemBoxDataProvider(PetitionService<?, ?> petitionService) {
        this.petitionService = petitionService;
    }

    @Override
    public List<Map<String, Serializable>> search(QuickFilter filter) {
        return petitionService.quickSearchMap(filter);
    }

    @Override
    public Long count(QuickFilter filter) {
        return petitionService.countQuickSearch(filter);
    }

    @Override
    public void configureLineActions(ItemBoxData line) {

    }

}