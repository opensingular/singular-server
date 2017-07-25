package org.opensingular.server.commons.test;

import org.opensingular.flow.core.DefinitionInfo;
import org.opensingular.flow.core.FlowMap;
import org.opensingular.flow.core.ITaskDefinition;
import org.opensingular.flow.core.FlowDefinition;
import org.opensingular.flow.core.FlowInstance;
import org.opensingular.flow.core.builder.FlowBuilder;
import org.opensingular.flow.core.builder.FlowBuilderImpl;
import org.opensingular.flow.core.defaults.PermissiveTaskAccessStrategy;
import org.opensingular.server.commons.flow.SingularRequirementTaskPageStrategy;
import org.opensingular.server.commons.wicket.view.form.FormPage;

import javax.annotation.Nonnull;

@DefinitionInfo("fooFlowWithTransitionCommons")
public class FOOFlowWithTransition extends FlowDefinition<FlowInstance> {

    public FOOFlowWithTransition() {
        super(FlowInstance.class);
    }

    @Nonnull
    @Override
    protected FlowMap createFlowMap() {
        FlowBuilder flow = new FlowBuilderImpl(this);

        ITaskDefinition startbarDef  = () -> "Start bar";
        ITaskDefinition middlebarDef = () -> "Transition bar";
        ITaskDefinition endbarDef = () -> "End bar";

        flow.addHumanTask(startbarDef)
                .withExecutionPage(SingularRequirementTaskPageStrategy.of(FormPage.class))
                .uiAccess(new PermissiveTaskAccessStrategy());

        flow.addHumanTask(middlebarDef)
                .withExecutionPage(SingularRequirementTaskPageStrategy.of(FormPage.class))
                .uiAccess(new PermissiveTaskAccessStrategy());

        flow.addEndTask(endbarDef);

        flow.setStartTask(startbarDef);

        flow.from(startbarDef).go(middlebarDef);
        flow.from(middlebarDef).go(endbarDef);

        return flow.build();
    }
}
