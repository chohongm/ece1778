package in.nishantarora.assignment4;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import io.realm.Realm;

import static android.content.ContentValues.TAG;

public class ImageFragment extends Fragment {
    // Just a key.
    public static final String IMAGE_ID = "image_id";
    // The image we're viewing.
    private String imageId;
    private ImageWithFace image;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment using
     * the provided parameters.
     *
     * @param imageId Parameter 1.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String imageId) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_ID, imageId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageId = (String) getArguments().getSerializable
                    (IMAGE_ID);
        }
    }

    /**
     * This is how we can jump to the previous fragment provided we
     * maintained a stack when we first came here.
     */
    public void onBackPressed() {
        try {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            }
        } catch (Exception e) {
            Log.v(TAG, "could not go back.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        // Getting Image
        final Realm realm = Realm.getDefaultInstance();
        final ImageWithFace image = realm.where(ImageWithFace
                .class).equalTo("id", imageId).findFirst();

        // Setting Title.
        getActivity().setTitle(image.getTitle());

        Button delete = (Button) view.findViewById(R.id.delete_button);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        image.deleteFromRealm();
                    }
                });
                onBackPressed();
            }
        });

        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);

        // Marking Face
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important

        // Creating Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(image.getUri(), myOptions);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);
        if (image.getx() > 0 && image.gety() > 0) {
            canvas.drawCircle(image.getx(), image.gety(), image.gete() * 2,
                    paint);
        }

        imageView.setAdjustViewBounds(true);
        imageView.setImageBitmap(mutableBitmap);

        return view;
    }
}
