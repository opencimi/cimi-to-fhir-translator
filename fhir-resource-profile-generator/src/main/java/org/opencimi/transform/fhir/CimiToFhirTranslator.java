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

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.utils.transform.FhirTransformationEngine;
import org.hl7.fhir.r4.utils.transform.serializer.StructureMapSerializer;
import org.opencimi.transform.ModelTransform;
import org.opencimi.transform.parser.TransformationDeserializer;
import org.opencimi.transform.tools.CimiTransformHelper;
import org.opencimi.transform.translator.fhir.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Load all BMM modules (DONE see CIMI Helper)
 * Flatten specified clinical statements (DONE see CIMI Helper)
 * Generate and cache logical profiles (DONE)
 * Load transformation definitions (DONE)
 * Generate and cache maps
 * Run transformations
 * Persist resource profiles
 */

public class CimiToFhirTranslator {

    private CimiTransformHelper helper;
    private FhirLogicalProfileGenerator logicalProfileGenerator;
    private List<StructureDefinition> logicalProfiles = new ArrayList<>();
    private List<ModelTransform> transformations;
    private Map<String, StructureMap> resourceProfileMaps = new HashMap<>();
    private List<StructureDefinition> resourceProfiles = new ArrayList<>();

    public CimiToFhirTranslator(CimiTransformHelper helper, FhirLogicalProfileGenerator logicalProfileGenerator) {
        this.helper = helper;
        this.logicalProfileGenerator = logicalProfileGenerator;
    }

    public void initialize() {
        helper.initialize();
        this.logicalProfiles = logicalProfileGenerator.generateLogicalProfile(helper.getBmmModel());
        TransformationDeserializer deserializer = new TransformationDeserializer();
        this.transformations = deserializer.loadFromClassPath("/mapping/maps/CimiTransformations.xml");//TODO pass through configuration file
    }

    public void generateFhirResourceProfiles() {
        StructureMapSerializer serializer = new StructureMapSerializer();
        PrePopulatedValidationSupport validationSupport = new PrePopulatedValidationSupport();
        transformations.forEach( transform -> {
            StructureMap map = StructureMapFactory.build(transform);
            resourceProfileMaps.put(map.getUrl(), map);
            System.out.println(serializer.render(map));
            logicalProfiles.forEach(logicalProfile -> {
                validationSupport.addStructureDefinition(logicalProfile);
            });
            FhirTransformationEngine transformationEngine = configureTransformationEngine(resourceProfileMaps, validationSupport);
            try {
                List<StructureDefinition> result = transformationEngine.analyse(null, map).getProfiles();
                System.out.println(result);
            } catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static FhirTransformationEngine configureTransformationEngine(Map<String, StructureMap> maps, PrePopulatedValidationSupport validationSupport) {
        FhirContext context = FhirContext.forR4();
        DefaultProfileValidationSupport validation = new DefaultProfileValidationSupport();
        for (StructureDefinition sd : new DefaultProfileValidationSupport().fetchAllStructureDefinitions(context)) {
            validationSupport.addStructureDefinition(sd);
        }
        HapiWorkerContext hapiContext = new HapiWorkerContext(context, validationSupport);
        return new FhirTransformationEngine(hapiContext, maps, null, null);
    }
}
