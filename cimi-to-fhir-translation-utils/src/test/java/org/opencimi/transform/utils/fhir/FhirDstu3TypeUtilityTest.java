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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by cnanjo on 10/25/16.
 */
public class FhirDstu3TypeUtilityTest {
    @Test
    public void isSubsumedBy() throws Exception {
        assertTrue(FhirDstu3TypeUtility.isSubsumedBy("code", "string"));
        assertTrue(FhirDstu3TypeUtility.isSubsumedBy("string", "element"));
        assertTrue(FhirDstu3TypeUtility.isSubsumedBy("code", "element"));//transitive
        assertFalse(FhirDstu3TypeUtility.isSubsumedBy("string", "code"));//ordered relation
        assertFalse(FhirDstu3TypeUtility.isSubsumedBy("string", "string"));//not reflexive
        assertTrue(FhirDstu3TypeUtility.isSubsumedBy("Quantity", "element"));
    }

    @Test
    public void subsumes() throws Exception {
        assertTrue(FhirDstu3TypeUtility.subsumes("Quantity", "Count"));
        assertFalse(FhirDstu3TypeUtility.subsumes("Count", "Quantity"));
        assertFalse(FhirDstu3TypeUtility.subsumes("Count", "Count"));

    }

}