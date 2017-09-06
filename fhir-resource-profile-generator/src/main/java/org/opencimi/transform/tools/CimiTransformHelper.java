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

import org.opencimi.transform.*;
import org.opencimi.transform.serializer.TransformationSerializer;
import org.openehr.bmm.core.BmmClass;
import org.openehr.bmm.core.BmmModel;
import org.openehr.bmm.core.BmmPackage;
import org.openehr.bmm.persistence.PersistedBmmSchema;
import org.openehr.bmm.persistence.deserializer.BmmSchemaDeserializer;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CimiTransformHelper {

    private String configurationFilePath;
    private Configuration config;
    private BmmModel bmmModel;

    public CimiTransformHelper(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public void initialize() {
        this.config = ConfigurationLoader.load(configurationFilePath);
        this.bmmModel = loadCimiModels();
    }

    public String getConfigurationFilePath() {
        return configurationFilePath;
    }

    public void setConfigurationFilePath(String configurationFilePath) {
        this.configurationFilePath = configurationFilePath;
    }

    public Configuration getConfig() {
        return config;
    }

    public BmmModel getBmmModel() {
        return bmmModel;
    }

    public BmmClass flattenClinicalStatement(String statementName, String statementTopicClassName, String statementContextClassName) {
        BmmClass clinicalStatement = bmmModel.getClassDefinition("ClinicalStatement");
        BmmClass clinicalStatementFlattened = clinicalStatement.flattenBmmClass();
        clinicalStatementFlattened.setName(statementName);
        clinicalStatementFlattened.getProperties().remove("topic");
        clinicalStatementFlattened.getProperties().remove("context");
        BmmClass medicationAct = bmmModel.getClassDefinition(statementTopicClassName).flattenBmmClass();
        BmmClass medicationOrder = bmmModel.getClassDefinition(statementContextClassName).flattenBmmClass();
        clinicalStatementFlattened.getProperties().putAll(medicationAct.getProperties());
        medicationAct.getProperties().put("topicKey", medicationAct.getProperties().get("key"));
        medicationAct.getProperties().remove("key");
        clinicalStatementFlattened.getProperties().putAll(medicationOrder.getProperties());
        return clinicalStatementFlattened;
    }

    public BmmModel loadCimiModels() {
        PersistedBmmSchema bmmSchema = null;
        PersistedBmmSchema previousSchema = null;
        for (String persistedBmmSchemaPath : config.getBmmFiles()) {
            System.out.println("Processing " + persistedBmmSchemaPath);
            BmmSchemaDeserializer schemaDeserializer = new BmmSchemaDeserializer();
            bmmSchema = schemaDeserializer.deserialize(persistedBmmSchemaPath);
            if (previousSchema != null) {
                bmmSchema.merge(previousSchema);
            }
            previousSchema = bmmSchema;
        }

        bmmSchema.createBmmSchema();
        bmmModel = bmmSchema.getBmmModel();
        config.getClinicalStatementConfigurations().forEach( statement -> {
            BmmPackage boundStatementPkg = new BmmPackage("boundstatement");
            bmmModel.addPackage(boundStatementPkg);
            BmmClass clinicalStatement = flattenClinicalStatement(statement.getStatementName(), statement.getStatementTopicName(), statement.getStatementContextName());
            boundStatementPkg.addClass(clinicalStatement);
            bmmModel.addClassDefinition(clinicalStatement);
        });
        return bmmSchema.getBmmModel();
    }

    /**
     * Routine converting the XMI representation of an AML model into its BMM equivalent.
     *
     * @param args
     */
    public static void main(String[] args) {

        if (validArguments(args)) {
            String configFileDir = null;
            configFileDir = args[0];
            CimiTransformHelper helper = new CimiTransformHelper(configFileDir);
            helper.initialize();
            BmmClass clinicalStatementFlattened = helper.flattenClinicalStatement("MedicationOrderStatement","MedicationAct","MedicationOrder");
            helper.createTransformationTemplate(clinicalStatementFlattened);
        } else {
            //logger.error("You have entered an invalid number of arguments. Please enter the path to config.xml file");
        }
    }

    public void createTransformationTemplate(BmmClass clinicalStatementFlattened) {
        List<ModelTransform> transforms = new ArrayList<>();
        String transformFileName = clinicalStatementFlattened.getName() + "Transform";
        ModelTransform transform = new ModelTransform(transformFileName);
        transforms.add(transform);
        TransformInput transformSource = new TransformInput("CIMI", clinicalStatementFlattened.getName(), "http://opencimi.org/structuredefinition/" + clinicalStatementFlattened.getName());
        transform.setSource(transformSource);
        TransformInput transformTarget = new TransformInput("FHIR", "TBD", "http://hl7.org/fhir/structuredefinition/TBD");
        transform.setTarget(transformTarget);
        RuleGroup ruleGroup = new RuleGroup();
        transform.addRuleGroup(ruleGroup);
        clinicalStatementFlattened.getProperties().forEach((propertyName, property) -> {
            Rule rule = new Rule();
            ruleGroup.addRule(rule);
            RuleSource source = new RuleSource();
            rule.addSource(source);
            ModelAttribute attribute = new ModelAttribute(propertyName);
            source.addAttribute(attribute);
            RuleTarget target = new RuleTarget(new TypeConversion("fhir:copy"));
            rule.addTarget(target);
            target.addAttribute(new ModelAttribute("TBD"));
        });
        TransformationSerializer transformationSerializer = new TransformationSerializer();
        String serializedTransform = transformationSerializer.serialize(transforms);
        try (PrintWriter out = new PrintWriter(config.getOutputDirectory() + transformFileName + ".xml")) {
            out.println(serializedTransform);
        } catch (Exception e) {
            e.printStackTrace();
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
