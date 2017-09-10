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
 * Author: Esteban Aliverti
 */
package org.opencimi.transform.translator.fhir;

import java.io.InputStream;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.opencimi.transform.utils.fhir.CimiToFhirTypeIndex;
import org.openehr.bmm.core.*;
import org.openehr.bmm.persistence.PersistedBmmSchema;
import org.openehr.bmm.persistence.deserializer.BmmSchemaDeserializer;
import org.openehr.odin.CompositeOdinObject;
import org.openehr.odin.antlr.OdinVisitorImpl;
import org.openehr.odin.loader.OdinLoaderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FhirLogicalProfileGenerator {
    
    private Logger LOG = LoggerFactory.getLogger(FhirLogicalProfileGenerator.class);
    
    private static final String ANY_CLASS = "Any";
    private static final String GENERIC_PARAM_NAME = "T";
    
    private final String baseUrl;
    private CimiToFhirTypeIndex cimiToFhirTypeIndex;
    
    private class TypeConversionResult{
        String bmmType;
        String fhirType;
    }

    public FhirLogicalProfileGenerator(String baseUrl) {
        this.baseUrl = baseUrl;
        cimiToFhirTypeIndex = new CimiToFhirTypeIndex();
    }

    public List<StructureDefinition> generateLogicalProfile(List<InputStream> bmmSchemas) {
        PersistedBmmSchema bmm = deserializeSchemas(bmmSchemas);
        bmm.createBmmSchema();
        return generateLogicalProfile(bmm.getBmmModel());
    }

    public List<StructureDefinition> generateLogicalProfile(BmmModel bmmModel) {
        return bmmModel.getClassDefinitions().values().stream()
            .filter(cd -> !cimiToFhirTypeIndex.isExcludedClass(cd))
            .map(cd -> toStructureDefinition(bmmModel, cd))
            .collect(toList());
    }

    public PersistedBmmSchema deserializeSchemas(List<InputStream> bmmSchemas) {

        PersistedBmmSchema result = new PersistedBmmSchema();

        for (InputStream bmmSchema : bmmSchemas) {
            OdinLoaderImpl loader = new OdinLoaderImpl();
            OdinVisitorImpl visitor = loader.loadOdinFile(bmmSchema);
            CompositeOdinObject root = visitor.getAstRootNode();
            BmmSchemaDeserializer deserializer = new BmmSchemaDeserializer();
            result.merge(deserializer.deserialize(root));
        }

        return result;
    }

    public StructureDefinition toStructureDefinition(BmmModel bmmModel, BmmClass classDefinition) {
        LOG.debug("Creating StructureDefinition for "+classDefinition.getName());
        StructureDefinition logicalProfile = new StructureDefinition();
        logicalProfile.setKind(StructureDefinition.StructureDefinitionKind.LOGICAL);

        logicalProfile.setTitle(classDefinition.getName());
        logicalProfile.setName(classDefinition.getName());
        logicalProfile.setDescription(classDefinition.getDocumentation());
        logicalProfile.setType(classDefinition.getName());
        logicalProfile.setUrl(baseUrl+"/"+logicalProfile.getName());
        logicalProfile.setStatus(Enumerations.PublicationStatus.DRAFT);
        logicalProfile.setAbstract(classDefinition.isAbstract());

        cimiToFhirTypeIndex.addStructureDefinitionIndex(classDefinition.getName(), logicalProfile);
        
        //Base Definition
        if (classDefinition.getAncestors() != null && !classDefinition.getAncestors().isEmpty()){
            logicalProfile.setBaseDefinition(baseUrl+"/"+classDefinition.getAncestors().values().iterator().next().getName());
        }
        
        //Root element
        logicalProfile.getSnapshot().addElement(createRootElementDefinition(bmmModel, logicalProfile.getName()));
        
        //elements
        Map<String, BmmProperty> properties = classDefinition.getProperties();
        for (BmmProperty property : properties.values()) {
            if (cimiToFhirTypeIndex.isExcludedType(property.getType())){
                LOG.debug("Skipping "+property.getName()+" because its type ("+property.getType().getTypeName()+") is black listed.");
                continue;
            }
            ElementDefinition elementDefinition = toElementDefinition(bmmModel, logicalProfile.getName(), property);
            logicalProfile.getSnapshot().addElement(elementDefinition);
        }
        

        return logicalProfile;
    }

    private ElementDefinition createRootElementDefinition(BmmModel bmmModel, String parentName) {
        ElementDefinition element = new ElementDefinition();
        element.setLabel(parentName);
        element.setPath(parentName);
        element.setDefinition("");
        
        return element;
    }
    
    private ElementDefinition toElementDefinition(BmmModel bmmModel, String parentName, BmmProperty property) {
        ElementDefinition element = new ElementDefinition();
        element.setLabel(property.getName());
        element.setPath(parentName + "." + property.getName());
        element.setDefinition(property.getDocumentation());
        
        //Cardinality
        int lowerBound = property.getMandatory() ? 1 : 0;
        String upperBound = "1";
        
        TypeConversionResult typeConversionResult = calculateBmmConcreteType(bmmModel, property.getType());
        
        if (property instanceof BmmContainerProperty){
            lowerBound = ((BmmContainerProperty)property).getCardinality().getLower();
            if (((BmmContainerProperty)property).getCardinality().isOpen()){
                upperBound = "*";
            } else {
                upperBound = ((BmmContainerProperty)property).getCardinality().getUpper()+"";
            }
        }
        
        
        //Type
        String fhirType = typeConversionResult.fhirType;
        if (fhirType == null){
            LOG.debug(typeConversionResult.bmmType+" doesn't seem to be a primitive nor complex type.");
            StructureDefinition structureDefinition = cimiToFhirTypeIndex.getStructureDefinition(typeConversionResult.bmmType);
            if (structureDefinition != null){
                fhirType = structureDefinition.getUrl();
            } else {
                LOG.debug(typeConversionResult.bmmType+" has no associated StructureDefinition. About to generate it.");
                fhirType = toStructureDefinition(bmmModel, bmmModel.getClassDefinition(typeConversionResult.bmmType)).getName();
            }
        }

        if (fhirType != null){
            element.addType()
                .setCode(fhirType);
        } 
        
        //Cardinality
        element.setMin(lowerBound);
        element.setMax(upperBound);
        
        return element;
    }
    
    private TypeConversionResult calculateBmmConcreteType(BmmModel model, BmmType type){
        
        BmmClass clazz = type.getBaseClass();
        
        boolean isContainer = type instanceof BmmContainerType;
        boolean isOpenType = type instanceof BmmOpenType;
        boolean isGenericType = type instanceof BmmGenericType;
        
        if (isContainer){
            return calculateBmmConcreteType(model, ((BmmContainerType)type).getBaseType());
        } else if (isOpenType){
            BmmOpenType openType = (BmmOpenType)type;
            if (openType.getGenericConstraint() == null){
                clazz = model.getClassDefinition(ANY_CLASS);
            } else {
                if (GENERIC_PARAM_NAME.equals(openType.getGenericConstraint().getName())){
                    if (openType.getGenericConstraint().getConformsToType() == null){
                        clazz = model.getClassDefinition(ANY_CLASS);
                    } else {
                        clazz = openType.getGenericConstraint().getConformsToType();
                    }
                } else {
                    clazz = openType.getGenericConstraint().getConformsToType();
                }
            }
//        } else if (isGenericType){
//            BmmGenericType genericType = (BmmGenericType) type;
//            clazz = genericType.getBaseClass();
//            genericType.getGenericParameters()
//            
//            1- Get the baseClass
//            2- If the baseClass is INTERVAL_VALUE
//                If genericParameter is DATE or DATE_TIME -> type = Period
//                If genericParameter is QUANTITY -> type = Range
//                else -> exception
//            3- If the baseClass is RATIO        
        }
        
        if (clazz == null){
            throw new IllegalStateException("Couldn't determine the concrete type of "+type);
        }

        TypeConversionResult result = new TypeConversionResult();
        result.bmmType = calculateBmmConcreteType(model, clazz);
        result.fhirType = cimiToFhirTypeIndex.getFhirType(type);
        
         
        return result;
    }
    
    private String calculateBmmConcreteType(BmmModel model, BmmClass clazz){
        
        if (clazz instanceof BmmGenericClass){
            return clazz.getName();
        } else {
            return clazz.getTypeName();
        }
    }
    
}
