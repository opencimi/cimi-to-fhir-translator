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
 * Author: Travis Lukach
 */
package org.opencimi.fhir.profile.generator.utils;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.JsonParser;
import ca.uhn.fhir.parser.XmlParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.exceptions.DefinitionException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.r4.context.IWorkerContext;
import org.hl7.fhir.r4.elementmodel.Element;
import org.hl7.fhir.r4.elementmodel.ObjectConverter;
import org.hl7.fhir.r4.formats.IParser;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r4.model.StructureMap;
import org.hl7.fhir.r4.utils.StructureMapUtilities;
import org.hl7.fhir.utilities.TextFile;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HapiFhirMapping {
    static String resourcePath = null;
    private static final Logger logger = LogManager.getLogger(HapiFhirMapping.class);
    private static StructureMapUtilities mappingUtils;
    static IWorkerContext context;
    static HapiWorkerContext hapiContext;
    static PrePopulatedValidationSupport validationSupport; //TODO: I eventually want this to just be IValidationSupport


    public static void main(String[] args){
        resourcePath = getResourcePath();

        validationSupport = new PrePopulatedValidationSupport();
        context = new HapiWorkerContext(FhirContext.forR4(), validationSupport);
        if (context != null) {
            Map<String, StructureMap> maps = new HashMap<String, StructureMap>();
            mappingUtils = new StructureMapUtilities(context, maps, null, null);

            if (args[0] == null) {
                logger.error(
                        "The file for Structure Definition for the logical model should be provided. It should be the fisrt argument");
                return;
            }
            if (args[1] == null) {
                logger.error(
                        "The file for the mapping of the logical model to the FHIR profile should be provided. It should be the second argument");
                return;
            }

            File arg0 = new File(args[0]);
            File arg1 = new File(args[1]);
            if (arg0.isFile() && arg1.isFile()) {
                generateProfile(args[0], args[1], maps);
            }
            else if(arg0.isDirectory() && arg1.isDirectory()) {
                generateProfilesFromFolder(args[0], args[1], maps);
            }
            else {
                System.out.println("The arguments are not valid file or folder");
            }
            System.out.println("Successfully processed");
        }


    }

    /**
     *
     * @param logicalDefFile
     * @param mappingFile
     * @param maps
     */
    private static void generateProfile(String logicalDefFile, String mappingFile, Map<String, StructureMap> maps) {
        List<StructureDefinition> result = null;
        StructureDefinition structureDefinition = null;

        try {
            if (logicalDefFile.endsWith(".xml")) {
                structureDefinition = (StructureDefinition) hapiContext.newXmlParser().parse(new FileInputStream(logicalDefFile));
            } else if (logicalDefFile.endsWith(".json")) {
                //structureDefinition = (StructureDefinition) new JsonParser(context).parse(new FileInputStream(logicalDefFile));
                structureDefinition = (StructureDefinition)hapiContext.newJsonParser().parse(new FileInputStream(logicalDefFile));
                context.fetchResource(structureDefinition.getClass(), structureDefinition.getUrl());
            } else {
                logger.error("Wrong Logical Structure Definition file format.");
                return;
            }
            (context).fetchResource(structureDefinition.getClass(), structureDefinition.getUrl());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        StructureMap map = null;
        try {
            map = mappingUtils.parse(TextFile.fileToString(mappingFile));
            maps.put(map.getUrl(), map);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        // }
        File currentDir = new File(".");
        String filePath = currentDir.getAbsolutePath();

        try {
            result = mappingUtils.analyse(null, map).getProfiles();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        for (StructureDefinition sd : result) {
            try {
                //new XmlParser(context)
                //		.compose(new FileOutputStream(filePath.replace(".", "") + sd.getId() + ".xml"), sd);
                hapiContext.newXmlParser().compose(
                        new BufferedOutputStream(new FileOutputStream(filePath.replace(".", "target/") + sd.getId() + ".xml")),sd);

            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private static void generateProfilesFromFolder(String logicalDefFilesFolder, String mappingFilesFolder,
                                                   Map<String, StructureMap> maps) {
        List<StructureDefinition> result = null;
        StructureDefinition structureDefinition = null;

        for (String file : new File(logicalDefFilesFolder).list()) {
            try {
                if (file.endsWith(".xml")) {
                    structureDefinition = (StructureDefinition) hapiContext.newXmlParser().parse(new FileInputStream(file));
                } else if (file.endsWith(".json")) {
                    structureDefinition = (StructureDefinition) hapiContext.newJsonParser().parse(new FileInputStream(file));
                } else {
                    logger.error("Wrong Logical Structure Definition file format.");
                    return;
                }
                context.fetchResource(structureDefinition.getClass(), structureDefinition.getUrl());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        StructureMap map = null;
        for (String file : new File(mappingFilesFolder).list()) {
            try {
                map = mappingUtils.parse(TextFile.fileToString(file));
                maps.put(map.getUrl(), map);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        File currentDir = new File(".");
        String filePath = currentDir.getAbsolutePath();

        try {
            result = mappingUtils.analyse(null, map).getProfiles();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        for (StructureDefinition sd : result) {
            try {
                hapiContext.newXmlParser()
                        .compose(new BufferedOutputStream(new FileOutputStream(new File(filePath.replace(".", "") + sd.getId() + ".xml"))), sd);
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage());
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }



    private static String getResourcePath() {
        MappingTool tool = new MappingTool();
        URL url = tool.getClass().getClassLoader().getResource("test.map");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            logger.error(e.getMessage());
        }
        return file.getParent();
    }
}
