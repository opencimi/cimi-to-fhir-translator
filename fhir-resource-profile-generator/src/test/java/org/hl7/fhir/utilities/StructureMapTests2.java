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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public class StructureMapTests2 {
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
        this.context = FhirContext.forDstu3();
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
     * Test and generate Colorectal
     * @throws FileNotFoundException
     * @throws Exception
     */
    @Test
    public void testTransformProfilesCDATwo() throws FileNotFoundException, Exception {
        Map<String, StructureMap> maps = new HashMap<String, StructureMap>();
        StructureMapUtilities scu;
        StructureDefinition sd1;
        PrePopulatedValidationSupport support = new PrePopulatedValidationSupport();
        try {
            sd1 = this.context.newXmlParser().parseResource(StructureDefinition.class, new FileReader(new File(resourcePath + "/mapping/logical/structuredefinition-Colorectal.xml")));
            if (sd1.getId().contains("/"))
                sd1.setId(sd1.getId().split("/")[sd1.getId().split("/").length - 1]);
            support.addStructureDefinition(sd1);
            this.loadDstu3Structures(support, resourcePath + "/mapping/logical/StructureDefinitions/");
        } catch (Exception e) {
            //logger.error(e.getMessage());
            System.err.println(e.getMessage());
        }
        this.hapiContext = new HapiWorkerContext(this.context, support);
        StructureMap map = null;
        scu = new StructureMapUtilities(this.hapiContext, maps, null, null);
        try {
            map = scu.parse(TextFile.fileToString(resourcePath + "/mapping/maps/colorectal3.map"));
            maps.put(map.getUrl(), map);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            logger.error(e.getMessage());
        }
        File currentDir = new File(".");
        String filePath = currentDir.getAbsolutePath();
        List<StructureDefinition> result = scu.analyse(null, map).getProfiles();
        for (StructureDefinition sd : result)
            this.context.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(sd, new FileWriter(filePath.replace(".", "target/") + sd.getId() + ".xml"));
    }

    @Test
    public void testTransformProfiles() throws FileNotFoundException, Exception {
        Map<String, StructureMap> maps = new HashMap<String, StructureMap>();
        StructureMapUtilities scu;
        StructureDefinition sd1;
        StructureDefinition sd2;
        PrePopulatedValidationSupport support = new PrePopulatedValidationSupport();
        try {
            sd1 = this.context.newJsonParser().parseResource(StructureDefinition.class, new FileReader(new File(resourcePath + "/mapping/logical/CODED_TEXT.profile.json")));
//            if (sd1.getId().contains("/"))
//                sd1.setId(sd1.getId().split("/")[sd1.getId().split("/").length - 1]);
            if (sd1.getId() == null)
                sd1.setId(sd1.getUrl().split("/")[sd1.getUrl().split("/").length - 1]);
            support = new PrePopulatedValidationSupport();
            support.addStructureDefinition(sd1);
            this.loadDstu3Structures(support, resourcePath + "/mapping/logical/StructureDefinitions/");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        File currentDir = new File(".");
        String filePath = currentDir.getAbsolutePath();
        this.hapiContext = new HapiWorkerContext(this.context, support);
        StructureMap map = null;
        scu = new StructureMapUtilities(this.hapiContext, maps, null, null);
        try {
            //map = scu.parse(TextFile.fileToString(resourcePath + "/mapping/maps/Coded_TextToCoding2.map"));
            //this.context.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(map, new FileWriter(filePath.replace(".", "target/") + map.getId() + ".xml"));
            map = this.createTestStructuremap();
            maps.put(map.getUrl(), map);
        } catch (Exception e) {
            //logger.error(e.getMessage());
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }

        List<StructureMap.StructureMapGroupRuleTargetComponent> elementList = new ArrayList<>();


        List<StructureDefinition> result = scu.analyse(null, map).getProfiles();
        for (StructureDefinition sd : result) {
            List<ElementDefinition> definitions = sd.getDifferential().getElement();
            this.context.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(sd, new FileWriter(filePath.replace(".", "target/") + sd.getId() + ".xml"));
        }
    }

/*
    @Test
    public void runHapiMain(){
        HapiFhirMapping.main(new String[] {m0, m1});
    }
*/

    public void loadDstu3Structures(PrePopulatedValidationSupport validationSupport, String dir) throws FileNotFoundException {
        List<StructureDefinition> structures = new ArrayList<>();
        File directory = new File(dir);
        File[] directoryListing = directory.listFiles();
        if (directoryListing != null){
            for (File child : directoryListing){
                StructureDefinition sd = this.context.newJsonParser().parseResource(StructureDefinition.class, new FileReader(child));
                /*if (sd.getUrl().contains("/"))
                    sd.setUrl(sd.getUrl().split("/")[sd.getUrl().split("/").length - 1]);*/
                validationSupport.addStructureDefinition(sd);
            }
        }
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

    public boolean removeDifferentialElememnt(String targetValue, StructureDefinition sd){
        if (targetValue == null || sd.getDifferential() == null)
            return false;
        StructureDefinition.StructureDefinitionDifferentialComponent diff = sd.getDifferential();
        for (ElementDefinition e : diff.getElement()) {
            if (!e.getPath().contains("."))
                continue;
            if (e.getPath().endsWith("."+targetValue)) {
                sd.getDifferential().getElement().remove(e);
                return true;
            }
        }

        return false;
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
        for (StructureDefinition sd : new DefaultProfileValidationSupport().fetchAllStructureDefinitions(this.context)) {/*
            if (sd.getId().contains("/")) {
                sd.setId(sd.getId().split("/")[sd.getId().split("/").length - 1]);
            }*/
            this.validationSupport.addStructureDefinition(sd);
        }
        this.validationSupport.addStructureDefinition(this.createTestStructure());
        this.hapiContext = new HapiWorkerContext(this.context, this.validationSupport);
        StructureMap map = null;
        StructureMapUtilities scu = new StructureMapUtilities(hapiContext, maps, null, null);
        try {
            map = this.context.newXmlParser().parseResource(StructureMap.class, new FileReader((new File(resourcePath + "/mapping/maps/testStructuremap.xml"))));
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
     * Creates code generated strucutres for transformation against an existing FHIR Structure (Coding)
     * @throws Exception
     */
    @Test
    public void testMappingTransform() throws Exception{
        Map<String, StructureMap> maps = new HashMap<>(); //Instantiate a hashmap for StructureMaps
        this.validationSupport = new PrePopulatedValidationSupport();  //Create Validation Instance
        DefaultProfileValidationSupport dSupport = new DefaultProfileValidationSupport();
        this.loadDstu3Structures( this.validationSupport, resourcePath + "/mapping/logical/StructureDefinitions/");
        StructureDefinition sd1 = this.createTestStructure(); //Calls a method that constructs a comp
        this.validationSupport.addStructureDefinition(sd1); //Add custom structure to validation support.
        this.hapiContext = new HapiWorkerContext(this.context, this.validationSupport);
        StructureMap map = null;
        try {
            map = this.createTestStructuremap();
            maps.put(map.getUrl(), map);
        } catch (Exception e) {
            //logger.error(e.getMessage());
            System.err.println(e.getMessage());
            System.err.println(e.getStackTrace());
        }
        StructureMapUtilities scu = new StructureMapUtilities(hapiContext, maps, null, null);
        List<StructureDefinition> result = scu.analyse(null, map).getProfiles();
    }


    /**
     * Rewrites a structure map (if valid) to an XML format
     */
    @Test
    public void currentStructureMap(){
        StructureMap map = null;
        this.validationSupport = new PrePopulatedValidationSupport();
        this.hapiContext = new HapiWorkerContext(this.context, this.validationSupport);
        StructureMapUtilities scu = new StructureMapUtilities(this.hapiContext, new HashMap<>(), null, null);
        try {
            map = scu.parse(TextFile.fileToString(resourcePath + "/mapping/maps/colorectal3.map"));
            this.context.newXmlParser().setPrettyPrint(true).encodeResourceToWriter(map, new FileWriter(new File(".").getAbsolutePath().replace(".", "target/") + map.getId() + "test.xml"));
        } catch (Exception e) {
            logger.error(e.getMessage());
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
     *
     * @return
     */
    public List<ElementDefinition.TypeRefComponent> createTypeRefList(){
        List<ElementDefinition.TypeRefComponent> retVal = new ArrayList<>();
        retVal.add(new ElementDefinition.TypeRefComponent().setCode("uri").setProfile("System"));
        return retVal;

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

    public List<StructureMap.StructureMapStructureComponent> createMapStructureListShort() {
        List<StructureMap.StructureMapStructureComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapStructureComponent source = new StructureMap.StructureMapStructureComponent();
        StructureMap.StructureMapStructureComponent target = new StructureMap.StructureMapStructureComponent();
        source.setUrl("http://opencimi.org/logical-model/fhir/CODED_TEXT");
        source.setMode(StructureMap.StructureMapModelMode.SOURCE);
        retVal.add(source);

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
        targetIn.setType("StructureDefinition/Coding");
        targetIn.setMode(StructureMap.StructureMapInputMode.TARGET);
        retVal.add(sourceIn);
        retVal.add(targetIn);
        return retVal;
    }

    public List<StructureMap.StructureMapGroupInputComponent> buildTestInputShort(){
        List<StructureMap.StructureMapGroupInputComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapGroupInputComponent sourceIn = new StructureMap.StructureMapGroupInputComponent();
        StructureMap.StructureMapGroupInputComponent targetIn = new StructureMap.StructureMapGroupInputComponent();
        sourceIn.setName("source");
        sourceIn.setType("CODED_TEXT");
        sourceIn.setMode(StructureMap.StructureMapInputMode.SOURCE);
        retVal.add(sourceIn);
        return retVal;
    }



    public List<StructureMap.StructureMapGroupRuleComponent> buildTestRulesShort() throws Exception {
        List<StructureMap.StructureMapGroupRuleComponent> retVal = new ArrayList<>();
        StructureMap.StructureMapGroupRuleComponent coding = new StructureMap.StructureMapGroupRuleComponent();
        StructureMap.StructureMapGroupRuleComponent codingCode = new StructureMap.StructureMapGroupRuleComponent();


        coding.setName("coding");

        StructureMap.StructureMapGroupRuleSourceComponent source = new StructureMap.StructureMapGroupRuleSourceComponent();
        StructureMap.StructureMapGroupRuleTargetComponent target = new StructureMap.StructureMapGroupRuleTargetComponent();

        source.setContext("source");
        source.setElement("terminology_id");
        source.setVariable("term");
        coding.addSource(source);
//        source = new StructureMap.StructureMapGroupRuleSourceComponent();
//        source.setContext("source");
//        source.setElement("code");
//        source.setVariable("code");
//        coding.addSource(source);
        //target.setContext("target");
       // target.setElement("code");
        /*target.setTransform(StructureMap.StructureMapTransform.C);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("term")));
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("code")));
        coding.addTarget(target);*/
        //coding.addTarget();
        codingCode.setName("%20Coding.code");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        source.setContext("source");
        source.setElement("code");
        source.setVariable("code");
        codingCode.addSource(source);
        target.setContext("target");
        target.setTransform(StructureMap.StructureMapTransform.C);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("term")));
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("code")));
        codingCode.addTarget(target);
        coding.addRule(codingCode);
        retVal.add(coding);
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

        /*
         * Coding.UserSelected
         */
        userSelected.setName("Coding.userSelected");
        StructureMap.StructureMapGroupRuleSourceComponent source = new StructureMap.StructureMapGroupRuleSourceComponent();
        StructureMap.StructureMapGroupRuleTargetComponent target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        userSelected.addSource(source);
        target.setContext("target");
        target.setElement("userSelected");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent().setValue(new BooleanType(false)));
        userSelected.addTarget(target);
        retVal.add(userSelected);
        /*
         * Coding.System
         */
        system.setName("Coding.system");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("terminology_id");
        source.setVariable("term");
        system.addSource(source);
        target.setContext("target");
        target.setElement("system");
        target.setVariable("sys");
        system.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("sys");
        target.setElement("extension");
        target.setVariable("ex");
        system.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("url");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new UriType("opencimi.org/structuremaps/Coded_TextToCoding/system/extension")));
        system.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("value");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("term")));
        system.addTarget(target);

        retVal.add(system);
        /*
         * Coding.extension
         */
        extension.setName("Coding.extension");
        source = new StructureMap.StructureMapGroupRuleSourceComponent();
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        source.setContext("source");
        source.setElement("uri");
        source.setVariable("su");
        extension.addSource(source);
        target.setContext("target");
        target.setElement("extension");
        target.setVariable("ex");
        extension.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("url");
        //target.setContextType(StructureMap.StructureMapContextType.VARIABLE);
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new UriType("opencimi.org/structuremaps/Coded_TextToCoding/extension")));
        extension.addTarget(target);
        target = new StructureMap.StructureMapGroupRuleTargetComponent();
        target.setContext("ex");
        target.setElement("value");
        target.setTransform(StructureMap.StructureMapTransform.COPY);
        target.addParameter(new StructureMap.StructureMapGroupRuleTargetParameterComponent(new IdType("su")));
        extension.addTarget(target);
        retVal.add(extension);
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

        return retVal;
    }


    /**
     * Builds the sources for mapping
     * @param sources
     * @return
     */
    public List<StructureMap.StructureMapGroupRuleSourceComponent> buildSourceList(StructureMap.StructureMapGroupRuleSourceComponent[] sources){
        List<StructureMap.StructureMapGroupRuleSourceComponent> retVal = new ArrayList<>();
        retVal.addAll(Arrays.asList(sources));
        return retVal;
    }

    /**
     * Builds a source statement for mapping
     * @param context
     * @param element
     * @param variable
     * @param type
     * @param min
     * @param max
     * @return
     */
    public StructureMap.StructureMapGroupRuleSourceComponent buildSource(String context, @Nullable String element, @Nullable String variable, @Nullable String type, @Nullable Integer min, @Nullable String max){
        StructureMap.StructureMapGroupRuleSourceComponent retVal = new StructureMap.StructureMapGroupRuleSourceComponent(); //This methods is 100% dynamic, can be used in  main program
        retVal.setContext(context);
        if (element != null)
            retVal.setElement(element);
        if (variable != null)
            retVal.setVariable(variable);
//        if (type != null)
//            retVal.setType(type);
//        if (min != null)
//            retVal.setMin(min);
//        if (max != null)
//            retVal.setMax(max);
        return retVal;
    }

    /**
     * Builds a holder for a list of targets
     * @param sources
     * @return
     */
    public List<StructureMap.StructureMapGroupRuleTargetComponent> buildTargetList(StructureMap.StructureMapGroupRuleTargetComponent[] sources){
        List<StructureMap.StructureMapGroupRuleTargetComponent> retVal = new ArrayList<>();
        retVal.addAll(Arrays.asList(sources));
        return retVal;
    }

    /**
     * Builds dynamic targets for mapping
     * @param context
     * @param element
     * @param variable
     * @param transform
     * @param params
     * @return
     * @throws Exception
     */
    public StructureMap.StructureMapGroupRuleTargetComponent buildTarget(@Nullable String context,@Nullable String element,@Nullable String variable, @Nullable StructureMap.StructureMapTransform transform, @Nullable TargetParam[] params) throws Exception{
        StructureMap.StructureMapGroupRuleTargetComponent retVal = new StructureMap.StructureMapGroupRuleTargetComponent();
        if (context != null)
            retVal.setContext(context);
        if (element != null)
            retVal.setElement(element);
        if (variable != null)
            retVal.setVariable(variable);
        if (transform != null)
            retVal.setTransform(transform);
        if (params != null){
            if (params.length > 0)
                retVal.setParameter(this.constructParameters(params));
        }
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

    public void mockTest(){

    }
}
