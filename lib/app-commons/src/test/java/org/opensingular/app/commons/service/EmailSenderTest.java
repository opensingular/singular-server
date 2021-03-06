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

package org.opensingular.app.commons.service;

import org.junit.Assert;
import org.junit.Test;
import org.opensingular.app.commons.mail.persistence.entity.email.EmailAddresseeEntity;
import org.opensingular.app.commons.mail.service.dto.Email;
import org.opensingular.app.commons.mail.service.email.EmailSender;
import org.opensingular.app.commons.mail.service.email.EmailSenderScheduledJob;
import org.opensingular.app.commons.test.SpringBaseTest;
import org.opensingular.lib.commons.util.Loggable;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;

import static org.opensingular.app.commons.service.EmailTestMocks.*;
public class EmailSenderTest extends SpringBaseTest implements Loggable {

    @Inject
    private EmailSenderScheduledJob emailSenderJob;

    @Inject
    private EmailSender emailSender;

    @Test
    @Transactional
    public void testJobWithoutEmailToSend() {
        emailSenderJob.setEmailsPerPage(20);

        Object run = emailSenderJob.run();
        Assert.assertEquals("0 sent from total of 0", run);

        Assert.assertEquals("EmailSenderScheduledJob [getScheduleData()="
                        + emailSenderJob.getScheduleData().toString() + ", getId()=" + emailSenderJob.getId() + "]",
                emailSenderJob.toString());
    }

    @Test
    public void testNotSendEmail() {
        emailSender.setHost(null);
        Assert.assertFalse(emailSender.send((Email.Addressee) null));
    }

    @Test
    public void sendEmailExceptionTest() {
        emailSender.setHost("opensingular.org");

        Date date = new Date();

        Email email = createMockEmail();
        Assert.assertNull(email.getCreationDate());

        EmailAddresseeEntity entity = createMockEmailAddresseeEntity(date);

        Email.Addressee addressee = new Email.Addressee(email, entity);

        addressee.setSentDate(date);
        Assert.assertEquals(date, addressee.getSentDate());
        addressee.setSentDate(null);

        emailSender.setPort(null);
        Assert.assertEquals(-1, emailSender.getPort());

        emailSender.setPort("8080");
        Assert.assertEquals(8080, emailSender.getPort());

        emailSender.setUsername("test@email.com");

        Assert.assertFalse(emailSender.send(addressee));

    }

    @Test
    public void testEmailAddresseEntity() {
        Date                 date   = new Date();
        EmailAddresseeEntity entity = createMockEmailAddresseeEntity(date);

        entity.setSentDate(date);
        Assert.assertEquals(date, entity.getSentDate());

        Assert.assertEquals(new Long(1), entity.getCod());

        Assert.assertEquals("opensingular@gmail.com", entity.getAddress());

    }
}
