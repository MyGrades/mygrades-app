package de.mygrades.database;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

/**
 * Generator to create DAOs.
 */
public class MyGradesDaoGenerator {

    private static final int VERSION = 3;
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
        addTransformerMapping(schema);
        addGradeEntry(schema);
        addOverview(schema);

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
        university.addLongProperty("universityId").primaryKey();
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
        rule.addLongProperty("ruleId").primaryKey();
        rule.addStringProperty("name").notNull();
        rule.addStringProperty("semesterFormat");
        rule.addStringProperty("semesterPattern");
        rule.addIntProperty("semesterStartSummer");
        rule.addIntProperty("semesterStartWinter");
        rule.addDoubleProperty("gradeFactor");
        rule.addDateProperty("lastUpdated");
        rule.addBooleanProperty("overview");

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
        action.addLongProperty("actionId").primaryKey();
        action.addIntProperty("position").notNull();
        action.addStringProperty("type");
        action.addStringProperty("method").notNull();
        action.addStringProperty("url");
        action.addStringProperty("parseExpression");

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
        actionParam.addLongProperty("actionParamId").primaryKey();
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
        transformerMapping.addLongProperty("transformerMappingId").primaryKey();
        transformerMapping.addStringProperty("name").notNull();
        transformerMapping.addStringProperty("parseExpression");

        // add 1:n relation for rule -> actions
        Property ruleId = transformerMapping.addLongProperty("ruleId").notNull().getProperty();
        rule.addToMany(transformerMapping, ruleId, "transformerMappings");
    }

    /**
     * Add gradeEntry entity.
     *
     * @param schema - database schema
     */
    private static void addGradeEntry(Schema schema) {
        gradeEntry = schema.addEntity("GradeEntry");
        gradeEntry.addStringProperty("name").notNull();
        gradeEntry.addDoubleProperty("grade");
        gradeEntry.addStringProperty("examId");
        gradeEntry.addStringProperty("semester");
        gradeEntry.addStringProperty("state");
        gradeEntry.addDoubleProperty("creditPoints");
        gradeEntry.addStringProperty("annotation");
        gradeEntry.addStringProperty("attempt");
        gradeEntry.addStringProperty("examDate");
        gradeEntry.addIntProperty("semesterNumber");
        gradeEntry.addStringProperty("tester");
        gradeEntry.addStringProperty("hash").primaryKey();
        gradeEntry.addBooleanProperty("overviewPossible");
        gradeEntry.addIntProperty("seen");
        gradeEntry.addBooleanProperty("overviewFailedOnFirstTry");

        gradeEntry.setHasKeepSections(true);
    }

    /**
     * Add overview entity.
     *
     * @param schema - database schema
     */
    private static void addOverview(Schema schema) {
        overview = schema.addEntity("Overview");
        overview.addLongProperty("overviewId").primaryKey().autoincrement();
        overview.addDoubleProperty("average");
        overview.addIntProperty("participants");
        overview.addIntProperty("section1");
        overview.addIntProperty("section2");
        overview.addIntProperty("section3");
        overview.addIntProperty("section4");
        overview.addIntProperty("section5");
        overview.addIntProperty("userSection");
        overview.addStringProperty("gradeEntryHash");
        overview.setHasKeepSections(true);

        // add 1:1 relation for gradeEntry -> overview
        Property overviewId = gradeEntry.addLongProperty("overviewId").getProperty();
        gradeEntry.addToOne(overview, overviewId);
    }
}
