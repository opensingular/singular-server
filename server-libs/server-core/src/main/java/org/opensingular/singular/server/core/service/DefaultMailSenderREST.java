/*
 * Copyright (c) 2016, Mirante and/or its affiliates. All rights reserved.
 * Mirante PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package org.opensingular.singular.server.core.service;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.opensingular.flow.schedule.IScheduleService;
import org.opensingular.singular.server.commons.service.IMailSenderREST;
import org.opensingular.singular.support.spring.util.AutoScanDisabled;

@AutoScanDisabled
@RequestMapping(IMailSenderREST.PATH)
@RestController
public class DefaultMailSenderREST implements IMailSenderREST{

    @Inject
    private IScheduleService scheduleService;
    
    @Inject
    private EmailSenderScheduledJob emailSenderScheduledJob;
    
    @RequestMapping(value = PATH_SEND_ALL, method = RequestMethod.GET)
    public boolean sendAll() {
        try {
            scheduleService.trigger(emailSenderScheduledJob);
            return true;
        } catch (Exception e) {
            getLogger().error("Erro ao disparar envio de email", e);
            return false;
        }
    }

}