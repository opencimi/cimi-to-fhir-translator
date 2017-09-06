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
package org.opencimi.transform.tools;

import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private List<String> bmmFiles = new ArrayList<>();
    private String outputDirectory;
    private List<ClinicalStatementConfiguration> clinicalStatementConfigurations = new ArrayList<>();

    public List<String> getBmmFiles() {
        return bmmFiles;
    }

    public void setBmmFiles(List<String> bmmFiles) {
        this.bmmFiles = bmmFiles;
    }

    public void addBmmFile(String bmmFile) {
        this.bmmFiles.add(bmmFile);
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public List<ClinicalStatementConfiguration> getClinicalStatementConfigurations() {
        return clinicalStatementConfigurations;
    }

    public void setClinicalStatementConfigurations(List<ClinicalStatementConfiguration> clinicalStatementConfigurations) {
        this.clinicalStatementConfigurations = clinicalStatementConfigurations;
    }

    public void addClinicalStatementConfiguration(ClinicalStatementConfiguration clinicalStatementConfiguration) {
        this.clinicalStatementConfigurations.add(clinicalStatementConfiguration);
    }

    public void addClinicalStatementConfiguration(String name, String topic, String context) {
        this.clinicalStatementConfigurations.add(new ClinicalStatementConfiguration(name, topic, context));
    }
}
