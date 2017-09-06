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
package org.opencimi.transform.parser;

import org.junit.Before;
import org.junit.Test;
import org.opencimi.transform.ModelTransform;
import org.opencimi.transform.Rule;
import org.opencimi.transform.RuleGroup;
import org.opencimi.transform.TypeConversion;

import java.util.List;

import static org.junit.Assert.*;

public class TransformationReaderTest {

    private TransformationDeserializer transformationReader;

    @Before
    public void setup() {
        transformationReader = new TransformationDeserializer();
    }
    @Test
    public void processTransformFile() throws Exception {
        try {
            List<ModelTransform> transforms = transformationReader.loadFromClassPath("/mapping/maps/CimiTransformations.xml");
            assertNotNull(transforms);
            assertEquals(1, transforms.size());

            ModelTransform transform = transforms.get(0);
            assertEquals("MedicationOrderMap", transform.getName());

            assertNotNull(transform.getSource());
            assertEquals("CIMI", transform.getSource().getModel());
            assertEquals("MedicationOrderStatement", transform.getSource().getClassName());
            assertEquals("http://adl.org/medorderadl", transform.getSource().getContraintIdentifier());

            assertNotNull(transform.getTarget());
            assertEquals("FHIR", transform.getTarget().getModel());
            assertEquals("MedicationRequest", transform.getTarget().getClassName());
            assertEquals("http://SomeFhirProfileURL", transform.getTarget().getContraintIdentifier());

            assertNotNull(transform.getRuleGroupList());
            assertEquals(1, transform.getRuleGroupList().size());

            RuleGroup ruleGroup = transform.getRuleGroupList().get(0);

            assertNotNull(ruleGroup.getRules());
            assertEquals(4, ruleGroup.getRules().size());

            Rule rule1 = ruleGroup.getRules().get(0);
            assertEquals(1, rule1.getSources().size());
            assertEquals("identifier", rule1.getSources().get(0).getAttributeList().get(0).getName());
            assertEquals(1, rule1.getTargets().size());
            assertEquals("identifier", rule1.getTargets().get(0).getAttributeList().get(0).getName());

            Rule rule3 = ruleGroup.getRules().get(2);
            assertEquals(1, rule3.getSources().size());
            assertEquals("encounter", rule3.getSources().get(0).getAttributeList().get(0).getName());
            assertEquals(1, rule3.getTargets().size());
            assertEquals("context", rule3.getTargets().get(0).getAttributeList().get(0).getName());

            TypeConversion typeConversion = rule3.getTargets().get(0).getTypeConversion();
            assertNotNull(typeConversion);
            //assertEquals("fhir", typeConversion.getNamespace());
            assertEquals("reference", typeConversion.getType());
            assertEquals("referent", typeConversion.getOperationParameterList().get(0).getName());
            assertEquals("Encounter", typeConversion.getOperationParameterList().get(0).getValue());
            assertEquals("constraintIdentifier", typeConversion.getOperationParameterList().get(1).getName());
            assertEquals("http://opencimi.org/fhir/extension/encounter", typeConversion.getOperationParameterList().get(1).getValue());


            assertEquals(1, ruleGroup.getRules().get(0).getTargets().size());
        } catch(Exception e) {
            e.printStackTrace();
            fail("Error loading transforms");
        }

    }

}
