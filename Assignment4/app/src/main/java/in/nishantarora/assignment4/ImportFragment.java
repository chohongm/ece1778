package in.nishantarora.assignment4;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;

import io.realm.Realm;

import static android.content.ContentValues.TAG;


public class ImportFragment extends Fragment {
    // I believe this is is an easy way to manage permissions.
    private static final String[] GALLERY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Will use these to check which permissions where granted and what the
    // wanna do next.
    private static final int GALLERY_PERMISSION_RESPONSE_CODE = 101;
    private ArrayList<ImageData> images;
    private int selectedThreads;
    private int imagesDone = 0;
    private int imagesTotal = 0;
    private int imagePointer = 0;
    private int liveThreads = 0;
    private int facesFound = 0;

    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;

    ProgressBar progressBar;
    TextView progressText;
    TextView threadText;
    Spinner spinner;
    ImageButton importButton;

    private class FaceRecognitionTask extends AsyncTask<String, Integer,
            Boolean> {
        private boolean faceFound = false;
        @Override
        protected Boolean doInBackground(final String... params) {
            Realm faceData = Realm.getDefaultInstance();
            faceData.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm faceData) {
                    try {
                        DetectFace.FaceData face = new DetectFace(params[2])
                                .getFace();
                        ImageWithFace img = faceData.createObject(ImageWithFace
                                .class, params[0]);
                        img.setTitle(params[1]);
                        img.setUri(params[2]);
                        Log.v(TAG, face.x + " " + face.y + " " + face.e);
                        img.setx(face.x);
                        img.sety(face.y);
                        img.sete(face.e);
                        if (face.x > 0 && face.y > 0) {
                            faceFound = true;
                        }
                    } catch (Exception e) {
                        Log.v(TAG, "Value Already Exists.");
                    }
                }
            });
            faceData.close();
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            imagesDone += 1;
            if (faceFound) {
                facesFound += 1;
            }
            liveThreads--;
            updateProgress();
        }
    }

    private void updateProgress() {
        // Hiding the Import button.
        if (importButton.getVisibility() == View.VISIBLE) {
            importButton.setVisibility(View.INVISIBLE);
        }
        // Hiding the thread selector.
        if (spinner.getVisibility() == View.VISIBLE) {
            spinner.setVisibility(View.INVISIBLE);
        }
        // Showing the progress bar.
        if (progressBar.getVisibility() == View.INVISIBLE) {
            progressBar.setVisibility(View.VISIBLE);
        }
        // Showing the progress text.
        if (progressText.getVisibility() == View.INVISIBLE) {
            progressText.setVisibility(View.VISIBLE);
        }
        // Showing thread text.
        if (threadText.getVisibility() == View.INVISIBLE) {
            threadText.setVisibility(View.VISIBLE);
        }
        // Updating progress
        progressBar.setProgress((imagesDone*100)/imagesTotal);
        progressText.setText("Images: [" + imagesDone + "/" + imagesTotal +
                "] Faces Found: " + facesFound);
        threadText.setText("Running " + liveThreads + " Threads");
        // When complete
        if (imagesDone == imagesTotal){
            progressText.setText("All Done");
            onBackPressed();
        }
        if (liveThreads == 0) {
            threadText.setVisibility(View.INVISIBLE);
        }
        threadStarter();
    }

    private void threadStarter() {
        if (imagePointer < imagesTotal && liveThreads < selectedThreads) {
            ImageData image = images.get(imagePointer);
            Log.v(TAG, image.uri);
            new FaceRecognitionTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    String.valueOf(image.id), image.title, image.uri);
            imagePointer++;
            liveThreads++;
            updateProgress();
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

    /**
     * Checks if we have all the permissions required, else will request them.
     * @param permissions array of the required permissions.
     * @param responseCode just to know which permissions were granted.
     * @return boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermissions(String[] permissions, int responseCode) {
        // Let's say we have permissions
        boolean hasPermissions = true;
        // Iterating over the required permissions.
        for (String permission : permissions) {
            if (getContext().checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
                break;
            }
        }
        if (!hasPermissions) {
            Log.v(TAG, "Requesting permissions.");
            requestPermissions(permissions, responseCode);
        }
        return hasPermissions;
    }

    /**
     * Fragment class method. Some permissions were granted. We'll check
     * which permissions were granted. Using this info, we can continue the
     * method we paused because we did not have permissions.
     * @param requestCode Remember we set this earlier. This tells us
     *                    permissions were granted for which action.
     * @param permissions what permissions were granted
     * @param grantResults were they actually granted?
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        // Mapping permissions
        switch (requestCode) {
            case GALLERY_PERMISSION_RESPONSE_CODE: {
                Log.v(TAG, "Gallery Permissions Granted");
                processImport();
            }
        }
    }

    /**
     * This is how I like to store the image data. Need to make it
     * serializable so that I can pass this between fragments.
     */
    private class ImageData implements Serializable {
        long id;
        String uri, title;

        ImageData(long id, String uri, String title) {
            this.id = id;
            this.uri = uri;
            this.title = title;
        }
    }

    /**
     * Get's all images on the device in the give directory.
     * @param context for getting images.
     * @return ArrayList
     */
    private ArrayList<ImageData> getAllDCIMImages(Activity context) {
        ArrayList<ImageData> allImages = new ArrayList<>();

        // The data we need to query from the storage.
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE
        };

        // Sorting the data.
        String sort = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        // We need only the data in DCIM folder for now.
        String where = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] where_args = new String[]{
                "%" + Environment.DIRECTORY_DCIM + "%"};

        // Running the query.
        Cursor cursor = context.getContentResolver().query(uri, projection,
                where, where_args, sort);

        // Some vars.
        int img_id_col_id = 0;
        int img_uri_col_id = 0;
        int img_title_col_id = 0;

        // Mapping cursor cols to vars defined earlier.
        if (cursor != null) {
            img_id_col_id = cursor.getColumnIndexOrThrow(MediaStore.Images
                    .Media._ID);
            img_uri_col_id = cursor.getColumnIndexOrThrow(MediaStore.Images
                    .Media.DATA);
            img_title_col_id = cursor.getColumnIndexOrThrow(MediaStore
                    .Images.Media.TITLE);
        }
        // Iterating over the cursor to build are image array.
        while (cursor != null && cursor.moveToNext()) {
            ImageData img = new ImageData(
                    cursor.getLong(img_id_col_id),
                    cursor.getString(img_uri_col_id),
                    cursor.getString(img_title_col_id)
            );
            allImages.add(img);
        }
        // We're done, close this to free up resources.
        if (cursor != null) {
            cursor.close();
        }
        return allImages;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void processImport() {
        if(hasPermissions(GALLERY_PERMISSIONS,
                GALLERY_PERMISSION_RESPONSE_CODE)) {
            images = getAllDCIMImages(getActivity());
            imagesTotal = images.size();
            selectedThreads = (int) Math.pow(2, spinner.getSelectedItemPosition());
            Log.v("threads", String.valueOf(selectedThreads));
            Log.v("images", String.valueOf(images.size()));
            threadStarter();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_import, container, false);

        // Updating the toolbar
        getActivity().setTitle(R.string.action_import);

        // Spinner Dropdown.
        spinner = (Spinner) view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource
                (getContext(), R.array.thread_options, android.R.layout
                        .simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Import Button
        importButton = (ImageButton) view.findViewById(R.id
                .import_button);

        // Progress Place holders.
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        threadText = (TextView) view.findViewById(R.id.thread_text);
        progressText = (TextView) view.findViewById(R.id.progress_text);

        // On click.
        importButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                    processImport();
            }
        });

        return view;
    }
}
