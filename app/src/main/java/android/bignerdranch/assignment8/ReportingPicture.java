package android.bignerdranch.assignment8;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class ReportingPicture {
    public static Bitmap getScaledBitmap(String path2, int destWidth, int destHeight) {
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path2, options2);

        float srcWidth = options2.outWidth;
        float srcHeight = options2.outHeight;

        int inSampleSize = 1;
        if(srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
        }

        options2 = new BitmapFactory.Options();
        options2.inSampleSize = inSampleSize;

        return BitmapFactory.decodeFile(path2, options2);
    }

    public static Bitmap getScaledBitmap(String path2, Activity activity2) {
        Point size2 = new Point();
        activity2.getWindowManager().getDefaultDisplay().getSize(size2);

        return getScaledBitmap(path2, size2.x, size2.y);
    }
}
