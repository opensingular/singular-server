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

package org.opensingular.server.commons.service;

import org.apache.commons.collections.CollectionUtils;
import org.opensingular.flow.core.Flow;
import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowInstance;
import org.opensingular.flow.core.STransition;
import org.opensingular.flow.core.TaskInstance;
import org.opensingular.flow.core.TransitionCall;
import org.opensingular.flow.persistence.entity.Actor;
import org.opensingular.flow.persistence.entity.ProcessDefinitionEntity;
import org.opensingular.flow.persistence.entity.ModuleEntity;
import org.opensingular.flow.persistence.entity.ProcessInstanceEntity;
import org.opensingular.flow.persistence.entity.TaskInstanceEntity;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.SType;
import org.opensingular.form.persistence.FormKey;
import org.opensingular.form.persistence.entity.FormAnnotationEntity;
import org.opensingular.form.persistence.entity.FormEntity;
import org.opensingular.form.persistence.entity.FormVersionEntity;
import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.lib.commons.util.Loggable;
import org.opensingular.server.commons.exception.PetitionConcurrentModificationException;
import org.opensingular.server.commons.exception.SingularServerException;
import org.opensingular.server.commons.persistence.dao.flow.ActorDAO;
import org.opensingular.server.commons.persistence.dao.server.ModuleDAO;
import org.opensingular.server.commons.persistence.dao.flow.TaskInstanceDAO;
import org.opensingular.server.commons.persistence.dao.form.PetitionContentHistoryDAO;
import org.opensingular.server.commons.persistence.dao.form.PetitionDAO;
import org.opensingular.server.commons.persistence.dao.form.PetitionerDAO;
import org.opensingular.server.commons.persistence.dao.form.RequirementDefinitionDAO;
import org.opensingular.server.commons.persistence.dto.PetitionDTO;
import org.opensingular.server.commons.persistence.dto.PetitionHistoryDTO;
import org.opensingular.server.commons.persistence.entity.form.FormPetitionEntity;
import org.opensingular.server.commons.persistence.entity.form.FormVersionHistoryEntity;
import org.opensingular.server.commons.persistence.entity.form.PetitionContentHistoryEntity;
import org.opensingular.server.commons.persistence.entity.form.PetitionEntity;
import org.opensingular.server.commons.persistence.entity.form.PetitionerEntity;
import org.opensingular.server.commons.persistence.entity.form.RequirementDefinitionEntity;
import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.persistence.requirement.RequirementSearchExtender;
import org.opensingular.server.commons.spring.security.AuthorizationService;
import org.opensingular.server.commons.spring.security.PetitionAuthMetadataDTO;
import org.opensingular.server.commons.spring.security.SingularPermission;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Transactional
public abstract class PetitionService<PE extends PetitionEntity, PI extends PetitionInstance> implements Loggable {

    @Inject
    protected PetitionDAO<PE> petitionDAO;

    @Inject
    protected ModuleDAO moduleDAO;

    @Inject
    protected TaskInstanceDAO taskInstanceDAO;

    @Inject
    protected PetitionerDAO petitionerDAO;

    @Inject
    protected AuthorizationService authorizationService;

    @Inject
    protected ActorDAO actorDAO;

    @Inject
    private PetitionContentHistoryDAO petitionContentHistoryDAO;

    @Inject
    private FormPetitionService<PE> formPetitionService;

    @Inject
    private RequirementDefinitionDAO<RequirementDefinitionEntity> requirementDefinitionDAO;

    /**
     * Deve cria uma instância com base na entidade fornecida.
     */
    @Nonnull
    protected abstract PI newPetitionInstance(@Nonnull PE petitionEntity);

    /**
     * Deve cria uma nova entidade vazia de persistência.
     * @param requirementDefinitionEntity
     */
    @Nonnull
    protected abstract PE newPetitionEntityFor(RequirementDefinitionEntity requirementDefinitionEntity);

    /**
     * Recupera a petição associada ao fluxo informado ou dispara exception senão encontrar.
     */
    @Nonnull
    private PI getPetitionInstance(@Nonnull PE petitionEntity) {
        Objects.requireNonNull(petitionEntity);
        return newPetitionInstance(petitionEntity);
    }

    /**
     * Recupera a petição associada ao fluxo informado ou dispara exception senão encontrar.
     */
    @Nonnull
    public PI getPetitionInstance(@Nonnull FlowInstance flowInstance) {
        Objects.requireNonNull(flowInstance);
        PI instance = getPetitionInstance(getPetitionByProcessCod(flowInstance.getEntityCod()));
        instance.setFlowInstance(flowInstance);
        return instance;
    }


    /**
     * Recupera a petição associada a task informada ou dispara exception senão encontrar.
     */
    @Nonnull
    public PI getPetitionInstance(@Nonnull TaskInstance taskInstance) {
        Objects.requireNonNull(taskInstance);
        return getPetitionInstance(taskInstance.getFlowInstance());
    }

    /**
     * Retorna o serviço de formulários da petição.
     */
    @Nonnull
    protected FormPetitionService<PE> getFormPetitionService() {
        return Objects.requireNonNull(formPetitionService);
    }

    /**
     * Procura a petição com o código informado.
     */
    @Nonnull
    private Optional<PE> findPetitionByCod(@Nonnull Long cod) {
        Objects.requireNonNull(cod);
        return petitionDAO.find(cod);
    }

    /**
     * Procura a petição com o código informado.
     */
    @Nonnull
    public Optional<PI> findPetition(@Nonnull Long cod) {
        Objects.requireNonNull(cod);
        return petitionDAO.find(cod).map(this::newPetitionInstance);
    }

    /**
     * Recupera a petição com o código informado ou dispara Exception senão encontrar.
     */
    @Nonnull
    @Deprecated
    public PE getPetitionByCod(@Nonnull Long cod) {
        return findPetitionByCod(cod).orElseThrow(
                () -> SingularServerException.rethrow("Não foi encontrada a petição de cod=" + cod));
    }

    /**
     * Recupera a petição com o código informado ou dispara Exception senão encontrar.
     */
    @Nonnull
    public PI getPetition(@Nonnull Long cod) {
        return findPetition(cod).orElseThrow(
                () -> SingularServerException.rethrow("Não foi encontrada a petição de cod=" + cod));
    }

    /**
     * Recupera a petição associado a código de fluxo informado ou dispara exception senão encontrar.
     */
    @Nonnull
    @Deprecated
    public PE getPetitionByProcessCod(@Nonnull Integer cod) {
        Objects.requireNonNull(cod);
        return petitionDAO.findByProcessCodOrException(cod);
    }

    /**
     * Recupera a petição associado ao fluxo informado.
     */
    @Nonnull
    public PI getPetition(@Nonnull FlowInstance flowInstance) {
        Objects.requireNonNull(flowInstance);
        PE petition = getPetitionByProcessCod(flowInstance.getEntityCod());
        return newPetitionInstance(petition);
    }

    /**
     * Recupera a petição associada a tarefa informada.
     */
    @Nonnull
    public PI getPetition(@Nonnull TaskInstance taskInstance) {
        Objects.requireNonNull(taskInstance);
        return getPetition(taskInstance.getFlowInstance());
    }

    public void deletePetition(PetitionDTO peticao) {
        deletePetition(peticao.getCodPeticao());
    }

    public void deletePetition(@Nonnull Long idPeticao) {
        petitionDAO.find(idPeticao).ifPresent(pe -> petitionDAO.delete(pe));
    }

    public Long countQuickSearch(QuickFilter filter) {
        return countQuickSearch(filter, Collections.emptyList());
    }

    public Long countQuickSearch(QuickFilter filter, List<RequirementSearchExtender> extenders) {
        return petitionDAO.countQuickSearch(filter, extenders);
    }

    public List<Map<String, Serializable>> quickSearchMap(QuickFilter filter) {
        return quickSearchMap(filter, Collections.emptyList());
    }

    public List<Map<String, Serializable>> quickSearchMap(QuickFilter filter, List<RequirementSearchExtender> extenders) {
        return petitionDAO.quickSearchMap(filter, extenders);
    }


    @Nonnull
    public FormKey saveOrUpdate(@Nonnull PI petition, @Nonnull SInstance instance, boolean mainForm) {
        Objects.requireNonNull(petition);
        Objects.requireNonNull(instance);

        petitionDAO.saveOrUpdate((PE) petition.getEntity());

        if (petition.getPetitioner() != null) {
            petitionerDAO.saveOrUpdate(petition.getPetitioner());
        }
        return formPetitionService.saveFormPetition(petition, instance, mainForm);
    }

    public void onAfterStartProcess(PI petition, SInstance instance, String codResponsavel, FlowInstance flowInstance) {
    }

    public void onBeforeStartProcess(PI peticao, SInstance instance, String codResponsavel) {
    }

    public void savePetitionHistory(PetitionInstance petition, List<FormEntity> newEntities) {

        Optional<TaskInstanceEntity> taskInstance = findCurrentTaskEntityByPetitionId(petition.getCod());
        FormEntity formEntity = petition.getEntity().getMainForm();

        getLogger().info("Atualizando histórico da petição.");

        final PetitionContentHistoryEntity contentHistoryEntity = new PetitionContentHistoryEntity();

        contentHistoryEntity.setPetitionEntity(petition.getEntity());

        if (taskInstance.isPresent()) {
            contentHistoryEntity.setActor(taskInstance.get().getAllocatedUser());
            contentHistoryEntity.setTaskInstanceEntity(taskInstance.get());
        }

        if (CollectionUtils.isNotEmpty(formEntity.getCurrentFormVersionEntity().getFormAnnotations())) {
            contentHistoryEntity.setFormAnnotationsVersions(formEntity.getCurrentFormVersionEntity().getFormAnnotations().stream().map(FormAnnotationEntity::getAnnotationCurrentVersion).collect(Collectors.toList()));
        }

        contentHistoryEntity.setPetitionerEntity(petition.getPetitioner());
        contentHistoryEntity.setHistoryDate(new Date());

        petitionContentHistoryDAO.saveOrUpdate(contentHistoryEntity);

        contentHistoryEntity.setFormVersionHistoryEntities(
                petition.getEntity()
                        .getFormPetitionEntities()
                        .stream()
                        .filter(fpe -> newEntities.contains(fpe.getForm()))
                        .map(f -> formPetitionService.createFormVersionHistory(contentHistoryEntity, f))
                        .collect(Collectors.toList())
        );
    }


    /**
     * Executa a transição informada, consolidando todos os rascunhos, este metodo não salva a petição
     *
     * @param transitionName     nome da transicao
     * @param petition           peticao
     * @param transitionListener listener
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void executeTransition(String transitionName,
                                  PI petition,
                                  BiConsumer<PI, String> transitionListener,
                                  Map<String, String> processParameters,
                                  Map<String, String> transitionParameters) {
        try {
            if (transitionListener != null) {
                transitionListener.accept(petition, transitionName);
            }

            savePetitionHistory(petition, formPetitionService.consolidateDrafts(petition));
            FlowInstance processInstance = petition.getFlowInstance();
            checkTaskIsEqual(petition.getEntity().getProcessInstanceEntity(), processInstance);

            if (processParameters != null && !processParameters.isEmpty()) {
                for (Map.Entry<String, String> entry : processParameters.entrySet()) {
                    processInstance.getVariables().addValueString(entry.getKey(), entry.getValue());
                }
            }

            TransitionCall transitionCall = processInstance.prepareTransition(transitionName);
            if (transitionParameters != null && !transitionParameters.isEmpty()) {
                for (Map.Entry<String, String> transitionParameter : transitionParameters.entrySet()) {
                    transitionCall.addValueString(transitionParameter.getKey(), transitionParameter.getValue());
                }
            }
            transitionCall.go();
        } catch (SingularException e) {
            throw e;
        } catch (Exception e) {
            throw SingularServerException.rethrow(e.getMessage(), e);
        }
    }

    private void checkTaskIsEqual(ProcessInstanceEntity processInstanceEntity, FlowInstance piAtual) {
        //TODO (Daniel) Não creio que esse método esteja sendo completamente efetivo (revisar)
        if (!processInstanceEntity.getCurrentTask().getTaskVersion().getAbbreviation().equalsIgnoreCase(piAtual.getCurrentTaskOrException().getAbbreviation())) {
            throw new PetitionConcurrentModificationException("A instância está em uma tarefa diferente da esperada.");
        }
    }

    public List<Map<String, Serializable>> listTasks(QuickFilter filter, List<SingularPermission> permissions) {
        return listTasks(filter, authorizationService.filterListTaskPermissions(permissions), Collections.emptyList());
    }

    public Long countTasks(QuickFilter filter, List<SingularPermission> permissions) {
        return countTasks(filter, authorizationService.filterListTaskPermissions(permissions), Collections.emptyList());
    }

    public List<Map<String, Serializable>> listTasks(QuickFilter filter, List<SingularPermission> permissions, List<RequirementSearchExtender> extenders) {
        return petitionDAO.quickSearchMap(filter, authorizationService.filterListTaskPermissions(permissions), extenders);
    }

    public Long countTasks(QuickFilter filter, List<SingularPermission> permissions, List<RequirementSearchExtender> extenders) {
        return petitionDAO.countQuickSearch(filter, authorizationService.filterListTaskPermissions(permissions), extenders);
    }

    public Optional<TaskInstance> findCurrentTaskInstanceByPetitionId(Long petitionId) {
        return findCurrentTaskEntityByPetitionId(petitionId)
                .map(Flow::getTaskInstance);
    }

    @Nonnull
    public Optional<TaskInstanceEntity> findCurrentTaskEntityByPetitionId(@Nonnull Long petitionId) {
        //TODO (Daniel) Por que usar essa entidade em vez de TaskIntnstace ?
        Objects.requireNonNull(petitionId);
        List<TaskInstanceEntity> taskInstances = taskInstanceDAO.findCurrentTasksByPetitionId(petitionId);
        if (taskInstances.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(taskInstances.get(0));
    }

    public List<ModuleEntity> listAllModules() {
        return moduleDAO.listAll();
    }

    public ModuleEntity findByModuleCod(String cod) {
        return moduleDAO.get(cod).orElse(null);
    }

    @Nonnull
    public PI createNewPetitionWithoutSave(@Nullable Class<? extends FlowDefinition> classProcess, @Nullable PI parentPetition,
                                           @Nullable Consumer<PI> creationListener, RequirementDefinitionEntity requirementDefinitionEntity) {

        final PE petitionEntity = newPetitionEntityFor(requirementDefinitionEntity);

        if (classProcess != null) {
            petitionEntity.setProcessDefinitionEntity((ProcessDefinitionEntity) Flow.getProcessDefinition(classProcess).getEntityProcessDefinition());
        }
        if (parentPetition != null) {
            PetitionEntity parentPetitionEntity = parentPetition.getEntity();
            petitionEntity.setParentPetition(parentPetitionEntity);
            if (parentPetitionEntity.getRootPetition() != null) {
                petitionEntity.setRootPetition(parentPetitionEntity.getRootPetition());
            } else {
                petitionEntity.setRootPetition(parentPetitionEntity);
            }
        }

        PI petition = newPetitionInstance(petitionEntity);
        if (creationListener != null) {
            creationListener.accept(petition);
        }
        return petition;
    }


    public List<PetitionHistoryDTO> listPetitionContentHistoryByPetitionCod(long petitionCod, String menu, boolean filter) {
        PE petition = petitionDAO.findOrException(petitionCod);
        return petitionContentHistoryDAO.listPetitionContentHistoryByPetitionCod(petition, menu, filter);
    }

    public List<Actor> listAllocableUsers(Map<String, Object> selectedTask) {
        Integer taskInstanceId = Integer.valueOf(String.valueOf(selectedTask.get("taskInstanceId")));
        return actorDAO.listAllocableUsers(taskInstanceId);
    }

    public PetitionerEntity findPetitionerByExternalId(String externalId) {
        return petitionerDAO.findPetitionerByExternalId(externalId);
    }

    @Nonnull
    public boolean isPreviousTransition(@Nonnull TaskInstance taskInstance, @Nonnull String trasitionName) {
        Optional<STransition> executedTransition = taskInstance.getFlowInstance().getLastFinishedTask().map(TaskInstance::getExecutedTransition).orElse(Optional.<STransition>empty());
        if (executedTransition.isPresent()) {
            STransition transition = executedTransition.get();
            return trasitionName.equals(transition.getName());

        }
        return false;
    }

    public PetitionAuthMetadataDTO findPetitionAuthMetadata(Long petitionId) {
        return petitionDAO.findPetitionAuthMetadata(petitionId);
    }

    public List<FormVersionEntity> buscarDuasUltimasVersoesForm(@Nonnull Long codPetition) {
        PetitionEntity petitionEntity = petitionDAO.findOrException(codPetition);
        FormEntity mainForm = petitionEntity.getMainForm();
        return formPetitionService.findTwoLastFormVersions(mainForm.getCod());
    }

    /**
     * Procura a instância de processo (fluxo) associado ao formulário se o mesmo existir.
     */
    public Optional<ProcessInstanceEntity> getFormProcessInstanceEntity(@Nonnull SInstance instance) {
        return getFormPetitionService().findFormEntity(instance)
                .map(formEntity -> petitionDAO.findByFormEntity(formEntity))
                .map(PetitionEntity::getProcessInstanceEntity);
    }

    /**
     * Verifica se o formulário já foi persistido e possui um processo (fluxo) instanciado e associado.
     */
    public boolean formHasProcessInstance(SInstance instance) {
        return getFormProcessInstanceEntity(instance).isPresent();
    }

    /**
     * Recupera o formulário {@link SInstance} de abertura do requerimento.
     */
    @Nonnull
    public SIComposite getMainFormAsInstance(@Nonnull PetitionEntity petition) {
        Objects.requireNonNull(petition);
        return (SIComposite) getFormPetitionService().getSInstance(petition.getMainForm());
    }

    /**
     * Recupera o formulário {@link SInstance} de abertura do requerimento e garante que é do tipo inforado.
     */
    @Nonnull
    public <I extends SInstance, K extends SType<? extends I>> I getMainFormAsInstance(@Nonnull PetitionEntity petition,
                                                                                       @Nonnull Class<K> expectedType) {
        Objects.requireNonNull(petition);
        return getFormPetitionService().getSInstance(petition.getMainForm(), expectedType);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<FormPetitionEntity> findLastFormPetitionEntityByType(@Nonnull PetitionInstance petition,
                                                                         @Nonnull Class<? extends SType<?>> typeClass) {
        return getFormPetitionService().findLastFormPetitionEntityByType(petition, typeClass);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<SInstance> findLastFormPetitionInstanceByType(@Nonnull PetitionInstance petition,
                                                                  @Nonnull Class<? extends SType<?>> typeClass) {
        return getFormPetitionService().findLastFormPetitionInstanceByType(petition, typeClass);
    }

    /**
     * Procura na petição a versão mais recente do formulário do tipo informado.
     */
    @Nonnull
    public Optional<SIComposite> findLastestFormInstanceByType(@Nonnull PetitionInstance petition,
                                                               @Nonnull Class<? extends SType<?>> typeClass) {
        //TODO Verificar se esse método não está redundante com FormPetitionService.findLastFormPetitionEntityByType
        Objects.requireNonNull(petition);
        return petitionContentHistoryDAO.findLastestByPetitionCodAndType(typeClass, petition.getCod())
                .map(FormVersionHistoryEntity::getFormVersion)
                .map(version -> (SIComposite) getFormPetitionService().getSInstance(version));
    }

    /**
     * Procura na petição o formulário mais recente dentre os tipos informados.
     */
    @Nonnull
    protected Optional<SIComposite> findLastestFormInstanceByType(@Nonnull PetitionInstance petition,
                                                                  @Nonnull Collection<Class<? extends SType<?>>> typesClass) {
        Objects.requireNonNull(petition);
        FormVersionHistoryEntity max = null;
        for (Class<? extends SType<?>> type : typesClass) {
            //TODO (Daniel) Deveria fazer uma única consulta para otimziar o resultado
            Optional<FormVersionHistoryEntity> result = petitionContentHistoryDAO.findLastestByPetitionCodAndType(type,
                    petition.getCod());
            if (result.isPresent() && (max == null || max.getPetitionContentHistory().getHistoryDate().before(
                    result.get().getPetitionContentHistory().getHistoryDate()))) {
                max = result.get();
            }
        }
        return Optional.ofNullable(max).map(
                version -> (SIComposite) getFormPetitionService().getSInstance(version.getFormVersion()));
    }

    public FlowInstance startNewProcess(PetitionInstance petition, FlowDefinition flowDefinition) {
        FlowInstance newFlowInstance = flowDefinition.newPreStartInstance();
        newFlowInstance.setDescription(petition.getDescription());

        ProcessInstanceEntity processEntity = newFlowInstance.saveEntity();
        PE petitionEntity = (PE) petition.getEntity();
        petitionEntity.setProcessInstanceEntity(processEntity);
        petitionEntity.setProcessDefinitionEntity(processEntity.getProcessVersion().getProcessDefinition());
        petitionDAO.saveOrUpdate(petitionEntity);

        newFlowInstance.start();

        petition.setFlowInstance(newFlowInstance);

        return newFlowInstance;
    }

    //TODO vinicius.nunes LENTO
    @Deprecated
    public boolean containChildren(Long petitionCod) {
        return petitionDAO.containChildren(petitionCod);
    }

    public void updatePetitionDescription(SInstance currentInstance, PI petition) {
        String description = currentInstance.toStringDisplay();
        if (description != null && description.length() > 200) {
            getLogger().error("Descrição do formulário muito extensa. A descrição foi cortada.");
            description = description.substring(0, 197) + "...";
        }
        petition.setDescription(description);
    }

    public RequirementDefinitionEntity findRequirementDefinition(Long requirementId) {
        return requirementDefinitionDAO.findOrException(requirementId);
    }

}