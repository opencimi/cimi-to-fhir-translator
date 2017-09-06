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

public class Rule {
    private List<RuleSource> sources = new ArrayList<>();
    private List<RuleTarget> targets = new ArrayList<>();

    public Rule() {
    }

    public List<RuleSource> getSources() {
        return sources;
    }

    public void setSources(List<RuleSource> sources) {
        this.sources = sources;
    }

    public void addSource(RuleSource source) {
        this.sources.add(source);
    }

    public List<RuleTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<RuleTarget> targets) {
        this.targets = targets;
    }

    public void addTarget(RuleTarget target) {
        this.targets.add(target);
    }
}
