package in.nishantarora.assignment4;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.util.Log;

import static android.content.ContentValues.TAG;


class DetectFace {
    private String uri;

    DetectFace(String uri) {
        this.uri = uri;
    }

    private static int roundEven(float f) {
        return Math.round(f / 2) * 2;
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
        int SCALE_FACTOR = 8;
        BitmapFactory.Options BMPFactorOpt = new BitmapFactory.Options();
        BMPFactorOpt.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap image = BitmapFactory.decodeFile(uri, BMPFactorOpt);
        int imageHeight = roundEven(image.getHeight()/SCALE_FACTOR);
        int imageWidth = roundEven(image.getWidth()/SCALE_FACTOR);
        int MAX_NUM_FACES = 1;
        Bitmap tmpBitmap = Bitmap.createScaledBitmap(image, imageWidth,
                imageHeight, true);
        FaceDetector.Face[] faces = new FaceDetector.Face[MAX_NUM_FACES];
        FaceDetector faceDetector = new FaceDetector(imageWidth, imageHeight,
                MAX_NUM_FACES);
        faceDetector.findFaces(tmpBitmap, faces);
        if (faces[0] != null) {
            PointF midPoint = new PointF();
            faces[0].getMidPoint(midPoint);
            float eyeDist = faces[0].eyesDistance();
            Log.v("x", String.valueOf(midPoint.x));
            Log.v("y", String.valueOf(midPoint.y));
            Log.v("e", String.valueOf(eyeDist));
            return new FaceData(
                    midPoint.x * SCALE_FACTOR,
                    midPoint.y * SCALE_FACTOR,
                    eyeDist * SCALE_FACTOR);
        }
        Log.v(TAG, "no faces found");
        return new FaceData(0, 0, 0);
    }
}
