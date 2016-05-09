package br.net.mirante.singular.server.commons.wicket.view.form;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;

import br.net.mirante.singular.flow.core.MTransition;
import br.net.mirante.singular.form.RefService;
import br.net.mirante.singular.form.SInstance;
import br.net.mirante.singular.form.context.SFormConfig;
import br.net.mirante.singular.form.document.RefSDocumentFactory;
import br.net.mirante.singular.form.document.RefType;
import br.net.mirante.singular.form.document.SDocument;
import br.net.mirante.singular.form.document.SDocumentFactory;
import br.net.mirante.singular.form.document.ServiceRegistry;
import br.net.mirante.singular.form.internal.xml.MElement;
import br.net.mirante.singular.form.io.MformPersistenciaXML;
import br.net.mirante.singular.form.type.core.annotation.AtrAnnotation;
import br.net.mirante.singular.form.type.core.annotation.SIAnnotation;
import br.net.mirante.singular.form.wicket.component.SingularButton;
import br.net.mirante.singular.form.wicket.component.SingularValidationButton;
import br.net.mirante.singular.form.wicket.enums.AnnotationMode;
import br.net.mirante.singular.form.wicket.enums.ViewMode;
import br.net.mirante.singular.form.wicket.panel.SingularFormPanel;
import br.net.mirante.singular.persistence.entity.ProcessInstanceEntity;
import br.net.mirante.singular.server.commons.flow.metadata.ServerContextMetaData;
import br.net.mirante.singular.server.commons.form.SingularServerDocumentFactory;
import br.net.mirante.singular.server.commons.wicket.SingularSession;
import br.net.mirante.singular.server.commons.wicket.view.template.Content;
import br.net.mirante.singular.util.wicket.bootstrap.layout.BSContainer;
import br.net.mirante.singular.util.wicket.modal.BSModalBorder;
import br.net.mirante.singular.util.wicket.model.IReadOnlyModel;

public abstract class AbstractFormContent extends Content {


    protected final BSContainer modalContainer = new BSContainer("modals");
    protected final String formId;
    protected final String typeName;
    private final BSModalBorder closeModal = construirCloseModal();
    protected IModel<String> msgFlowModel = new Model<String>();
    protected IModel<String> transitionNameModel = new Model<String>();
    protected ViewMode viewMode = ViewMode.EDITION;
    protected AnnotationMode annotationMode = AnnotationMode.NONE;
    protected SingularFormPanel<String> singularFormPanel;
    @Inject
    @Named("formConfigWithDatabase")
    protected SFormConfig<String> singularFormConfig;

    public AbstractFormContent(String idWicket, String type, String formId, ViewMode viewMode, AnnotationMode annotationMode) {
        super(idWicket, false, false);
        this.viewMode = viewMode;
        this.annotationMode = annotationMode;
        this.typeName = type;
        this.formId = formId;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        add(buildForm());
    }

    private Form<?> buildForm() {
        loadOrCreateFormModel(formId, typeName, viewMode, annotationMode);
        Form<?> form = new Form<>("save-form");
        form.setMultiPart(true);
        form.add(buildSingularBasePanel());
        form.add(modalContainer);
        BSModalBorder enviarModal = buildConfirmationModal(modalContainer, getInstanceModel());
        form.add(buildSendButton(enviarModal));
        form.add(buildSaveButton());
        form.add(buildSaveAnnotationButton());
        form.add(buildFlowButtons());
        form.add(buildValidateButton());
        form.add(buildCloseButton());
        form.add(closeModal);
        return form;
    }

    private IReadOnlyModel<SInstance> getInstanceModel() {
        return (IReadOnlyModel<SInstance>) () -> Optional
                .ofNullable(singularFormPanel)
                .map(SingularFormPanel::getRootInstance)
                .map(IModel::getObject)
                .orElse(null);
    }

    private Component buildFlowButtons() {
        BSContainer container = new BSContainer("custom-buttons");
        container.setVisible(true);
        List<MTransition> trans = currentTaskTransitions(formId);
        if (CollectionUtils.isNotEmpty(trans) && (ViewMode.EDITION.equals(viewMode) || AnnotationMode.EDIT.equals(annotationMode))) {
            int index = 0;
            for (MTransition t : trans) {
                if (t.getMetaDataValue(ServerContextMetaData.KEY) != null
                        && t.getMetaDataValue(ServerContextMetaData.KEY).isEnabledOn(SingularSession.get().getServerContext())) {
                    String btnId = "flow-btn" + index;
                    buildFlowTransitionButton(
                            btnId,
                            container,
                            modalContainer,
                            t.getName(),
                            getInstanceModel(),
                            viewMode);
                }
            }
        } else {
            container.setVisible(false);
            container.setEnabled(false);
        }
        return container;
    }

    protected abstract void buildFlowTransitionButton(String buttonId, BSContainer buttonContainer, BSContainer modalContainer, String transitionName, IModel<? extends SInstance> instanceModel, ViewMode viewMode);


    protected abstract List<MTransition> currentTaskTransitions(String formId);


    public interface ProcessFormService extends Serializable {
        ProcessInstanceEntity getProcessInstance();
    }

    private SingularFormPanel buildSingularBasePanel() {
        singularFormPanel = new SingularFormPanel<String>("singular-panel", singularFormConfig) {

            @Override
            protected SInstance createInstance(SFormConfig<String> singularFormConfig) {
                RefType refType = singularFormConfig.getTypeLoader().loadRefTypeOrException(typeName);
                String xml = getFormXML();
                SInstance instance = createInstance(singularFormConfig, refType, xml);
                return instance;
            }

            private SInstance createInstance(SFormConfig<String> singularFormConfig, RefType refType, String xml) {
//                SDocumentFactory documentFactory = singularFormConfig.getDocumentFactory();
                SDocumentFactory documentFactory = new TaskAwareDocumentFactory(AbstractFormContent.this);
                if (StringUtils.isBlank(xml)) {
                    return documentFactory.createInstance(refType);
                } else {
                    SInstance instance = MformPersistenciaXML.fromXML(refType, xml, documentFactory);
                    MformPersistenciaXML.annotationLoadFromXml(instance, getAnnotationsXML(getFormModel()));
                    return instance;
                }
            }

            @Override
            public ViewMode getViewMode() {
                return viewMode;
            }

            @Override
            public AnnotationMode annotation() {
                return annotationMode;
            }
        };

        return singularFormPanel;
    }

    private Component buildSendButton(final BSModalBorder enviarModal) {
        final Component button = new SingularButton("send-btn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                enviarModal.show(target);
            }

            @Override
            public IModel<? extends SInstance> getCurrentInstance() {
                return singularFormPanel.getRootInstance();
            }


        };
        return button.add(visibleOnlyIfDraftInEditionBehaviour());
    }

    private Component buildSaveButton() {
        final Component button = new SingularButton("save-btn") {
            @Override
            public IModel<? extends SInstance> getCurrentInstance() {
                return singularFormPanel.getRootInstance();
            }

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                MElement rootXml = MformPersistenciaXML.toXML(getCurrentInstance().getObject());
                setFormXML(Optional.ofNullable(rootXml).map(MElement::toStringExato).orElse(null));
                processAnnotations(getCurrentInstance().getObject());
                getCurrentInstance().getObject().getDocument().persistFiles();
                try{
                    saveForm(getCurrentInstance());
                    addToastrSuccessMessage("message.success");
                    atualizarContentWorklist(target);
                }catch (HibernateOptimisticLockingFailureException e){
                    addToastrErrorMessage("message.save.concurrent_error");
                }
            }
        };
        return button.add(visibleOnlyInEditionBehaviour());
    }

    protected void atualizarContentWorklist(AjaxRequestTarget target) {
        target.appendJavaScript("Singular.atualizarContentWorklist();");
    }


    private Component buildSaveAnnotationButton() {
        final Component button = new SingularValidationButton("save-annotation-btn") {
            @Override
            public IModel<? extends SInstance> getCurrentInstance() {
                return singularFormPanel.getRootInstance();
            }

            protected void save(AjaxRequestTarget target) {
                getCurrentInstance().getObject().getDocument().persistFiles();
                processAnnotations(getCurrentInstance().getObject());
                saveForm(getFormModel());
                atualizarContentWorklist(target);
            }

            @Override
            protected void onValidationSuccess(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                try {
                    save(target);
                    addToastrSuccessMessage("message.success");
                }catch (HibernateOptimisticLockingFailureException e){
                    addToastrErrorMessage("message.save.concurrent_error");
                }
            }

            @Override
            protected void onValidationError(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                save(target);
            }
        };
        return button.add(visibleOnlyInAnnotationBehaviour());
    }

    protected void processAnnotations(SInstance instancia) {
        AtrAnnotation annotatedInstance = instancia.as(AtrAnnotation::new);
        List<SIAnnotation> allAnnotations = annotatedInstance.allAnnotations();
        if (!allAnnotations.isEmpty()) {
            Optional<String> annXml = annotationsToXml(annotatedInstance);
            setAnnotationsXML(getFormModel(), annXml.orElse(""));
        }
    }

    private Optional<String> annotationsToXml(AtrAnnotation annotatedInstance) {
        return MformPersistenciaXML.toStringXML(annotatedInstance.persistentAnnotations());
    }

    @SuppressWarnings("rawtypes")
    protected AjaxLink<?> buildCloseButton() {
        return new AjaxLink("close-btn") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (isReadOnly()) {
                    atualizarContentWorklist(target);
                    target.appendJavaScript("window.close()");
                } else {
                    closeModal.show(target);
                }
            }
        };
    }

    private boolean isReadOnly() {
        return viewMode == ViewMode.VISUALIZATION
                && annotationMode != AnnotationMode.EDIT;
    }

    protected BSModalBorder construirCloseModal() {
        BSModalBorder closeModal = new BSModalBorder("close-modal", getMessage("label.title.close.draft"));
        closeModal.addButton(BSModalBorder.ButtonStyle.EMPTY, "label.button.cancel", new AjaxButton("cancel-close-btn") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                closeModal.hide(target);
            }
        });
        closeModal.addButton(BSModalBorder.ButtonStyle.DANGER, "label.button.confirm", new AjaxButton("close-btn") {
            @Override
            protected String getOnClickScript() {
                return " Singular.atualizarContentWorklist();"
                        + "window.close();";
            }
        });

        return closeModal;
    }

    protected Component buildValidateButton() {
        final SingularValidationButton button = new SingularValidationButton("validate-btn") {

            @Override
            protected void onValidationSuccess(AjaxRequestTarget target, Form<?> form,
                                               IModel<? extends SInstance> instanceModel) {
                addToastrSuccessMessage("message.validation.success");
            }

            @Override
            protected void onValidationError(AjaxRequestTarget target, Form<?> form, IModel<? extends SInstance> instanceModel) {
                super.onValidationError(target, form, instanceModel);
                addToastrErrorMessage("message.validation.error");
            }

            @Override
            public IModel<? extends SInstance> getCurrentInstance() {
                return singularFormPanel.getRootInstance();
            }
        };

        return button.add(visibleOnlyInEditionBehaviour());
    }

    protected abstract BSModalBorder buildConfirmationModal(BSContainer modalContainer, IModel<? extends SInstance> instanceModel);

    protected Behavior visibleOnlyInEditionBehaviour() {
        return new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                component.setVisible(viewMode.isEdition());
            }
        };
    }

    protected Behavior visibleOnlyIfDraftInEditionBehaviour() {
        return new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                component.setVisible(!hasProcess() && viewMode.isEdition());
            }
        };
    }

    protected Behavior visibleOnlyInAnnotationBehaviour() {
        return new Behavior() {
            @Override
            public void onConfigure(Component component) {
                super.onConfigure(component);

                component.setVisible(annotationMode.editable());
            }
        };
    }

    protected abstract String getFormXML();

    protected abstract void setFormXML(String xml);

    protected abstract ProcessInstanceEntity getProcessInstance();

    protected abstract void setProcessInstance(ProcessInstanceEntity pie);

    protected abstract void saveForm(IModel<?> currentInstance);

    protected abstract void send(IModel<? extends SInstance> currentInstance, MElement xml);

    protected abstract void loadOrCreateFormModel(String formId, String type, ViewMode viewMode, AnnotationMode annotationMode);

    protected abstract IModel<?> getFormModel();

    protected abstract String getAnnotationsXML(IModel<?> model);

    protected abstract void setAnnotationsXML(IModel<?> model, String xml);

    protected abstract boolean hasProcess();

    protected abstract String getIdentifier();
}

/**
 * This DocumentFactory is just a delagate which sets a Aware Service for
 * accessing the current ProcessInstance.
 */
class TaskAwareDocumentFactory extends SingularServerDocumentFactory
        implements Serializable {

    AbstractFormContent content;

    public TaskAwareDocumentFactory( AbstractFormContent content){
        this.content = content;
    }

    SDocumentFactory target() { return content.singularFormConfig.getDocumentFactory();}

    ProcessInstanceEntity instance() { return content.getProcessInstance();}

    @Override
    public RefSDocumentFactory getDocumentFactoryRef() {
        return target().getDocumentFactoryRef();
    }

    @Override
    public ServiceRegistry getServiceRegistry() {
        return target().getServiceRegistry();
    }

    @Override
    protected void setupDocument(SDocument document) {
        super.setupDocument(document);
        //TODO: Fabs this does not work for init instance
        document.bindLocalService("processService",AbstractFormContent.ProcessFormService.class, RefService.of(
                new AbstractFormContent.ProcessFormService(){
                    @Override public ProcessInstanceEntity getProcessInstance() {
                        return instance();
                    }
                })
        );
    }
}