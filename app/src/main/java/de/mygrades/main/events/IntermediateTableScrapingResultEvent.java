package de.mygrades.main.events;

/**
 * IntermediateTableScrapingResultEvent is used to send
 * intermediate result of scraping (table of grades as string) to subscribers.
 */
public class IntermediateTableScrapingResultEvent {
    private String parsedTable;

    public IntermediateTableScrapingResultEvent(String parsedTable) {
        this.parsedTable = parsedTable;
    }

    public String getParsedTable() {
        return parsedTable;
    }

    public void setParsedTable(String parsedTable) {
        this.parsedTable = parsedTable;
    }
}
