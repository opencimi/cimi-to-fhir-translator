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
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.StructureDefinition;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import static org.junit.Assert.fail;
import org.junit.Ignore;

/**
 * Created by cnanjo on 10/14/16.
 */
public class FhirLogicalProfilePublicationTest {

    String endpoint = "https://stu3.simplifier.net/CIMIFHIRLogicalProfi";
    String username = "cnanjo@gmail.com";
        String password = "C1M1 Rul35!";

    private FhirContext ctx;
    private FhirLogicalProfileGenerator generator;
    private IGenericClient client;

    @Before
    public void setUp() throws Exception {
        ctx = FhirContext.forR4();
        BasicAuthInterceptor authInterceptor = new BasicAuthInterceptor(username, password);
        client = ctx.newRestfulGenericClient(endpoint);
        client.registerInterceptor(authInterceptor);
        generator = new FhirLogicalProfileGenerator("http://opencimi.org/logical-model/fhir");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    @Ignore("Do not run this test by default")
    public void testLogicalProfilePublication() {

        cleanupServer();
        
        List<InputStream> sources = new ArrayList<>();
        sources.add(FhirLogicalProfilePublicationTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_CORE.v.0.0.2.bmm"));
        sources.add(FhirLogicalProfilePublicationTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_FOUNDATION.v.0.0.2.bmm"));
        sources.add(FhirLogicalProfileGeneratorTest.class.getResourceAsStream("/bmm/ballot_may_2017/CIMI_RM_CLINICAL.v.0.0.2.bmm"));

        List<StructureDefinition> logicalProfiles = generator.generateLogicalProfile(sources);

        IParser jsonParser = ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        FhirValidator validator = ctx.newValidator();

        for (StructureDefinition logicalProfile : logicalProfiles) {
            String structureDefinition = jsonParser.encodeResourceToString(logicalProfile);
            System.out.println(logicalProfile.getName() + "=\n" + structureDefinition);
            System.out.println("\n");

            ValidationResult validationResult = validator.validateWithResult(logicalProfile);
            if (!validationResult.isSuccessful()) {
                System.out.println("The generated resource can't be validated:");
                for (SingleValidationMessage message : validationResult.getMessages()) {
                    System.out.println("\t[" + message.getSeverity() + "] " + message.getMessage());
                }
                fail();
            }
        }

        for (StructureDefinition logicalProfile : logicalProfiles) {
            MethodOutcome outcome = client.create().resource(logicalProfile).execute();
        }

    }

    private void cleanupServer() {
        IParser jsonParser = ctx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        
        Bundle bundle = client.search().forResource(StructureDefinition.class).count(500).returnBundle(Bundle.class).execute();
        System.out.println(""+bundle.getTotal());
        
        int i = 1;
        for (Bundle.BundleEntryComponent e : bundle.getEntry()) {
            if (e.getResource() instanceof StructureDefinition){
                IBaseOperationOutcome deleteOutcome = client.delete().resource(e.getResource()).execute();
            }
        }
            
    }
}
