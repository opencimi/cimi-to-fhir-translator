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
package org.opencimi.transform.translator.fhir;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.fail;


/**
 * Created by cnanjo on 10/14/16.
 */
public class FhirLogicalProfileGeneratorTest {

    private FhirLogicalProfileGenerator generator;

    @Before
    public void setUp() throws Exception {
        generator = new FhirLogicalProfileGenerator("http://opencimi.org/logical-model/fhir");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testLogicalProfileGeneration() {
        
        List<InputStream> sources = new ArrayList<>();
        sources.add(FhirLogicalProfileGeneratorTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_CORE.v.0.0.2.bmm"));
        sources.add(FhirLogicalProfileGeneratorTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_FOUNDATION.v.0.0.2.bmm"));
        sources.add(FhirLogicalProfileGeneratorTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_CLINICAL.v.0.0.2.bmm"));
        
        List<StructureDefinition> logicalProfiles = generator.generateLogicalProfile(sources);
        
        FhirContext ctx = FhirContext.forDstu3();
        IParser jsonParser = ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        FhirValidator validator = ctx.newValidator();
        
        for (StructureDefinition logicalProfile : logicalProfiles) {
            String structureDefinition = jsonParser.encodeResourceToString(logicalProfile);
            System.out.println(logicalProfile.getName()+"=\n" + structureDefinition);
            System.out.println("\n");
            
            ValidationResult validationResult = validator.validateWithResult(logicalProfile);
            if (!validationResult.isSuccessful()){
                System.out.println("The generated resource can't be validated:");
                for (SingleValidationMessage message : validationResult.getMessages()) {
                    System.out.println("\t["+message.getSeverity()+"] "+message.getMessage());
                }
                fail();
            }
        }
        
    }
}