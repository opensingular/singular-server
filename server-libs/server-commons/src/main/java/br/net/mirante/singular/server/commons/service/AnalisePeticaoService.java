package br.net.mirante.singular.server.commons.service;

import br.net.mirante.singular.flow.core.Flow;
import br.net.mirante.singular.flow.core.ProcessInstance;
import br.net.mirante.singular.persistence.entity.TaskInstanceEntity;
import br.net.mirante.singular.server.commons.exception.SingularServerException;
import br.net.mirante.singular.server.commons.persistence.dao.flow.TaskInstanceDAO;
import br.net.mirante.singular.server.commons.persistence.dto.TaskInstanceDTO;
import br.net.mirante.singular.server.commons.persistence.entity.form.Petition;
import br.net.mirante.singular.server.commons.wicket.SingularSession;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
public class AnalisePeticaoService<T extends TaskInstanceDTO> {

    @Inject
    PetitionService petitionService;
    @Inject
    private TaskInstanceDAO taskInstanceDAO;

    private List<String> getIdsPerfis() {
        return SingularSession.get().getRoles().stream().collect(Collectors.toList());
    }


    public List<? extends TaskInstanceDTO> listTasks(int first, int count, String sortProperty, boolean ascending, String siglaFluxo, String filtroRapido, boolean concluidas) {
        return taskInstanceDAO.findTasks(first, count, sortProperty, ascending, siglaFluxo, getIdsPerfis(), filtroRapido, concluidas);
    }


    public Integer countTasks(String siglaFluxo, String filtroRapido, boolean concluidas) {
        return taskInstanceDAO.countTasks(siglaFluxo, getIdsPerfis(), filtroRapido, concluidas);
    }


    public TaskInstanceEntity findCurrentTaskByPetitionId(String petitionId) {
        List<TaskInstanceEntity> taskInstances = taskInstanceDAO.findCurrentTasksByPetitionId(petitionId);
        if (taskInstances.isEmpty()) {
            return null;
        } else {
            return taskInstances.get(0);
        }
    }

    @SuppressWarnings("unchecked")
    public void salvarExecutarTransicao(String transitionName, Petition peticao) {
        try {
            petitionService.saveOrUpdate(peticao);
            ProcessInstance pi = Flow.getProcessInstance((Class)Class.forName(peticao.getProcessType()), peticao.getProcessInstanceEntity().getCod());
            pi.executeTransition(transitionName);
        } catch (Exception e) {
            throw new SingularServerException(e.getMessage(), e);
        }
    }
}