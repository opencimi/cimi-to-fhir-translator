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
package org.opencimi.transform.utils.fhir;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hl7.fhir.r4.model.StructureDefinition;
import org.openehr.bmm.core.BmmClass;
import org.openehr.bmm.core.BmmContainerType;
import org.openehr.bmm.core.BmmGenericType;
import org.openehr.bmm.core.BmmOpenType;
import org.openehr.bmm.core.BmmType;

/**
 *
 * @author esteban
 */
public class CimiToFhirTypeIndex {
    
    private final Map<String, String> cimiToFhirMappings = new HashMap<>();
    private final Map<String, String> fhirToCimiMappings = new HashMap<>();
    private final Map<String, StructureDefinition> cimiToStructureDefinitionMappings = new LinkedHashMap<>();

    public CimiToFhirTypeIndex() {
        
        //Primitive Types
        addCoreTypeMapping("Any", "Element");
        addCoreTypeMapping("Boolean", "boolean");
        addCoreTypeMapping("Byte", "integer"); //TDB
        addCoreTypeMapping("Character", "integer");
        addCoreTypeMapping("Count", "integer");
        addCoreTypeMapping("DATE", "date");
        addCoreTypeMapping("DATE_TIME", "dateTime");
        addCoreTypeMapping("INSTANT", "instant");
        addCoreTypeMapping("Integer", "integer");
        addCoreTypeMapping("POSITIVE_INTEGER_COUNT", "positiveInt");
        addCoreTypeMapping("Real", "decimal");
        addCoreTypeMapping("String", "string");
        addCoreTypeMapping("TIME", "time");
        addCoreTypeMapping("UNSIGNED_INTEGER_COUNT", "integer");
        addCoreTypeMapping("URI", "uri");
        addCoreTypeMapping("URI_VALUE", "uri");
        
        //Generic types
        addCoreTypeMapping("INTERVAL_VALUE<DATE>", "Period");
        addCoreTypeMapping("INTERVAL_VALUE<DATE_TIME>", "Period");
        addCoreTypeMapping("INTERVAL_VALUE<QUANTITY>", "Range");
        
        //Complex Types
        addCoreTypeMapping("CODED_TEXT", "CodeableConcept");
    }
    
    /**
     * This method maps CIMI primitive and complex types to corresponding
     * FHIR primitive or complex types.
     * 
     * @param cimiType
     * @param fhirType 
     */
    public void addCoreTypeMapping(String cimiType, String fhirType){
        cimiToFhirMappings.put(cimiType, fhirType);
        fhirToCimiMappings.put(fhirType, cimiType);
    }
    
    public String getFhirType(String cimiType) {
        return cimiToFhirMappings.get(cimiType);
    }
    
    public String getFhirType(BmmType cimiType) {
        return cimiToFhirMappings.get(cimiType.getTypeName());
    }
    
    public String getCimiType(String fhirType) {
        return fhirToCimiMappings.get(fhirType);
    }
    
    /**
     * This method indexes CIMI Types to the corresponding 
     * {@link StructureDefinition FHIR Structure Definition}.
     * Use only for CIMI classes that have no equivalent in the FHIR primitive
     * and complex types.
     * 
     * @param cimiType
     * @param definition 
     */
    public void addStructureDefinitionIndex(String cimiType, StructureDefinition definition){
        cimiToStructureDefinitionMappings.put(cimiType, definition);
    }
    
    public StructureDefinition getStructureDefinition(String cimiType){
        return cimiToStructureDefinitionMappings.get(cimiType);
    }
    
    public boolean isExcludedType(BmmType cimiType){
        return false;
    }
    
    public boolean isExcludedClass(BmmClass cimiClass){
        return Arrays.asList(new String[]{
            "any",
            "array",
            "list",
            "byte",
            "integer",
            "unsigned_integer_count",
            "character",
            "boolean",
            "date",
            "positive_integer_count",
            "instant",
            "real",
            "string",
            "count",
            "date_time",
            "time",
            "uri_value",
            "uri"
        }).contains(cimiClass.getName().toLowerCase());
    }
    
}
