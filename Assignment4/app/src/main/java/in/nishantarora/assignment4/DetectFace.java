package in.nishantarora.assignment4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by whizzzkid on 3/11/16.
 */

class DetectFace {
    private String uri;

    DetectFace(String uri) {
        this.uri = uri;
    }

    class FaceData {
        float x, y, e;
        FaceData(float x, float y, float e) {
            this.x = x;
            this.y = y;
            this.e = e;
        }
    }

    FaceData getFace() {
        BitmapFactory.Options BMPFactorOpt = new BitmapFactory.Options();
        BMPFactorOpt.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap tmpBitmap = BitmapFactory.decodeFile(uri, BMPFactorOpt);
        int imageHeight = tmpBitmap.getHeight();
        int imageWidth = tmpBitmap.getWidth();
        int maxNumFaces = 1;
        FaceDetector.Face[] faces = new FaceDetector.Face[maxNumFaces];
        FaceDetector faceDetector = new FaceDetector(imageWidth, imageHeight, maxNumFaces);
        faceDetector.findFaces(tmpBitmap, faces);
        if (faces[0] != null) {
            PointF midPoint = new PointF();
            faces[0].getMidPoint(midPoint);
            float eyeDist = faces[0].eyesDistance();
            Log.v("x", String.valueOf(midPoint.x));
            Log.v("y", String.valueOf(midPoint.y));
            Log.v("e", String.valueOf(eyeDist));
            return new FaceData(midPoint.x, midPoint.y, eyeDist);
        } else {
            Log.v(TAG, "no faces found");
            return new FaceData(0, 0, 0);
        }
    }
}
