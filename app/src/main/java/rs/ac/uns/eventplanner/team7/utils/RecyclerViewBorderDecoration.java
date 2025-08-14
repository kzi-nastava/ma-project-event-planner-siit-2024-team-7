package rs.ac.uns.eventplanner.team7.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewBorderDecoration extends RecyclerView.ItemDecoration {
    private final Paint paint;
    private final int borderWidth;

    public RecyclerViewBorderDecoration(Context context, int color, int borderWidth) {
        this.borderWidth = borderWidth;
        paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(borderWidth);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            Rect rect = new Rect(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            canvas.drawRect(rect, paint);
        }
    }
}
