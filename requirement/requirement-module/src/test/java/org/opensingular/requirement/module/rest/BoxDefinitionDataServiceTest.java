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

package org.opensingular.requirement.module.rest;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opensingular.lib.commons.context.SingularContextSetup;
import org.opensingular.lib.support.spring.util.ApplicationContextProvider;
import org.opensingular.requirement.module.DefaultActionProvider;
import org.opensingular.requirement.module.connector.DefaultModuleService;
import org.opensingular.requirement.module.persistence.filter.BoxFilter;
import org.opensingular.requirement.module.provider.RequirementBoxItemDataProvider;
import org.opensingular.requirement.module.service.dto.ItemBox;
import org.opensingular.requirement.module.spring.security.AuthorizationService;
import org.opensingular.requirement.module.workspace.BoxDefinition;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class BoxDefinitionDataServiceTest {

    @Mock
    private ApplicationContext context;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private DefaultModuleService moduleService;

    @Mock
    private BoxDefinition boxDefinition;

    private BoxFilter boxFilter;

    private Long countSize = 1L;

    private String boxId = "123456";

    public void setUpApplicationContextMock() {

        ApplicationContextProvider applicationContextProvider = new ApplicationContextProvider();
        applicationContextProvider.setApplicationContext(context);
        when(context.getBean(AuthorizationService.class)).thenReturn(authorizationService);

    }

    @Before
    public void setUp() {
        SingularContextSetup.reset();
        RequirementBoxItemDataProvider boxItemDataProvider = mock(RequirementBoxItemDataProvider.class);
        List<Map<String, Serializable>> searchResult = new ArrayList<>();
        Map<String, Serializable> firstItemMap = new HashMap<>();
        searchResult.add(firstItemMap);
        firstItemMap.put("id", "123456");

        boxFilter = new BoxFilter();

        when(boxItemDataProvider.count(eq(boxFilter))).thenReturn(countSize);
        when(boxItemDataProvider.search(eq(boxFilter))).thenReturn(searchResult);
        when(boxItemDataProvider.getActionProvider()).thenReturn(new DefaultActionProvider());

        when(boxDefinition.getDataProvider()).thenReturn(boxItemDataProvider);

        setUpApplicationContextMock();
    }

    @Test
    public void testCount() {
        assertThat(moduleService.countFiltered(boxDefinition, boxFilter), Matchers.is(countSize));
    }

    @Test
    public void testSearch() {
        assertThat(moduleService.searchFiltered(boxDefinition, boxFilter).size(), Matchers.is(countSize.intValue()));
    }

}