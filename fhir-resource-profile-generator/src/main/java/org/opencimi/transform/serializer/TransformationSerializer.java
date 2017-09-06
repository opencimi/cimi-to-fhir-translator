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
package org.opencimi.transform.serializer;

import org.opencimi.transform.ModelTransform;
import org.opencimi.transform.TransformInput;

import java.util.List;

public class TransformationSerializer {

    public String serialize(List<ModelTransform> transforms) {
        StringBuilder builder = new StringBuilder();
        builder.append("<transformations>");
        transforms.forEach(transform -> {
            builder.append(serialize(transform));
        });
        builder.append("</transformations>");
        return builder.toString();
    }

    public String serialize(ModelTransform transform) {
        StringBuilder builder = new StringBuilder();
        builder.append("<transformation>");
        TransformInput source = transform.getSource();
        builder.append("<source model=\"").append(source.getModel()).append("\" class=\"").append(source.getClassName()).append("\" constraint=\"").append(source.getContraintIdentifier()).append("\"/>");
        TransformInput target = transform.getTarget();
        builder.append("<target model=\"").append(target.getModel()).append("\" class=\"").append(target.getClassName()).append("\" constraint=\"").append(target.getContraintIdentifier()).append("\"/>");
        transform.getRuleGroupList().forEach(ruleGroup -> {
            builder.append("<ruleGroup>");
            ruleGroup.getRules().forEach(rule -> {
                builder.append("<rule>");
                rule.getSources().forEach(ruleSource -> {
                    builder.append("<source>");
                    ruleSource.getAttributeList().forEach(modelAttribute -> {
                        builder.append("<attribute name=\"").append(modelAttribute.getName()).append("\"/>");
                    });
                    builder.append("</source>");
                });
                rule.getTargets().forEach(ruleTarget -> {
                    builder.append("<target>");
                    ruleTarget.getAttributeList().forEach(modelAttribute -> {
                        builder.append("<attribute name=\"").append(modelAttribute.getName()).append("\"/>");
                    });
                    if (ruleTarget.getTypeConversion() != null) {
                        builder.append("<typeConversion type=\"").append(ruleTarget.getTypeConversion().getType()).append("\">");
                        ruleTarget.getTypeConversion().getOperationParameterList().forEach(operationParameter -> {
                            builder.append("<argument name=\"").append(operationParameter.getName()).append("\">").append(operationParameter.getValue()).append("</argument>");
                        });
                        builder.append("</typeConversion>");
                    }
                    builder.append("</target>");
                });
                builder.append("</rule>");
            });
            builder.append("</ruleGroup>");
        });
        builder.append("</transformation>");
        return builder.toString();
    }
}
