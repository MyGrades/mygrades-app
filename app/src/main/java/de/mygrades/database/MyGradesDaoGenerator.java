package de.mygrades.database;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Generator to create DAOs.
 */
public class MyGradesDaoGenerator {

    private static final int VERSION = 1;
    private static final String PACKAGE = "de.mygrades.database.dao";
    private static final String DIR_PATH = "app/src/main/java";

    private static Entity university;
    private static Entity rule;
    private static Entity action;
    private static Entity actionParam;
    private static Entity transformerMapping;
    private static Entity gradeEntry;
    private static Entity overview;

    public static void main(String[] args) throws Exception {
        Schema schema = new Schema(VERSION, PACKAGE);

        addUniversity(schema);
        addRule(schema);
        addAction(schema);
        addActionParam(schema);
        /*addTransformerMapping(schema);
        addGradeEntry(schema);
        addOverview(schema);*/

        DaoGenerator daoGenerator = new DaoGenerator();
        daoGenerator.generateAll(schema, DIR_PATH);
    }

    /**
     * Add university entity.
     *
     * @param schema - database schema
     */
    private static void addUniversity(Schema schema) {
        university = schema.addEntity("University");
        university.addIdProperty();
        university.addLongProperty("universityId").unique().notNull();
        university.addStringProperty("name").notNull();
        university.addBooleanProperty("published");
        university.addStringProperty("updatedAtServer");

        // add "keep" sections
        university.setHasKeepSections(true);
    }

    /**
     * Add rule entity.
     *
     * @param schema - database schema
     */
    private static void addRule(Schema schema) {
        rule = schema.addEntity("Rule");
        rule.addIdProperty();
        rule.addLongProperty("ruleId").unique().notNull();
        rule.addStringProperty("type").notNull();
        rule.addDateProperty("lastUpdated");

        // add 1:n relation for university -> rules
        Property universityId = rule.addLongProperty("universityId").notNull().getProperty();
        university.addToMany(rule, universityId, "rules");

        // add "keep" sections
        rule.setHasKeepSections(true);
    }

    /**
     * Add action entity.
     *
     * @param schema - database schema
     */
    private static void addAction(Schema schema) {
        action = schema.addEntity("Action");
        action.addIdProperty();
        action.addLongProperty("actionId").unique().notNull();
        action.addIntProperty("position").notNull();
        action.addStringProperty("method").notNull();
        action.addStringProperty("url");
        action.addStringProperty("parseExpression");
        action.addStringProperty("parseType");

        // add 1:n relation for rule -> actions
        Property ruleId = action.addLongProperty("ruleId").notNull().getProperty();
        rule.addToMany(action, ruleId, "actions");

        // add "keep" sections
        action.setHasKeepSections(true);
    }

    /**
     * Add actionParam entity.
     *
     * @param schema - database schema
     */
    private static void addActionParam(Schema schema) {
        actionParam = schema.addEntity("ActionParam");
        actionParam.addIdProperty().primaryKey();
        actionParam.addLongProperty("actionParamId").unique().notNull();
        actionParam.addStringProperty("key").notNull();
        actionParam.addStringProperty("value");
        actionParam.addStringProperty("type");

        // add 1:n relation for action -> actionParams
        Property actionId = actionParam.addLongProperty("actionId").notNull().getProperty();
        action.addToMany(actionParam, actionId).setName("actionParams");
    }

    /**
     * Add transformerMapping entity.
     *
     * @param schema - database schema
     */
    private static void addTransformerMapping(Schema schema) {
        transformerMapping = schema.addEntity("TransformerMapping");
        transformerMapping.addIdProperty().primaryKey();
        transformerMapping.addLongProperty("ruleId").notNull();
        transformerMapping.addStringProperty("name").notNull();
        transformerMapping.addStringProperty("parseExpression");
        transformerMapping.addStringProperty("parseType");

        // add 1:n relation for rule -> transformerMappings
        Property transformerMappingId = transformerMapping.addLongProperty("transformerMappingId").unique().notNull().getProperty();
        rule.addToMany(transformerMapping, transformerMappingId).setName("transformerMappings");
    }

    /**
     * Add gradeEntry entity.
     *
     * @param schema - database schema
     */
    private static void addGradeEntry(Schema schema) {
        gradeEntry = schema.addEntity("GradeEntry");
        gradeEntry.addIdProperty().primaryKey();
        gradeEntry.addStringProperty("name").notNull();
        gradeEntry.addDoubleProperty("grade");
        gradeEntry.addStringProperty("examId");
        gradeEntry.addStringProperty("semester").notNull();
        gradeEntry.addStringProperty("state");
    }

    /**
     * Add overview entity.
     *
     * @param schema - database schema
     */
    private static void addOverview(Schema schema) {
        overview = schema.addEntity("Overview");
        overview.addIdProperty().primaryKey();
        overview.addDoubleProperty("average");
        overview.addIntProperty("participants");
        overview.addIntProperty("section1");
        overview.addIntProperty("section2");
        overview.addIntProperty("section3");
        overview.addIntProperty("section4");
        overview.addIntProperty("section5");
        overview.addIntProperty("userSection");

        // add 1:1 relation for gradeEntry -> overview
        Property overviewId = gradeEntry.addLongProperty("overviewId").unique().getProperty();
        gradeEntry.addToOne(overview, overviewId);
    }
}
