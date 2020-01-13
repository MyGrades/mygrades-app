package dh.mygrades.view.adapter.model;

/**
 * GradesFooterItem to show the footer.
 */
public class GradesFooterItem implements GradesAdapterItem {
    private boolean visible;

    public GradesFooterItem(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
