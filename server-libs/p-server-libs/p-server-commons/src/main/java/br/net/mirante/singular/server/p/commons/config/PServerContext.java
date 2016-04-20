package br.net.mirante.singular.server.p.commons.config;

import br.net.mirante.singular.server.commons.config.ConfigProperties;
import br.net.mirante.singular.server.commons.config.IServerContext;
import br.net.mirante.singular.server.commons.exception.SingularServerException;
import org.apache.wicket.request.Request;

import javax.servlet.http.HttpServletRequest;

/**
 * Utilitário para prover a configuração de contexto atual e os métodos utilitários
 * relacionados.
 */
public enum PServerContext implements IServerContext {

    PETITION("/petition/*", "singular.petition"),
    WORKLIST("/worklist/*", "singular.worklist");

    private final String contextPath;
    private final String propertiesBaseKey;

    PServerContext(String defaultPath, String propertiesBaseKey) {
        this.propertiesBaseKey = propertiesBaseKey;
        String key = propertiesBaseKey + ".context";
        String path = ConfigProperties.get(key);
        if (path == null || path.length() <= 0) {
            path = defaultPath;
        }
        if (!path.endsWith("/*")) {
            if (path.endsWith("*")) {
                path = path.substring(0, path.length() - 2) + "/*";
            } else if (path.endsWith("/")) {
                path += "*";
            } else {
                path += "/*";
            }
        }
        this.contextPath = path;
    }


    @Override
    public String getPropertiesBaseKey() {
        return propertiesBaseKey;
    }

    @Override
    public String getName() {
        return this.name();
    }

    /**
     * O contexto no formato aceito por servlets e filtros
     *
     * @return
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para java regex
     *
     * @return
     */
    public String getPathRegex() {
        return getContextPath().replaceAll("\\*", ".*");
    }

    /**
     * Conversao do formato aceito por servlets e filtros (contextPath) para um formato de url
     * sem a / ao final.
     *
     * @return
     */
    public String getUrlPath() {
        String path = getContextPath().replace("*", "").replace(".", "").trim();
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }



}