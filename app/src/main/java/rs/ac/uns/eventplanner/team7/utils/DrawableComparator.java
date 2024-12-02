package rs.ac.uns.eventplanner.team7.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DrawableComparator {
    public static boolean areDrawablesIdentical(Drawable drawable1, Drawable drawable2) {
        if (drawable1 == null || drawable2 == null) return false;

        // Convert Drawables to Bitmaps
        Bitmap bitmap1 = drawableToBitmap(drawable1);
        Bitmap bitmap2 = drawableToBitmap(drawable2);

        return bitmap1.sameAs(bitmap2); // Compare the bitmaps
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
