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

package org.opensingular.app.commons.spring.persistence.database;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.opensingular.lib.commons.base.SingularException;
import org.opensingular.lib.commons.scan.SingularClassPathScanner;
import org.opensingular.lib.commons.util.Loggable;

import javax.persistence.Entity;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SingularSchemaExport implements Loggable {


    private SingularSchemaExport() {

    }

    /**
     * Método com objeto de gerar o script de toda a base do singular, inclusive com os inserts.
     *
     * @param packages          O pacote na qual deverá ser gerado o script.
     * @param dialect           O dialect do banco escolhido.
     * @param directoryFileName O diretorio na qual será gerado o script.
     * @param scriptsPath       O path dos scripts adicionais.
     * @return Return all the scripts DML and DDL that will be executed by Hibernate.
     */
    public static void generateScriptToFile(String[] packages, Class<? extends Dialect> dialect, List<String> scriptsPath, String directoryFileName) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(directoryFileName), StandardCharsets.UTF_8))) {//NOSONAR
            writer.write(generateScript(packages, dialect, scriptsPath).toString());
        } catch (Exception e) {
            throw new ExportScriptGenerationException(e.getMessage(), e);
        }
    }

    /**
     * Método com objeto de gerar o script de toda a base do singular, inclusive com os inserts.
     *
     * @param packages    O pacote na qual deverá ser gerado o script.
     * @param dialect     O dialect do banco escolhido.
     * @param scriptsPath O path dos scripts adicionais.
     * @return Return all the scripts DML and DDL that will be executed by Hibernate.
     */
    public static StringBuilder generateScript(String[] packages, Class<? extends Dialect> dialect, List<String> scriptsPath) {

        StringBuilder scriptsText = readScriptsContent(scriptsPath);

        try {
            Set<Class<?>> typesAnnotatedWith = SingularClassPathScanner.get().findClassesAnnotatedWith(Entity.class);
            List<Class<?>> list = typesAnnotatedWith
                    .stream()
                    .filter(t -> filterPackages(t, packages))
                    .collect(Collectors.toList());

            //creates a minimal configuration in a MetadataSources
            MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", dialect != null ? dialect.getName() : H2Dialect.class.getName())
                    .applySetting("hibernate.id.new_generator_mappings", "false")
                    .build());

            for (Class<?> c : list) {
                metadata.addAnnotatedClass(c);
            }

            //exports the creation scripts in a file then reads from it
            SchemaExport schemaExport = new SchemaExport();
            schemaExport.setOutputFile("db/ddl/scripts.sql");
            schemaExport.createOnly(EnumSet.of(TargetType.SCRIPT), metadata.buildMetadata());

            String[] scriptsEntities = readFile("db/ddl/scripts.sql").split("\n");

            return formatterScript(scriptsEntities, scriptsText);
        } catch (Exception e) {
            throw new ExportScriptGenerationException(e.getMessage(), e);
        }
    }

    private static boolean filterPackages(Class<?> t, String[] packages) {
        for (String somePackage : packages) {
            if (t.getPackage().getName().startsWith(somePackage)) {
                return true;
            }
        }
        return false;
    }

    private static StringBuilder readScriptsContent(List<String> scriptsPath) {
        try {
            StringBuilder scriptsText = new StringBuilder();
            if (CollectionUtils.isNotEmpty(scriptsPath)) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                for (String script : scriptsPath) {
                    script = removeStartingSlash(script);
                    configureScriptTextByScriptPath(scriptsText, classLoader, script);
                }
            }
            return scriptsText;
        } catch (IOException e) {
            throw SingularException.rethrow(e);
        }
    }

    private static void configureScriptTextByScriptPath(StringBuilder scriptsText, ClassLoader classLoader,
            String scriptPath) throws IOException {
        InputStream stream = classLoader.getResourceAsStream(scriptPath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                scriptsText.append(line).append('\n');
                line = reader.readLine();
            }
        }
    }

    private static String removeStartingSlash(String script) {
        if (script.startsWith("/")) {
            return script.replaceFirst("/", "");
        }
        return script;
    }

    /**
     * Method reponsible to formmat the script, should skip line, and put the DDL and DML scripts.
     *
     * @param scriptsEntities   the script of the entities generated by hibernate.
     * @param scriptsAdicionais some extra scripts, normally DML scripts.
     * @return A StringBuilder containg all DML and DDL scripts.
     */
    private static StringBuilder formatterScript(String[] scriptsEntities, StringBuilder scriptsAdicionais) {
        Formatter formatter = FormatStyle.DDL.getFormatter();

        StringBuilder stringSql = new StringBuilder();

        for (String string : scriptsEntities) {
            String lineFormated = formatter.format(string) + "; \n";
            stringSql.append(lineFormated);
        }

        if (scriptsAdicionais != null) {
            stringSql.append(scriptsAdicionais);
        }

        return stringSql;
    }


    private static String readFile(String path) throws IOException {
        Path filePath = Paths.get(path);
        byte[] encoded = Files.readAllBytes(filePath);
        String s = new String(encoded, Charset.forName("UTF-8"));
        Files.delete(filePath);
        return s;
    }
}
