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

import java.util.*;

/**
 * Utility class for assessing whether a given FHIR type subsumes or is subsumed by another FHIR type.
 *
 * Created by cnanjo on 10/25/16.
 * TODO Handle type using FHIR Type enumeration or better yet, HAPI FHIR
 */
public class FhirDstu3TypeUtility {

    private static final Map<String,List<String>> subsumptionTable;

    static {
        subsumptionTable = new HashMap<>();
        initializeSubsumptionTable();
    }

    /**
     * Method returns true if the descendant argument is subsumed by the ancestor argument.
     *
     * @param descendant The subsumee
     * @param ancestor The subsumer
     * @return
     * @throws RuntimeException Error thrown if descendant or ancestor are not valid FHIR types
     */
    public static boolean isSubsumedBy(String descendant, String ancestor) throws RuntimeException {
        boolean isSubsumed = false;
        List<String> ancestors = subsumptionTable.get(descendant);
        if(ancestors == null) {
            isSubsumed = false;
        } else {
            //TODO Need to add type check for ancestor type
            if(ancestors.contains(ancestor)) {
                isSubsumed = true;
            } else {
                isSubsumed = false;
            }
        }
        return isSubsumed;
    }

    /**
     * Method returns true if ancestor subsumes descendant
     *
     * @param ancestor The subsumer
     * @param descendant The subsumee
     * @return
     * @throws RuntimeException  Error thrown if descendant or ancestor are not valid FHIR types
     */
    public static boolean subsumes(String ancestor, String descendant) throws RuntimeException {
        return isSubsumedBy(descendant, ancestor);
    }

    /**
     * Validates whether the FHIR type argument is a valid FHIR type
     *
     * @param fhirType
     * @return
     */
    private boolean validateFhirType(String fhirType) {
        return true;//TODO Add code for FHIR type validation
    }

    /**
     * Method builds the subsumption table.
     *
     */
    private static void initializeSubsumptionTable() {
        subsumptionTable.put("code", getAsList("string", "element"));
        subsumptionTable.put("string", getAsList("element"));
        subsumptionTable.put("markdown",getAsList("string", "element"));
        subsumptionTable.put("id", getAsList("string", "element"));
        subsumptionTable.put("oid", getAsList("uri", "element"));
        subsumptionTable.put("uri", getAsList("element"));
        subsumptionTable.put("boolean", getAsList("element"));
        subsumptionTable.put("dateTime", getAsList("element"));
        subsumptionTable.put("date", getAsList("element"));
        subsumptionTable.put("time", getAsList("element"));
        subsumptionTable.put("instant", getAsList("element"));
        subsumptionTable.put("decimal", getAsList("element"));
        subsumptionTable.put("unsignedInt",getAsList("integer","element"));
        subsumptionTable.put("integer", getAsList("element"));
        subsumptionTable.put("positiveInt", getAsList("integer", "element"));
        subsumptionTable.put("Ratio", getAsList("element"));
        subsumptionTable.put("Period", getAsList("element"));
        subsumptionTable.put("Range", getAsList("element"));
        subsumptionTable.put("Attachment", getAsList("element"));
        subsumptionTable.put("Identifier", getAsList("element"));
        subsumptionTable.put("HumanName", getAsList("element"));
        subsumptionTable.put("Annotation", getAsList("element"));
        subsumptionTable.put("Address", getAsList("element"));
        subsumptionTable.put("SampledData", getAsList("element"));
        subsumptionTable.put("Quantity", getAsList("element"));
        subsumptionTable.put("CodeableConcept", getAsList("element"));
        subsumptionTable.put("Signature", getAsList("element"));
        subsumptionTable.put("Coding", getAsList("element"));
        subsumptionTable.put("Timing", getAsList("element"));
        subsumptionTable.put("Age", getAsList("Quantity"));
        subsumptionTable.put("Distance", getAsList("Quantity"));
        subsumptionTable.put("SimpleQuantity", getAsList("Quantity"));
        subsumptionTable.put("Duration", getAsList("Quantity"));
        subsumptionTable.put("Count", getAsList("Quantity"));
        subsumptionTable.put("Money", getAsList("Quantity"));
    }

    /**
     * Helper method to convert a string array to an ArrayList
     *
     * @param items
     * @return
     */
    private static List<String> getAsList(String ... items) {
        return new ArrayList<String>(
                Arrays.asList(items));
    }
}
