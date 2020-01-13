package dh.mygrades;

/**
 * Application test to test the rules against html fragments.
 * Furthermore it tests the correct functionality of the scraper and parser.
 */
public class RuleTest extends AbstractRuleTest {

    /**
     * Test first rule for FernUni in Hagen.
     */
    public void testFernuniHagenFirstRule() {
        scrape("FernUniversität in Hagen", "fernunihagen/1/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test second rule for FernUni in Hagen.
     */
    public void testFernuniHagenSecondRule() {
        scrape("FernUniversität in Hagen", "fernunihagen/2/1.html", 1);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test rule for Fachhochschule Aachen.
     */
    public void testFhAachen() {
        scrape("Fachhochschule Aachen", "fhaachen/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test rule for Hochschule Mainz with super broken markup.
     */
    public void testHsMainz1() {
        scrape("Hochschule Mainz", "hsmainz/1/1.html", 0);
        assertEquals(5, gradeEntries.size());
    }

    /**
     * Test rule for Hochschule Mainz.
     */
    public void testHsMainz2() {
        scrape("Hochschule Mainz", "hsmainz/2/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test rule for Hochschule RheinMain.
     */
    public void testHsrm() {
        scrape("Hochschule RheinMain", "hsrm/1.html", 0);
        assertEquals(44, gradeEntries.size());
        assertEquals(1.64f, averageCalculator.getAverage(), 0.01f);
        assertEquals(165.0, averageCalculator.getCreditPointsSum(), 0.00001d);

        scrapeForOverview("1111");
        assertEquals(44, (int) overview.getParticipants());
        assertEquals(3.0d, overview.getAverage());
    }

    /**
     * Test rule for TU Clausthal.
     */
    public void testTuClausthal() {
        scrape("Clausthal", "tuclausthal/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test rule for Uni Bonn.
     */
    public void testUniBonn() {
        scrape("Universität Bonn", "unibonn/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }

    /**
     * Test rule for Goethe-Uni Frankfurt.
     */
    public void testUniFrankfurt() {
        scrape("Goethe-Universität", "unifrankfurt/1.html", 0);
        assertTrue(gradeEntries.size() > 0);
    }
}
