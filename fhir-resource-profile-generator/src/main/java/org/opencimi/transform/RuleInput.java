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

public class RuleInput {

    private List<ModelAttribute> attributeList = new ArrayList<>();

    public List<ModelAttribute> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(List<ModelAttribute> attributeList) {
        this.attributeList = attributeList;
    }

    public void addAttribute(ModelAttribute attribute) {
        this.attributeList.add(attribute);
    }
}
