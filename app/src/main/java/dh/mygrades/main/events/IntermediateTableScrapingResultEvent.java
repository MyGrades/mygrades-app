package dh.mygrades.main.events;

/**
 * IntermediateTableScrapingResultEvent is used to send
 * intermediate result of scraping (table of grades as string) to subscribers.
 */
public class IntermediateTableScrapingResultEvent {
    private String parsedTable;
    private String gradeHash;

    public IntermediateTableScrapingResultEvent(String parsedTable, String gradeHash) {
        this.parsedTable = parsedTable;
        this.gradeHash = gradeHash;
    }

    public String getParsedTable() {
        return parsedTable;
    }

    public void setParsedTable(String parsedTable) {
        this.parsedTable = parsedTable;
    }

    public String getGradeHash() {
        return gradeHash;
    }

    public void setGradeHash(String gradeHash) {
        this.gradeHash = gradeHash;
    }
}
