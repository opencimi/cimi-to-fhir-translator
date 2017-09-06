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
package org.hl7.fhir.utilities;

import ca.uhn.fhir.context.FhirContext;
import com.sun.istack.internal.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.r4.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.hapi.ctx.PrePopulatedValidationSupport;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.utils.StructureMapUtilities;
import org.hl7.fhir.r4.utils.transform.FhirTransformationEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class FhirTransformationTest {
    @Parameterized.Parameter(0)
    public String m0;

    @Parameterized.Parameter(1)
    public String m1;

    @Parameterized.Parameters
    public static Collection<Object[]> getResource() throws URISyntaxException {
        String uri = StructureMapTests2.class.getClassLoader().getResource("test.map").toString().substring(6);
        File inFile = new File(uri + "/mapping/logical/structuredefinition-Colorectal.xml");
        File outFile = new File(uri + "/mapping/logical/lea.xml");
        Object[][] data = new Object[][] {{inFile.getPath(), outFile.getPath()}};
        return Arrays.asList(data);
    }

    /**
     * path to the files used to test the profile generator.
     */
    String resourcePath = null;

    /**
     * The logger object.
     */
    private static final Logger logger = LogManager.getLogger(StructureMapTests2.class);

    /**
     * The basic fhir context used to parse structure definitions.
     */
    FhirContext context;

    /**
     * HapiFhirContext used when building strucutre map utilities.
     */
    HapiWorkerContext hapiContext;

    /**
     * Used to validate definitions as well as add new structure definitions to a registry.
     */
    PrePopulatedValidationSupport validationSupport;


    /**
     * Sets up the resource paths as well as create the contexts using a defalut validator to start with.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        resourcePath = getFileFromURL().getParent();
        this.context = FhirContext.forR4();
    }

    /**
     * Make the context objects null, safely removing them from memory.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        this.context = null;
        this.hapiContext = null;
    }

    /**
     * Used in 'setUp' where the location of the test files are located.
     * @return
     */
    private File getFileFromURL() {
        URL url = this.getClass().getClassLoader().getResource("test.map");
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
            logger.error(e.getMessage());
        }
        return file;
    }

    /**
     * For a mapping that creates profiles versus
     * @throws Exception
     */
    @Test
    public void testMapping() throws Exception{
        Map<String, StructureMap> maps = new HashMap<>();
        this.validationSupport = new PrePopulatedValidationSupport();
        DefaultProfileValidationSupport validation = new DefaultProfileValidationSupport();
        for (StructureDefinition sd : new DefaultProfileValidationSupport().fetchAllStructureDefinitions(this.context)) {
            this.validationSupport.addStructureDefinition(sd);
        }
        StructureDefinition strCD = this.context.newXmlParser().parseResource(StructureDefinition.class, new FileReader((new File(resourcePath + "/mapping/logical/CODEDTEXT.xml"))));
        this.validationSupport.addStructureDefinition(strCD);
        //this.validationSupport.addStructureDefinition(this.createTestStructure());
        this.hapiContext = new HapiWorkerContext(this.context, this.validationSupport);
        StructureMap map = null;
        FhirTransformationEngine scu = new FhirTransformationEngine(hapiContext, maps, null, null);
        try {
            //map = this.context.newXmlParser().parseResource(StructureMap.class, new FileReader((new File(resourcePath + "/mapping/maps/testStructuremap.xml"))));
            map = createTestStructuremap();
            maps.put(map.getUrl(), map);
            List<StructureDefinition> result = scu.analyse(null, map).getProfiles();
            File currentDir = new File(".");
            String filePath = currentDir.getAbsolutePath();

            for (StructureDefinition sd : result) {
                List<ElementDefinition> definitions = sd.getDifferential().getElement();

                this.context.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(sd, new FileWriter(filePath.replace(".", "target/") + sd.getId() + ".xml"));
            }
        } catch (Exception e) {
            //logger.error(e.getMessage());
            System.err.println(e.getMessage());
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    /**
     * Creates a hard coded test structure for testing purposes
     * @return
     */
    public StructureDefinition createTestStructure(){
        StructureDefinition sd = new StructureDefinition();
        sd.setId("TestStructure");
        sd.setUrl("http://opencimi.org/structuredefinition/TestStructure");
        sd.setStatus(Enumerations.PublicationStatus.DRAFT);
        sd.setName("TestStructure");
        sd.setType("TestStructure");
        sd.setSnapshot(this.createTestSnapshot());
        sd.setDifferential(this.createTestDiff());
        sd.setKind(StructureDefinition.StructureDefinitionKind.LOGICAL);

        return sd;
    }

    /**
     * Creates a differential for testing purposes
     * @return
     */

    public StructureDefinition.StructureDefinitionDifferentialComponent createTestDiff(){
        StructureDefinition.StructureDefinitionDifferentialComponent retVal = new StructureDefinition.StructureDefinitionDifferentialComponent();
        List<ElementDefinition> eList = new ArrayList<>();
        ElementDefinition ed0 = new ElementDefinition();
        //ElementDefinition.ElementDefinitionBaseComponent base = new ElementDefinition.ElementDefinitionBaseComponent();
        //base.setId("http://hl7.org/fhir/StructureDefinition/Element");
        ed0.setId("TestStructure");
        ed0.setSliceName("TestStructure");
        ed0.setPath("TestStructure");
        // ed0.setBase(base);
        ed0.setMin(1);
        ed0.setMax("1");
        eList.add(ed0);


        ElementDefinition ed = new ElementDefinition();
        //ElementDefinition.ElementDefinitionBaseComponent base = new ElementDefinition.ElementDefinitionBaseComponent();
        //base.setId("http://hl7.org/fhir/StructureDefinition/Element");
        ed.setId("system");
        ed.setSliceName("system");
        ed.setPath("TestStructure.system");
        //ed.setBase(base);
        ed.setFixed(new UriType().setValue("HTTP://opencimi.org/structuredefinition/TestStructure.html"));
        //ed.setType(this.createTypeRefList());
        eList.add(ed);
        retVal.setElement(eList);
        return retVal;


    }

    /**
     * Creates a snapshot for testing purposes
     * @return
     */
    public StructureDefinition.StructureDefinitionSnapshotComponent createTestSnapshot(){
        StructureDefinition.StructureDefinitionSnapshotComponent retVal = new StructureDefinition.StructureDefinitionSnapshotComponent();
        List<ElementDefinition> eList = new ArrayList<>();
        ElementDefinition ed0 = new ElementDefinition();
        //ElementDefinition.ElementDefinitionBaseComponent base = new ElementDefinition.ElementDefinitionBaseComponent();
        //base.setId("http://hl7.org/fhir/StructureDefinition/Element");
        ed0.setId("TestStructure");
        ed0.setSliceName("TestStructure");
        ed0.setPath("TestStructure");
        // ed0.setBase(base);
        ed0.setMin(1);
        ed0.setMax("1");
        eList.add(ed0);


        ElementDefinition ed = new ElementDefinition();
        //ElementDefinition.ElementDefinitionBaseComponent base = new ElementDefinition.ElementDefinitionBaseComponent();
        //base.setId("http://hl7.org/fhir/StructureDefinition/Element");
        ed.setId("system");
        ed.setSliceName("system");
        ed.setPath("TestStructure.system");
        //ed.setBase(base);
        ed.setFixed(new UriType().setValue("HTTP://opencimi.org/structuredefinition/TestStructure"));
        //ed.setType(this.createTypeRefList());
        ed.setMin(1);
        ed.setMax("1");
        eList.add(ed);
        retVal.setElement(eList);
        return retVal;


    }

    /**
     * Creates a hard coded Structure Map for testing
     * @return
     * @throws Exception
     */
    public StructureMap createTestStructuremap() throws Exception {
        StructureMap retMap = new StructureMap();
        retMap.setUrl("http://opencimi.org/structuremap/testtransform");
        retMap.setName("TestTransform");
        retMap.setStatus(Enumerations.PublicationStatus.DRAFT);
        retMap.setStructure(this.createMapStructureList());
        retMap.setGroup(this.buildTestGroup());
        return retMap;
    }

    /**
     *
     * @return
     */
    public List<StructureMap.StructureMapStructureComponent> createMapStructureList(){
        List<StructureMap.StructureMapStructureComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapStructureComponent source = new StructureMap.StructureMapStructureComponent();
        StructureMap.StructureMapStructureComponent target = new StructureMap.StructureMapStructureComponent();
        source.setUrl("http://opencimi.org/logical-model/fhir/CODED_TEXT");
        source.setMode(StructureMap.StructureMapModelMode.SOURCE);
        target.setUrl("http://hl7.org/fhir/StructureDefinition/Coding");
        target.setMode(StructureMap.StructureMapModelMode.TARGET);
        retVal.add(source);
        retVal.add(target);
        return retVal;
    }

    /**
     *
     * @return
     * @throws Exception
     */
    public List<StructureMap.StructureMapGroupComponent> buildTestGroup() throws Exception{
        List<StructureMap.StructureMapGroupComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapGroupComponent group = new StructureMap.StructureMapGroupComponent();
        group.setName("TestStructureToCoding");
        group.setTypeMode(StructureMap.StructureMapGroupTypeMode.TYPEANDTYPES);
        group.setInput(this.buildTestInput());
        group.setRule(this.buildTestRules());
        retVal.add(group);
        return retVal;
    }

    /**
     * Builds teh structure map inputs for testing
     * @return
     */
    public List<StructureMap.StructureMapGroupInputComponent> buildTestInput(){
        List<StructureMap.StructureMapGroupInputComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapGroupInputComponent sourceIn = new StructureMap.StructureMapGroupInputComponent();
        StructureMap.StructureMapGroupInputComponent targetIn = new StructureMap.StructureMapGroupInputComponent();
        sourceIn.setName("source");
        sourceIn.setType("CODED_TEXT");
        sourceIn.setMode(StructureMap.StructureMapInputMode.SOURCE);
        targetIn.setName("target");
        targetIn.setType("Coding");
        targetIn.setMode(StructureMap.StructureMapInputMode.TARGET);
        retVal.add(sourceIn);
        retVal.add(targetIn);
        return retVal;
    }

    /**
     *
     * @return Creates Rules for testing
     * @throws Exception
     */
    public List<StructureMap.StructureMapGroupRuleComponent> buildTestRules() throws Exception{
        List<StructureMap.StructureMapGroupRuleComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapGroupRuleComponent userSelected = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent system = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent extension = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent code = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent version = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent display = new StructureMap.StructureMapGroupRuleComponent();

        /*
         * Coding.System
         */
        system.setName("Coding.system");
        StructureMap.StructureMapGroupRuleSourceComponent source = new StructureMap.StructureMapGroupRuleSourceComponent();
        StructureMap.StructureMapGroupRuleTargetComponent target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("terminology_id");
        source.setVariable("term");
        system.addSource(source);
        target.setContext("target");
        target.setElement("system");
        target.setVariable("sys");
        system.addTarget(target);

        retVal.add(system);

        /*
         * Coding.version
         */
        version.setName("Coding.version");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("terminology_version");
        source.setVariable("tv");
        version.addSource(source);
        target.setContext("target");
        target.setElement("version");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("tv")));
        version.addTarget(target);
        retVal.add(version);

        /*
         * Coding.code
         */
        code.setName("Coding.code");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("code");
        source.setVariable("c");
        code.addSource(source);
        target.setContext("target");
        target.setElement("code");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("c")));
        code.addTarget(target);
        retVal.add(code);

        /*
         * Coding.display
         */
        display.setName("Coding.display");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("term");
        source.setVariable("term");
        display.addSource(source);
        target.setContext("target");
        target.setElement("display");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("term")));
        display.addTarget(target);
        retVal.add(display);

        /*
         * Coding.UserSelected
         */
        userSelected.setName("Coding.userSelected");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        userSelected.addSource(source);
        target.setContext("target");
        target.setElement("userSelected");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new BooleanType(false)));
        userSelected.addTarget(target);

        retVal.add(userSelected);

        /**
         * Add URI Extension
         */
        extension.setName("Coding.extension");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        source.setContext("source");
        source.setElement("uri");
        source.setVariable("uri");
        extension.addSource(source);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("target");
        target.setElement("extension");
        target.setVariable("ex");
        extension.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("url");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new UriType("http://opencimi.org/fhir/extension/coding/coded_text_uri.html")));
        extension.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("value");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("uri")));
        extension.addTarget(target);

        retVal.add(extension);

        return retVal;
    }

    /**
     * creates target Parameters incliding type
     * @param params
     * @return
     * @throws Exception
     */
    public List<StructureMap.StructureMapGroupRuleTargetParameterComponent> constructParameters(TargetParam[] params) throws Exception{
        List<StructureMap.StructureMapGroupRuleTargetParameterComponent> parameterComponents = new ArrayList<>();
        for (TargetParam tp : params){
            if (tp.getType() == "Id") //TODO: Convert TypeParam.Type into an Enum.
                parameterComponents.add(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new IdType().setValue(tp.getValue())));
            else if (tp.getType() == "String")
                parameterComponents.add(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue((new StringType().setValue(tp.getValue()))));
            else if (tp.getType() == "Boolean") {
                boolean bValue = Boolean.getBoolean(tp.getValue());
                parameterComponents.add(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new BooleanType().setValue(bValue)));
            }
            else if (tp.getType() == "Integer"){
                int iValue = Integer.getInteger(tp.getValue());
                parameterComponents.add(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new IntegerType().setValue(iValue)));
            }
            else if (tp.getType() == "Decimal") {
                long lValue = Long.getLong(tp.getValue());
                parameterComponents.add(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new DecimalType(lValue)));
            }
        }
        return parameterComponents;
    }

    /**
     * Target Parameter Object
     */
    public class TargetParam {
        /**
         * Constructor for Target Parameter
         * @param type
         * @param value
         */
        public TargetParam(String type, String value){

            this.type = type;
            this.value = value;
        }

        /**
         * Type for parameter
         */
        private String type;

        /**
         * Value for the parameter
         */
        private String value;


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
