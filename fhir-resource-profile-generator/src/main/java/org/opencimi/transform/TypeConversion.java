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
package org.opencimi.transform;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TypeConversion {
    private String namespace;
    private String type;
    private List<OperationParameter> operationParameterList = new ArrayList<>();

    public TypeConversion() {
    }

    public TypeConversion(String type) {
        this.type = type;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<OperationParameter> getOperationParameterList() {
        return operationParameterList;
    }

    public void setOperationParameterList(List<OperationParameter> operationParameterList) {
        this.operationParameterList = operationParameterList;
    }

    public void addOperationParameter(OperationParameter parameter) {
        this.operationParameterList.add(parameter);
    }
}
