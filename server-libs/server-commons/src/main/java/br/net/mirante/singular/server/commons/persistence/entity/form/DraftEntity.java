package br.net.mirante.singular.server.commons.persistence.entity.form;

import br.net.mirante.singular.form.persistence.entity.FormEntity;
import br.net.mirante.singular.persistence.entity.ProcessDefinitionEntity;
import br.net.mirante.singular.server.commons.persistence.entity.enums.PersonType;
import br.net.mirante.singular.support.persistence.entity.BaseEntity;
import br.net.mirante.singular.support.persistence.enums.SimNao;
import br.net.mirante.singular.support.persistence.util.Constants;
import br.net.mirante.singular.support.persistence.util.GenericEnumUserType;
import br.net.mirante.singular.support.persistence.util.HybridIdentityOrSequenceGenerator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Entity
@Table(schema = Constants.SCHEMA, name = "TB_RASCUNHO")
@GenericGenerator(name = DraftEntity.PK_GENERATOR_NAME, strategy = HybridIdentityOrSequenceGenerator.CLASS_NAME)
public class DraftEntity extends BaseEntity<Long> {

    public static final String PK_GENERATOR_NAME = "GENERATED_CO_RASCUNHO";

    @Id
    @Column(name = "CO_RASCUNHO")
    @GeneratedValue(generator = PK_GENERATOR_NAME)
    private Long cod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CO_FORMULARIO")
    private FormEntity form;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_INICIO")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DT_EDICAO")
    private Date editionDate;

    @Override
    public Long getCod() {
        return cod;
    }

    public void setCod(Long cod) {
        this.cod = cod;
    }

    public FormEntity getForm() {
        return form;
    }

    public void setForm(FormEntity form) {
        this.form = form;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEditionDate() {
        return editionDate;
    }

    public void setEditionDate(Date editionDate) {
        this.editionDate = editionDate;
    }

}