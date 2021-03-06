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
package org.opensingular.app.commons.mail.persistence.dao;

import org.opensingular.app.commons.mail.persistence.entity.email.EmailEntity;
import org.opensingular.lib.support.persistence.BaseDAO;

import javax.transaction.Transactional;

@SuppressWarnings("unchecked")
@Transactional(Transactional.TxType.MANDATORY)
public class EmailDao<T extends EmailEntity> extends BaseDAO<T, Long>{
    
    public EmailDao() {
        super((Class<T>) EmailEntity.class);
    }

    public EmailDao(Class<T> entityClass) {
        super(entityClass);
    }

}
