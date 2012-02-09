package se.serp.LiULunch;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

// workaround for buggy ViewFlipper. see
// http://stackoverflow.com/questions/4674796/crash-when-rotating-activity-using-viewflipper

public class SafeViewFlipper extends ViewFlipper {

	public SafeViewFlipper(Context context) {
		super(context);
	}
	
	public SafeViewFlipper(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	@Override
    public void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        }
        catch (IllegalArgumentException e) {
            // This happens when you're rotating and opening the keyboard that the same time
            // Possibly other rotation related scenarios as well
            stopFlipping();
        }
    }

}
