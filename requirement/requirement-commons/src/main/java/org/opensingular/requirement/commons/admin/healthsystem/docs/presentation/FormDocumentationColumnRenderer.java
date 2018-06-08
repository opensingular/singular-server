/*
 *
 *  * Copyright (C) 2016 Singular Studios (a.k.a Atom Tecnologia) - www.opensingular.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.opensingular.requirement.commons.admin.healthsystem.docs.presentation;

import org.opensingular.requirement.commons.admin.healthsystem.docs.DocBlock;
import org.opensingular.requirement.commons.admin.healthsystem.docs.DocFieldMetadata;
import org.opensingular.requirement.commons.admin.healthsystem.docs.DocTable;

import javax.annotation.Nullable;

public interface FormDocumentationColumnRenderer {

    String getColumnName();

    /**
     * Render documentation column
     *
     * @param fieldMetadata
     *
     *
     * @return
     */
    String renderColumn(DocTable table, DocBlock block, @Nullable DocFieldMetadata fieldMetadata);

}