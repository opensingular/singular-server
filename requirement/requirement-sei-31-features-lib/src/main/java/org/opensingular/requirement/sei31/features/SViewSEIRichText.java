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

package org.opensingular.requirement.sei31.features;

import java.util.Objects;
import javax.annotation.Nonnull;

import org.opensingular.form.view.richtext.SViewByRichTextNewTab;
import org.opensingular.lib.commons.lambda.IFunction;

public class SViewSEIRichText extends SViewByRichTextNewTab {

    private SViewSEIRichText() {
        //This class is responsible for compatibility with SEI
        getConfiguration().setDoubleClickDisabledForCssClasses("ancoraSei");
    }

    public static ModeloSEIActionBuilder configProtocoloToIdSEIAction(@Nonnull IFunction<SILinkSEI, String> functionActionLink) {
        Objects.requireNonNull(functionActionLink, "Action Link Function must not be null!");
        SViewSEIRichText view = new SViewSEIRichText();
        view.configureProtocoloToIdSEIAction(functionActionLink);
        return new ModeloSEIActionBuilder(view);
    }

    private void configureProtocoloToIdSEIAction(IFunction<SILinkSEI, String> functionActionLink) {
        this.addAction(new InsertLinkSEIButtonRichText(functionActionLink));
    }


    void configureModeloSeiAction(IFunction<SIModeloSEI, String> functionActionLink) {
        this.addAction(new InsertModeloSEIButtonRichText(functionActionLink));
    }
}
