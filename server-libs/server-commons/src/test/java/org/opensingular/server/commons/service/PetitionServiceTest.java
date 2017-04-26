package org.opensingular.server.commons.service;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Test;
import org.opensingular.flow.core.ProcessInstance;
import org.opensingular.flow.core.TaskType;
import org.opensingular.form.SIComposite;
import org.opensingular.form.SInstance;
import org.opensingular.form.document.RefSDocumentFactory;
import org.opensingular.form.document.RefType;
import org.opensingular.form.document.SDocumentFactory;
import org.opensingular.form.helpers.AssertionsSInstance;
import org.opensingular.form.persistence.entity.FormTypeEntity;
import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.server.commons.STypeFOO;
import org.opensingular.server.commons.persistence.dao.form.PetitionDAO;
import org.opensingular.server.commons.persistence.dto.PetitionDTO;
import org.opensingular.server.commons.persistence.dto.TaskInstanceDTO;
import org.opensingular.server.commons.persistence.entity.form.PetitionEntity;
import org.opensingular.server.commons.persistence.filter.QuickFilter;
import org.opensingular.server.commons.spring.security.SingularPermission;
import org.opensingular.server.commons.test.FOOFlow;
import org.opensingular.server.commons.test.SingularCommonsBaseTest;
import org.springframework.test.annotation.Rollback;

import static org.junit.Assert.*;

@Transactional
public class PetitionServiceTest extends SingularCommonsBaseTest {

    @Inject
    public PetitionService<PetitionEntity, PetitionInstance> petitionService;

    @Inject
    public PetitionSender petitionSender;

    @Inject
    public SessionFactory sessionFactory;

    @Inject
    protected PetitionDAO<PetitionEntity> petitionDAO;

    @Test
    public void testName() throws Exception {
        Session s = sessionFactory.openSession();
        org.hibernate.Transaction t = s.beginTransaction();
        FormTypeEntity formTypeEntity = new FormTypeEntity();
        formTypeEntity.setAbbreviation("nada");
        formTypeEntity.setLabel("nada");
        formTypeEntity.setCacheVersionNumber(1l);
        s.saveOrUpdate(formTypeEntity);
        s.flush();
        t.commit();

    }

    @Test
    public void newPetitionEntity() {
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        assertNotNull(petitionEntity);
    }

    @Test
    public void newPetitionInstance() {
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        assertNotNull(petitionInstance);
        assertEquals(petitionEntity, petitionInstance.getEntity());
    }

    @Test
    public void saveNewPetition() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);

        petitionService.saveOrUpdate(petitionInstance, instance, true);

        SIComposite mainFormAsInstance = petitionService.getMainFormAsInstance(petitionEntity);
        new AssertionsSInstance(instance).isValueEquals(mainFormAsInstance);
    }

    @Test
    public void sendNewPetition() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);
        petitionInstance.setProcessDefinition(FOOFlow.class);

        petitionSender.send(petitionInstance, instance, "vinicius.nunes");
        petitionService.executeTransition("No more bar", petitionInstance, null, null);
    }

    @Test
    public void testFindPetition() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);

        Optional<PetitionInstance> petition = petitionService.findPetition(petitionInstance.getCod());
    }

    @Test
    public void testGetPetition() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);

        PetitionInstance petition = petitionService.getPetition(petitionInstance.getCod());
        Assert.assertEquals(petitionInstance.getEntity(), petition.getEntity());
    }

    @Test
    public void testDeletePetition() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);

        petitionService.deletePetition(petitionInstance.getCod());
        Assert.assertFalse(petitionService.findPetition(petitionInstance.getCod()).isPresent());
    }

    @Test
    public void testDeletePetitionWithPetitionDTO() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);

        PetitionDTO dto = new PetitionDTO();
        dto.setCodPeticao(petitionInstance.getCod());

        petitionService.deletePetition(dto);
        Assert.assertFalse(petitionService.findPetition(petitionInstance.getCod()).isPresent());
    }

    @Test
    public void testListCurrentTaskTransitionsWithEmptyTransitions() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(petitionInstance, instance, true);

        Assert.assertEquals(0, petitionService.listCurrentTaskTransitions(petitionInstance.getCod()).size());
    }

    @Test(expected = SingularException.class)
    @Rollback
    public void testGetPetitionException() {
        petitionService.getPetition((long)0);
    }

    @Test
    public void quickSearchTests() {

        long qtdEnviada = 0;
        long qtdRascunho = 0;
        List<PetitionEntity> petitionEntities = petitionDAO.listAll();

        for (PetitionEntity petitionEntity : petitionEntities) {
            if (petitionEntity.getProcessInstanceEntity() == null) {
                qtdRascunho++;
            } else {
                qtdEnviada++;
            }
        }

        QuickFilter f1 = new QuickFilter();
        List<Map<String, Serializable>> maps1 = petitionService.quickSearchMap(f1);
        assertEquals(qtdEnviada, maps1.size());

        QuickFilter f2 = new QuickFilter();
        f2.withRascunho(true).withSortProperty("description");
        List<Map<String, Serializable>> maps2 = petitionService.quickSearchMap(f2);
        assertEquals(qtdRascunho, maps2.size());

        QuickFilter f3 = new QuickFilter();
        Long        count = petitionService.countQuickSearch(f3);
        assertTrue(count == qtdEnviada);
    }

    @Test
    @Rollback
    public void countTasks(){
        QuickFilter filter = new QuickFilter();
        filter.withFilter("filter");
        filter.withProcessesAbbreviation(Arrays.asList("task1", "task2"));

        SingularPermission permission = new SingularPermission("singularId", "internalId");

        Assert.assertEquals(new Long(0), petitionService.countTasks(filter, Arrays.asList(permission)));
    }

    @Test
    public void listTasks() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        String description = "Descrição XYZ única - " + System.nanoTime();
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionInstance.setDescription(description);
        petitionService.saveOrUpdate(petitionInstance, instance, true);
        petitionInstance.setProcessDefinition(FOOFlow.class);
        petitionSender.send(petitionInstance, instance, "vinicius.nunes");

        QuickFilter           filter           = new QuickFilter();
        filter.withFilter(description);
        List<TaskInstanceDTO> taskInstanceDTOS = petitionService.listTasks(filter, Collections.emptyList());

        assertEquals(1, taskInstanceDTOS.size());
        TaskInstanceDTO task = taskInstanceDTOS.get(0);
        assertNull(task.getCodUsuarioAlocado());
        assertNull(task.getNomeUsuarioAlocado());
        assertEquals("Do bar", task.getTaskName());
        assertEquals(TaskType.PEOPLE, task.getTaskType());
        assertEquals("foooooo.StypeFoo", task.getType());
        assertEquals(description, task.getDescription());
    }

    @Test
    public void testSearchs() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        String description = "Descrição XYZ única - " + System.nanoTime();
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance petitionInstance = petitionService.newPetitionInstance(petitionEntity);
        petitionInstance.setDescription(description);
        petitionService.saveOrUpdate(petitionInstance, instance, true);
        petitionInstance.setProcessDefinition(FOOFlow.class);
        petitionSender.send(petitionInstance, instance, "vinicius.nunes");
        ProcessInstance  processInstance = petitionInstance.getProcessInstance();

        PetitionInstance p2 = petitionService.getPetitionInstance(processInstance);
        PetitionInstance p3 = petitionService.getPetitionInstance(processInstance.getCurrentTaskOrException());
        PetitionInstance p4 = petitionService.getPetition(processInstance);
        PetitionInstance p5 = petitionService.getPetition(processInstance.getCurrentTaskOrException());

        assertEquals(petitionInstance.getCod(), p2.getCod());
        assertEquals(petitionInstance.getCod(), p3.getCod());
        assertEquals(petitionInstance.getCod(), p4.getCod());
        assertEquals(petitionInstance.getCod(), p5.getCod());
    }

    @Test
    public void createPetitionWithoutSave() {
        RefSDocumentFactory documentFactoryRef = SDocumentFactory.empty().getDocumentFactoryRef();
        SInstance instance = documentFactoryRef.get().createInstance(RefType.of(STypeFOO.class));
        PetitionEntity petitionEntity = petitionService.newPetitionEntity();
        PetitionInstance parent = petitionService.newPetitionInstance(petitionEntity);
        petitionService.saveOrUpdate(parent, instance, true);
        parent.setProcessDefinition(FOOFlow.class);
        petitionSender.send(parent, instance, "vinicius.nunes");

        PetitionInstance petition = petitionService.createNewPetitionWithoutSave(FOOFlow.class, parent, PetitionInstance::getCod);

        assertNull(petition.getCod());
    }
}
