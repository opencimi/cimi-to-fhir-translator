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

import java.util.ArrayList;
import java.util.List;

public class TransformInput {
    private String model;
    private String className;
    private String contraintIdentifier;

    public TransformInput() {
    }

    public TransformInput(String model, String className, String contraintIdentifier) {
        this.model = model;
        this.className = className;
        this.contraintIdentifier = contraintIdentifier;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getContraintIdentifier() {
        return contraintIdentifier;
    }

    public void setContraintIdentifier(String contraintIdentifier) {
        this.contraintIdentifier = contraintIdentifier;
    }
}
