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
package org.opencimi.transform.fhir;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.StructureMap;
import org.opencimi.transform.ModelTransform;

public class StructureMapFactory {

    public static final String STRUCTURE_MAP_URI_BASE = "http://hl7.org/cimi/fhir/transformation/";

    public static StructureMap build(ModelTransform transform) {
        StructureMap map = new StructureMap();
        map.setName(transform.getName());
        map.setUrl(STRUCTURE_MAP_URI_BASE + map.getName().toLowerCase());
        map.addStructure().setUrl(transform.getSource().getContraintIdentifier())
                .setAlias(transform.getSource().getClassName())
                .setMode(StructureMap.StructureMapModelMode.SOURCE)
                .setDocumentation("Source model");
        map.addStructure().setUrl(transform.getTarget().getContraintIdentifier())
                .setAlias(transform.getTarget().getClassName())
                .setMode(StructureMap.StructureMapModelMode.TARGET)
                .setDocumentation("Target model");
        transform.getRuleGroupList().forEach( ruleGroup -> {
           StructureMap.StructureMapGroupComponent group = map.addGroup().setName("mainGroup");
           group.addInput().setName("source").setType(transform.getSource().getClassName()).setMode(StructureMap.StructureMapInputMode.SOURCE);
           group.addInput().setName("target").setType(transform.getTarget().getClassName()).setMode(StructureMap.StructureMapInputMode.TARGET);
           group.setTypeMode(StructureMap.StructureMapGroupTypeMode.TYPEANDTYPES);
           ruleGroup.getRules().forEach( rule -> {
               StructureMap.StructureMapGroupRuleComponent transformRule = group.addRule();
               transformRule.setName(rule.getSources().get(0).getAttributeList().get(0).getName() + "Rule");
               StructureMap.StructureMapGroupRuleSourceComponent source = transformRule.addSource().setContext("source").setElement(rule.getSources().get(0).getAttributeList().get(0).getName()).setVariable("a");
               StructureMap.StructureMapGroupRuleTargetComponent target = transformRule.addTarget().setContext("target").setElement(rule.getTargets().get(0).getAttributeList().get(0).getName()).setVariable("a");
               if(rule.getTargets().get(0).getTypeConversion() != null) { //.setTransform(StructureMap.StructureMapTransform.EXTENSION);
                   String type = rule.getTargets().get(0).getTypeConversion().getType();
                   if(type.equals("copy")) {
                       target.setTransform(StructureMap.StructureMapTransform.COPY);
                       target.addParameter().setValue(new IdType("a"));
                   }
               }

           });
        });
        return map;
    }

}