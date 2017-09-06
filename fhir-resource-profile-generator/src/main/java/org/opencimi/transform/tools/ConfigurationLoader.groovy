/*
 * #%L
 * OpenCIMI - OpenCIMI CIMI-to-FHIR Translation Utilities
 * %%
 * Copyright (C) 2016 - 2017 Cognitive Medical Systems
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 * Author: Claude Nanjo
 */
package org.opencimi.transform.tools

import java.nio.file.Files
import java.nio.file.Paths

class ConfigurationLoader {
    public static Configuration load(String configFileDir) {
        Configuration config = new Configuration()
        String configXml = null;
        if(configFileDir != null) {
            configXml = new String(Files.readAllBytes(Paths.get(configFileDir)));
        } else {
            configXml = this.getClass().getResource( '/config.xml' ).text
        }

        println 'Using config directory: ' + configFileDir

        try {
            def configuration  = new XmlSlurper().parseText(configXml);
            configuration.bmmFiles.bmmFile.each { file ->
                String path = file.@'name'
                config.addBmmFile(path)}
            configuration.clinicalStatements.clinicalStatement.each { stmt ->
                String name = stmt.@'name'
                String topic = stmt.@'topic'
                String context = stmt.@'context'
                config.addClinicalStatementConfiguration(name, topic, context);

            }
            configuration.outputDirectory.each { dir ->
                String path = dir.@'name'
                config.setOutputDirectory(path)}
        } catch(Exception e) {
            throw new RuntimeException("Error parsing configuration file", e)
        }
        return config;
    }
}
