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

public class ModelTransform {

    private String name;
    private TransformInput source;
    private TransformInput target;
    private List<RuleGroup> ruleGroupList = new ArrayList<>();

    public ModelTransform(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public TransformInput getSource() {
        return source;
    }

    public void setSource(TransformInput source) {
        this.source = source;
    }

    public TransformInput getTarget() {
        return target;
    }

    public void setTarget(TransformInput target) {
        this.target = target;
    }

    public List<RuleGroup> getRuleGroupList() {
        return ruleGroupList;
    }

    public void setRuleGroupList(List<RuleGroup> ruleGroupList) {
        this.ruleGroupList = ruleGroupList;
    }

    public void addRuleGroup(RuleGroup ruleGroup) {
        this.ruleGroupList.add(ruleGroup);
    }
}
