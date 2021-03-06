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

package org.opensingular.requirement.commons.admin.healthsystem;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opensingular.requirement.commons.SingularCommonsBaseTest;
import org.opensingular.requirement.module.persistence.dto.healthsystem.HealthInfoDTO;
import org.opensingular.requirement.module.service.HealthSystemDbService;

public class HealthSystemDBService extends SingularCommonsBaseTest {
    @Inject
    private HealthSystemDbService service;

    @Test
    @Ignore("Adapt DB MetaData to Hibernate 5: https://vladmihalcea.com/how-to-get-access-to-database-table-metadata-with-hibernate-5/")
    public void getAllDbMetaDataTest(){
        HealthInfoDTO allDbMetaData = service.getAllDbMetaData();

        Assert.assertNotNull(allDbMetaData);
        Assert.assertNotNull(allDbMetaData.getTablesList());
    }
}
