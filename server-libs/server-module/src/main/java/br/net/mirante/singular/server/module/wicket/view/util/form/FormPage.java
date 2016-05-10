package br.net.mirante.singular.server.module.wicket.view.util.form;

import static br.net.mirante.singular.util.wicket.util.WicketUtils.$m;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.wicketstuff.annotation.mount.MountPath;

import br.net.mirante.singular.flow.core.Flow;
import br.net.mirante.singular.flow.core.MTask;
import br.net.mirante.singular.flow.core.MTransition;
import br.net.mirante.singular.flow.core.TaskInstance;
import br.net.mirante.singular.form.SIComposite;
import br.net.mirante.singular.form.SInstance;
import br.net.mirante.singular.form.STypeComposite;
import br.net.mirante.singular.form.context.SFormConfig;
import br.net.mirante.singular.form.internal.xml.MElement;
import br.net.mirante.singular.form.type.basic.AtrBasic;
import br.net.mirante.singular.form.wicket.enums.AnnotationMode;
import br.net.mirante.singular.form.wicket.enums.ViewMode;
import br.net.mirante.singular.persistence.entity.ProcessInstanceEntity;
import br.net.mirante.singular.server.commons.config.SingularServerConfiguration;
import br.net.mirante.singular.server.commons.exception.SingularServerException;
import br.net.mirante.singular.server.commons.persistence.entity.form.Petition;
import br.net.mirante.singular.server.commons.service.AnalisePeticaoService;
import br.net.mirante.singular.server.commons.service.PetitionService;
import br.net.mirante.singular.server.commons.wicket.view.form.AbstractFormPage;

@SuppressWarnings("serial")
@MountPath("/view")
public class FormPage extends AbstractFormPage {

    private IModel<Petition> currentModel;

    @Inject
    private PetitionService petitionService;

    @Inject
    private AnalisePeticaoService analisePeticaoService;

    @Inject
    @Named("formConfigWithDatabase")
    private SFormConfig<String> singularFormConfig;

    @Inject
    private SingularServerConfiguration singularServerConfiguration;

    public FormPage(FormPageConfig config) {
        super(config);
    }


    @Override
    protected List<MTransition> currentTaskTransitions(String petitionId) {
        return Optional
                .ofNullable(Flow.getTaskInstance(analisePeticaoService.findCurrentTaskByPetitionId(petitionId)))
                .map(TaskInstance::getFlowTask)
                .map(MTask::getTransitions)
                .orElse(Collections.EMPTY_LIST);
    }

    @Override
    protected void executeTransition(String transitionName, IModel<?> currentInstance) {
        analisePeticaoService.salvarExecutarTransicao(transitionName, currentModel.getObject());
    }

    @Override
    protected IModel<?> getContentTitleModel() {
        return new ResourceModel("label.form.content.title", "Nova Solicitação");
    }

    @Override
    protected IModel<?> getContentSubtitleModel() {
        return new Model<String>() {
            @Override
            public String getObject() {
                if (Optional.ofNullable(currentModel)
                        .map(IModel::getObject)
                        .map(Petition::getProcessInstanceEntity)
                        .map(ProcessInstanceEntity::getCod).isPresent()) {
                    return StringUtils.EMPTY;
                } else {
                    return new ResourceModel("label.form.content.subtitle", "Solicitação").getObject();
                }
            }
        };
    }

    @Override
    protected String getFormXML() {
        return currentModel.getObject().getXml();
    }

    @Override
    protected void setFormXML(String xml) {
        currentModel.getObject().setXml(xml);
    }

    @Override
    protected ProcessInstanceEntity getProcessInstance() {
        return currentModel.getObject().getProcessInstanceEntity();
    }

    @Override
    protected void setProcessInstance(ProcessInstanceEntity pie) {
        currentModel.getObject().setProcessInstanceEntity(pie);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void saveForm(IModel<? extends SInstance> currentInstance) {
        petitionService.saveOrUpdate(updateDescription(currentInstance));
    }

    @Override
    protected void send(IModel<? extends SInstance> currentInstance, MElement xml) {
        petitionService.send(updateDescription(currentInstance));
    }

    private Petition updateDescription(IModel<? extends SInstance> currentInstance){
        Petition petition = currentModel.getObject();
        if (currentInstance.getObject() instanceof SIComposite) {
            petition.setDescription(((SIComposite) currentInstance.getObject()).toStringDisplay());
        }
        return petition;
    }

    @Override
    protected void loadOrCreateFormModel(String formId, String type, ViewMode viewMode, AnnotationMode annotationMode) {
        Petition peticao;
        if (formId == null || formId.isEmpty()) {
            peticao = new Petition();
            peticao.setType(type);
            singularServerConfiguration.processDefinitionFormNameMap().forEach((key, value) -> {
                if (value.equals(type)) {
                    peticao.setProcessType(Flow.getProcessDefinition(key).getKey());
                }
            });
            peticao.setCreationDate(new Date());
            peticao.setDescription("Nova Solicitação");
            peticao.setProcessName(recuperarNomeProcesso(type));

        } else {
            peticao = (Petition) petitionService.find(Long.valueOf(formId));
        }

        currentModel = $m.ofValue(peticao);
    }

    private String recuperarNomeProcesso(String typeName) {
        STypeComposite<?> canabidiol = (STypeComposite<?>) singularFormConfig
                .getTypeLoader().loadType(typeName).orElseThrow(() -> new SingularServerException("Não foi possivel carregar o tipo"));
        return canabidiol.as(AtrBasic::new).getLabel();
    }

    @Override
    protected IModel<? extends Petition> getFormModel() {
        return currentModel;
    }

    @Override
    protected String getAnnotationsXML(IModel<?> model) {
        return currentModel.getObject().getAnnotations();
    }

    @Override
    protected void setAnnotationsXML(IModel<?> model, String xml) {
        currentModel.getObject().setAnnotations(xml);
    }

    @Override
    protected boolean hasProcess() {
        return currentModel.getObject().getProcessInstanceEntity() != null;
    }


    @Override
    protected String getIdentifier() {
        return String.valueOf(Optional.ofNullable(currentModel)
                .map(IModel::getObject)
                .map(Petition::getCod).orElse(null));

    }
}
