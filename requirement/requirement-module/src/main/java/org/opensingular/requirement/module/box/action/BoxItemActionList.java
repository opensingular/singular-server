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

package org.opensingular.requirement.module.box.action;


import java.util.ArrayList;

import org.opensingular.requirement.module.box.BoxItemData;
import org.opensingular.requirement.module.box.action.defaults.AnalyseAction;
import org.opensingular.requirement.module.box.action.defaults.AssignAction;
import org.opensingular.requirement.module.box.action.defaults.DeleteAction;
import org.opensingular.requirement.module.box.action.defaults.EditAction;
import org.opensingular.requirement.module.box.action.defaults.ExtratoAction;
import org.opensingular.requirement.module.box.action.defaults.HistoryAction;
import org.opensingular.requirement.module.box.action.defaults.RelocateAction;
import org.opensingular.requirement.module.box.action.defaults.ViewAction;
import org.opensingular.requirement.module.service.dto.BoxItemAction;

public class BoxItemActionList extends ArrayList<BoxItemAction> {


    public BoxItemActionList addViewAction(BoxItemData line) {
        addAction(new ViewAction(line));
        return this;
    }

    public BoxItemActionList addDeleteAction(BoxItemData line) {
        addAction(new DeleteAction(line));
        return this;
    }

    public BoxItemActionList addAssignAction(BoxItemData line) {
        addAction(new AssignAction(line));
        return this;
    }


    public BoxItemActionList addRelocateAction(BoxItemData line) {
        addAction(new RelocateAction(line));
        return this;
    }

    public BoxItemActionList addAnalyseAction(BoxItemData line) {
        addAction(new AnalyseAction(line));
        return this;
    }

    public BoxItemActionList addEditAction(BoxItemData line) {
        addAction(new EditAction(line));
        return this;
    }


    public BoxItemActionList addHistoryAction(BoxItemData line) {
        addAction(new HistoryAction(line));
        return this;
    }

    /**
     * Method responsible for create the extrato action. This will generate a UUID for the id of the requiriment,
     * and will add a action for the read mode PDF of the requiriment form.
     *
     * @param line            The item containg the requiriment id.
     * @return <code>this</code>
     */
    public BoxItemActionList addExtratoAction(BoxItemData line) {
        addAction(new ExtratoAction(line.getRequirementId()));
        return this;
    }

    public BoxItemActionList addAction(BoxItemAction boxItemAction) {
        add(boxItemAction);
        return this;
    }
}