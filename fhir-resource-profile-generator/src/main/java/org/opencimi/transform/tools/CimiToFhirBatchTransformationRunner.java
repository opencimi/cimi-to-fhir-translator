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
package org.opencimi.transform.tools;

import org.opencimi.transform.fhir.CimiToFhirTranslator;
import org.opencimi.transform.translator.fhir.FhirLogicalProfileGenerator;

public class CimiToFhirBatchTransformationRunner {

    /**
     * Routine converting the XMI representation of an AML model into its BMM equivalent.
     *
     * @param args
     */
    public static void main(String[] args) {

        if (validArguments(args)) {
            String configFilePath = null;
            configFilePath = args[0];
            CimiTransformHelper helper = new CimiTransformHelper(configFilePath);
            helper.initialize();
            FhirLogicalProfileGenerator logicalProfileGenerator = new FhirLogicalProfileGenerator("http://opencimi.org/logical-model/fhir");
            CimiToFhirTranslator translator = new CimiToFhirTranslator(helper, logicalProfileGenerator);
            translator.initialize();
            translator.generateFhirResourceProfiles();
        } else {

        }
    }


    /**
     * Executable takes the following arguments:se {                                                                                                                       * <ul>//logger.error("You have entered an invalid number of arguments. Please enter the path to config.xml file");               * <li>The path to the config.xml file to run this routine. If this argument is omitted, application will look for a config.xml file at the top-level of the classpath</li>                                                                                                                           * <li>The output directory for the generated BMM files. This argument is required and must end with a file separator such as '/'</li>
     * </ul>
     *
     * @param args
     * @return
     */

    public static boolean validArguments(String[] args) {
        boolean isValid = true;
        if (args.length != 1) {
            System.out.println("You have entered an invalid number of arguments. Please enter the path to your config.xml file");
            isValid = false;
        }
        return isValid;
    }
}
