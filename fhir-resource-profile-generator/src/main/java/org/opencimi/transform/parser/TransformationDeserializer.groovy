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
package org.opencimi.transform.parser

import groovy.xml.QName
import org.opencimi.transform.ModelAttribute
import org.opencimi.transform.ModelTransform
import org.opencimi.transform.OperationParameter
import org.opencimi.transform.Rule
import org.opencimi.transform.RuleGroup
import org.opencimi.transform.RuleSource
import org.opencimi.transform.RuleTarget
import org.opencimi.transform.TransformInput
import org.opencimi.transform.TypeConversion

class TransformationDeserializer {


    def loadFromFilePath = {filePath -> new XmlParser().parse(new File(filePath))}
    def loadFromStream = {filePath -> new XmlParser().parse((InputStream)getClass().getResourceAsStream(filePath))}

    def loadTransform(def loadClosure, def loadArguments) {
        def transformXml = loadClosure.call(loadArguments)
        return processTransformFile(transformXml)
    }

    public List<ModelTransform> loadFromClassPath(String path) {
        return loadTransform(loadFromStream,path)
    }

    public List<ModelTransform> loadFromFilePath(String path) {
        return loadTransform(loadFromFilePath,path)
    }

    def processTransformFile(def transformXml) {
        def transforms = new ArrayList<ModelTransform>();
        transformXml.transformation.each { t ->
            def transform = new ModelTransform(t.@'name')
            transforms.add(transform)
            t.source.each { s ->
                def source = new TransformInput()
                source.model = s.@'model'
                source.className = s.@'class'
                source.contraintIdentifier = s.@'constraint'
                transform.source = source
            }
            t.target.each { s ->
                def target = new TransformInput()
                target.model = s.@'model'
                target.className = s.@'class'
                target.contraintIdentifier = s.@'constraint'
                transform.target = target
            }
            t.ruleGroup.each { group ->
                def ruleGroup = new RuleGroup()
                transform.ruleGroupList.add ruleGroup
                group.rule.each { ruleNode ->
                    def rule = new Rule()
                    ruleGroup.addRule rule
                    ruleNode.source.each { sourceNode ->
                        RuleSource ruleSource = new RuleSource()
                        rule.addSource ruleSource
                        sourceNode.attribute.each { attributeNode ->
                            ruleSource.addAttribute new ModelAttribute(attributeNode.@'name')
                        }
                    }
                    ruleNode.target.each { targetNode ->
                        RuleTarget ruleTarget = new RuleTarget()
                        rule.addTarget ruleTarget
                        targetNode.attribute.each { attributeNode ->
                            ruleTarget.addAttribute new ModelAttribute(attributeNode.@'name')
                        }
                        targetNode.typeConversion.each { typeConversionNode ->
                            def typeConversion = new TypeConversion();
                            ruleTarget.setTypeConversion typeConversion
                            def type = typeConversionNode.@'type'
                            if(type instanceof QName) {
                                typeConversion.setNamespace type.prefix
                                typeConversion.setType type.localPart
                            } else {
                                if(type.contains(":")) {
                                    def qname = type.split(":")
                                    typeConversion.setNamespace qname[0]
                                    typeConversion.setType qname[1]
                                } else {
                                    typeConversion.setType type;
                                }
                            }
                            typeConversionNode.arguments.argument.each { argumentNode ->
                                def param = new OperationParameter()
                                typeConversion.addOperationParameter param
                                param.setName argumentNode.@'name'
                                param.setValue argumentNode.text()
                            }
                        }
                    }
                }
            }
        }
        return transforms;
    }
}
