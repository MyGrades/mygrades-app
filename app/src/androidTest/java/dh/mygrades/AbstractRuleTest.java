package dh.mygrades;

import android.test.ApplicationTestCase;

import java.util.List;

import dh.mygrades.database.dao.Action;
import dh.mygrades.database.dao.ActionDao;
import dh.mygrades.database.dao.DaoSession;
import dh.mygrades.database.dao.GradeEntry;
import dh.mygrades.database.dao.GradeEntryDao;
import dh.mygrades.database.dao.Overview;
import dh.mygrades.database.dao.University;
import dh.mygrades.database.dao.UniversityDao;
import dh.mygrades.main.processor.GradesProcessor;
import dh.mygrades.main.processor.LoginProcessor;
import dh.mygrades.main.processor.UniversityProcessor;
import dh.mygrades.util.AverageCalculator;
import dh.mygrades.util.Config;

/**
 * Base class for application tests to test the rules against html fragments.
 * Furthermore it tests the correct functionality of the scraper and parser.
 */
public abstract class AbstractRuleTest extends ApplicationTestCase<MyGradesApplication> {

    protected GradesProcessor gradesProcessor;
    protected LoginProcessor loginProcessor;
    protected UniversityProcessor universityProcessor;
    protected DaoSession daoSession;
    protected AverageCalculator averageCalculator;

    protected List<University> universities;
    protected List<GradeEntry> gradeEntries;
    protected Overview overview;

    public AbstractRuleTest() {
        super(MyGradesApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        createApplication();
        assertNotNull(getContext());

        daoSession = ((MyGradesApplication) getContext().getApplicationContext()).getDaoSession();
        assertNotNull(daoSession);
        daoSession.clear();
        daoSession.getGradeEntryDao().deleteAll();

        universityProcessor = new UniversityProcessor(getContext());
        assertNotNull(universityProcessor);

        loginProcessor = new LoginProcessor(getContext());
        assertNotNull(loginProcessor);

        gradesProcessor = new GradesProcessor(getContext());
        assertNotNull(gradesProcessor);

        universityProcessor.getUniversities(true);
        getUniversitiesFromDatabase();

        super.setUp(); // should be on top actually, but then the test is flaky :/
    }

    @Override
    protected void tearDown() throws Exception {
        loginProcessor.logout();
        loginProcessor = null;
        universities = null;
        gradesProcessor = null;
        loginProcessor = null;
        daoSession = null;
        averageCalculator = null;
        gradeEntries = null;
        overview = null;
        super.tearDown();
    }

    private void getUniversitiesFromDatabase() {
        UniversityDao universityDao = daoSession.getUniversityDao();
        universities = universityDao.queryBuilder()
                .where(UniversityDao.Properties.Published.eq(true))
                .list();
    }

    private University getUniversityByName(String name) {
        for (University u : universities) {
            if (u.getName().toLowerCase().contains(name.toLowerCase())) {
                return u;
            }
        }
        return null;
    }

    protected void scrape(String universityName, String url, int ruleIndex) {
        scrape(universityName, url, "", "", ruleIndex);
    }

    protected void scrape(String universityName, String url, String username, String password, int ruleIndex) {
        University university = getUniversityByName(universityName);
        assertNotNull(university);
        long universityId = university.getUniversityId();
        long ruleId = university.getRules().get(ruleIndex).getRuleId(); // TODO: sort rules or get rule by name
        universityProcessor.getDetailedUniversity(universityId);
        overwriteUrl(ruleId, url);
        loginProcessor.loginAndScrapeForGrades(username, password, universityId, ruleId);
        gradeEntries = daoSession.getGradeEntryDao().loadAll();
        averageCalculator = new AverageCalculator(false);
        averageCalculator.calculateFromGradeEntries(gradeEntries);
    }

    protected void scrapeForOverview(String examId) {
        GradeEntry gradeEntry = daoSession.getGradeEntryDao().queryBuilder()
                .where(GradeEntryDao.Properties.ExamId.eq(examId)).build().unique();
        assertNotNull(gradeEntry);
        String gradeHash = gradeEntry.getHash();
        gradesProcessor.scrapeForOverview(gradeHash);
        gradeEntry.refresh();
        overview = gradeEntry.getOverview();
        assertNotNull(overview);
    }

    private void overwriteUrl(long ruleId, String url) {
        Action action = daoSession.getActionDao().queryBuilder()
                .where(ActionDao.Properties.RuleId.eq(ruleId))
                .where(ActionDao.Properties.Position.eq(0))
                .build().unique();
        assertNotNull(action);
        action.setUrl(Config.getTestServerUrl() + url);
        action.update();
    }
}
