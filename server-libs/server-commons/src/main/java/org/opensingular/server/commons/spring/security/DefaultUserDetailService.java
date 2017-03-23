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

package org.opensingular.server.commons.spring.security;


import org.opensingular.flow.core.SUser;
import org.opensingular.server.commons.config.IServerContext;
import org.opensingular.server.commons.config.SingularServerConfiguration;
import org.opensingular.server.commons.persistence.dao.flow.ActorDAO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DefaultUserDetailService implements SingularUserDetailsService {

    @Inject
    private ActorDAO actorDAO;

    @Inject
    private SingularServerConfiguration singularServerConfiguration;

    @Override
    public SingularUserDetails loadUserByUsername(String username, IServerContext context) throws UsernameNotFoundException {
        SUser user = actorDAO.buscarPorCodUsuario(username);
        return new DefaultUserDetails(username, new ArrayList<>(), Optional.ofNullable(user).map(SUser::getSimpleName).orElse(username), context);
    }

    @Override
    public IServerContext[] getContexts() {
        return singularServerConfiguration.getContexts();
    }

    @Override
    public List<SingularPermission> searchPermissions(String idUsuarioLogado) {
        return Collections.emptyList();
    }
}
