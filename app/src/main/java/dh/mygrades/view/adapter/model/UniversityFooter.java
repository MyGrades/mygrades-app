package dh.mygrades.view.adapter.model;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * POJO for footer item in UniversitiesAdapter.
 */
public class UniversityFooter extends UniversityGroupItem {
    private boolean visible;

    public UniversityFooter() {
        visible = false;
    }

    @Override
    public long getGroupId() {
        return -1;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void goToSubmitWish(Context context) {
        String uri = "dh.mygrades.view.activity://goto.fragment/postwish";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        context.startActivity(intent);
    }
}
