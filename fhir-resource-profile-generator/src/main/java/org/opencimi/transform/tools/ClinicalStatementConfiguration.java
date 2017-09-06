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

public class ClinicalStatementConfiguration {
    private String statementName;
    private String statementTopicName;
    private String statementContextName;

    public ClinicalStatementConfiguration(String statementName, String statementTopicName, String statementContextName) {
        this.statementName = statementName;
        this.statementTopicName = statementTopicName;
        this.statementContextName = statementContextName;
    }

    public String getStatementName() {
        return statementName;
    }

    public void setStatementName(String statementName) {
        this.statementName = statementName;
    }

    public String getStatementTopicName() {
        return statementTopicName;
    }

    public void setStatementTopicName(String statementTopicName) {
        this.statementTopicName = statementTopicName;
    }

    public String getStatementContextName() {
        return statementContextName;
    }

    public void setStatementContextName(String statementContextName) {
        this.statementContextName = statementContextName;
    }
}
