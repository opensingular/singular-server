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
package org.opensingular.app.commons.mail.service.email;

import org.opensingular.app.commons.mail.exception.SingularMailException;
import org.opensingular.app.commons.mail.persistence.dao.EmailAddresseeDao;
import org.opensingular.app.commons.mail.persistence.dao.EmailDao;
import org.opensingular.app.commons.mail.persistence.entity.email.EmailAddresseeEntity;
import org.opensingular.app.commons.mail.persistence.entity.email.EmailEntity;
import org.opensingular.app.commons.mail.service.dto.Email;
import org.opensingular.form.document.SDocument;
import org.opensingular.form.persistence.entity.AttachmentContentEntity;
import org.opensingular.form.persistence.entity.AttachmentEntity;
import org.opensingular.form.persistence.service.AttachmentPersistenceService;
import org.opensingular.form.type.core.attachment.IAttachmentRef;
import org.opensingular.form.validation.SingularEmailValidator;
import org.opensingular.lib.commons.base.SingularProperties;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensingular.lib.commons.base.SingularProperties.EMAIL_COD_MODULE;


@Transactional(Transactional.TxType.MANDATORY)
public class EmailPersistenceService implements IEmailService<Email> {

    @Inject
    private EmailDao<EmailEntity> emailDao;

    @Inject
    private EmailAddresseeDao<EmailAddresseeEntity> emailAddresseeDao;

    @Inject
    @Named(SDocument.FILE_PERSISTENCE_SERVICE)
    private AttachmentPersistenceService<AttachmentEntity, AttachmentContentEntity> persistenceHandler;

    @Override
    public boolean send(Email email) {
        EmailEntity emailEntity = new EmailEntity();
        if (!validateRecipients(email.getAllRecipients())) {
            throw new SingularMailException("O destinatário de e-mail é inválido.");
        }
        emailEntity.setSubject(email.getSubject());
        emailEntity.setContent(email.getContent());
        emailEntity.setReplyTo(email.getReplyToJoining());

        String emailIdentifier = Optional.ofNullable(email.getModuleCod())
                .orElse(SingularProperties.get().getPropertyOpt(EMAIL_COD_MODULE).orElse(null));
        emailEntity.setModule(emailIdentifier);

        for (IAttachmentRef attachmentRef : email.getAttachments()) {
            IAttachmentRef attachment = persistenceHandler.copy(attachmentRef, null).getNewAttachmentRef();
            emailEntity.getAttachments().add(persistenceHandler.getAttachmentEntity(attachment));
        }
        emailEntity.setCreationDate(new Date());
        emailDao.save(emailEntity);

        for (Email.Addressee addressee : email.getAllRecipients()) {
            EmailAddresseeEntity addresseeEntity = new EmailAddresseeEntity();
            addresseeEntity.setAddress(addressee.getAddress());
            addresseeEntity.setAddresseType(addressee.getType());
            addresseeEntity.setEmail(emailEntity);

            emailAddresseeDao.save(addresseeEntity);
        }
        return true;
    }

    private boolean validateRecipients(List<Email.Addressee> recipients) {
        for (Email.Addressee addressee : recipients) {
            if (!SingularEmailValidator.getInstance(false).isValid(addressee.getAddress())) {
                return false;
            }
        }
        return !recipients.isEmpty();
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void markAsSent(Email.Addressee addressee) {
        EmailAddresseeEntity entity = emailAddresseeDao.findOrException(addressee.getCod());
        entity.setSentDate(new Date());
        emailAddresseeDao.saveOrUpdate(entity);

        addressee.setSentDate(entity.getSentDate());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int countPendingRecipients() {
        return emailAddresseeDao.countPending();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Email.Addressee> listPendingRecipients(int firstResult, int maxResults, @Nullable String codModule) {
        return emailAddresseeDao.listPending(firstResult, maxResults)
                .stream()
                .filter(s -> codModule == null || codModule.equals(s.getEmail().getModule()))
                .map(addressee -> {
            Email email = new Email();
            email.withSubject(addressee.getEmail().getSubject());
            email.withContent(addressee.getEmail().getContent());
            email.addReplyTo(addressee.getEmail().getReplyTo());
            email.setCreationDate(addressee.getEmail().getCreationDate());

            for (AttachmentEntity attachmentEntity : addressee.getEmail().getAttachments()) {
                email.addAttachments(persistenceHandler.createRef(attachmentEntity));
            }

            return new Email.Addressee(email, addressee);
        }).collect(Collectors.toList());
    }
}
