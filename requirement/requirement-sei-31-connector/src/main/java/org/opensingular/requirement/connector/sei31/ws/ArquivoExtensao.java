
package org.opensingular.requirement.connector.sei31.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java de ArquivoExtensao complex type.
 * 
 * <p>O seguinte fragmento do esquema especifica o conteúdo esperado contido dentro desta classe.
 * 
 * <pre>
 * &lt;complexType name="ArquivoExtensao">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="IdArquivoExtensao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Extensao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Descricao" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/all>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArquivoExtensao", propOrder = {

})
public class ArquivoExtensao {

    @XmlElement(name = "IdArquivoExtensao", required = true)
    protected String idArquivoExtensao;
    @XmlElement(name = "Extensao", required = true)
    protected String extensao;
    @XmlElement(name = "Descricao", required = true)
    protected String descricao;

    /**
     * Obtém o valor da propriedade idArquivoExtensao.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdArquivoExtensao() {
        return idArquivoExtensao;
    }

    /**
     * Define o valor da propriedade idArquivoExtensao.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdArquivoExtensao(String value) {
        this.idArquivoExtensao = value;
    }

    /**
     * Obtém o valor da propriedade extensao.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExtensao() {
        return extensao;
    }

    /**
     * Define o valor da propriedade extensao.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExtensao(String value) {
        this.extensao = value;
    }

    /**
     * Obtém o valor da propriedade descricao.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Define o valor da propriedade descricao.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescricao(String value) {
        this.descricao = value;
    }

}
