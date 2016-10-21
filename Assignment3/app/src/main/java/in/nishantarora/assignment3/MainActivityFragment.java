package in.nishantarora.assignment3;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;

import static android.content.ContentValues.TAG;

/**
 * This is the Gallery of all images.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivityFragment extends Fragment {

    // I believe this is is an easy way to manage permissions.
    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final String[] GALLERY_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Will use these to check which permissions where granted and what the
    // wanna do next.
    private static final int GALLERY_PERMISSION_RESPONSE_CODE = 101;
    private static final int CAMERA_PERMISSION_RESPONSE_CODE = 102;
    // Some other requests.
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Firebase stuff.
    private StorageReference mStorageRef = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://assignment3-fa123.appspot.com");
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://assignment3-fa123.firebaseio.com/");
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;
    private LocationManager lm;
    private GridView gallery;
    private ImageAdapter imageAdapter;
    private View view;

    /**
     * Constructor.
     */
    public MainActivityFragment() {
    }

    /**
     * Checks if we have all the permissions required, else will request them.
     * @param permissions array of the required permissions.
     * @param responseCode just to know which permissions were granted.
     * @return boolean
     */
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
     * I wonder if we could have a better way. Updates the ImageAdapter needs
     * to refresh maybe because we added or deleted an image and we can now
     * update the gridview.
     */
    private void refreshImageAdapter() {
        imageAdapter = new ImageAdapter(getActivity());
        gallery.setAdapter(imageAdapter);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int
                    position, long id) {
                ImageAdapter.ImageData image = imageAdapter.getItem(position);
                Fragment fragment = ImageFragment.newInstance(image);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    /**
     * Android locations are painful. The camera is unreliable in GeoTagging
     * images. Even if we have permissions to access the location the Camera
     * will not always tag the image. The probable cause is it looks for the
     * exact location from gps. But that means if the user clicks a photo and
     * closes it, the camera does not get a fix. So I intend to get a coarse
     * location and Geotag the image myself.
     * @return location object
     */
    private Location getLocation() {
        // Let's make it simpler to get location. We do not need precision.
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        // The best provider is the one available, we are not willing to wait
        // for the gps hardware to come online.
        return lm.getLastKnownLocation(lm.getBestProvider(criteria, true));
    }

    /**
     * As I said before, contentresolver is amazing. You can do stuff with
     * the files as if they were DB and it will make this info available for
     * everyone. It can always change the files later.
     * @param image the image we are geotagging,
     * @return geotagged image.
     */
    private ImageAdapter.ImageData geoTagImage(ImageAdapter.ImageData image) {
        // I guess the camera fucked up, it's not reliable.
        if (image.lat == 0 && image.lng == 0) {
            Location location = getLocation();

            // Straight away update the image object.
            image.lng = (float) location.getLongitude();
            image.lat = (float) location.getLatitude();

            // Updating the image on device.
            String where = MediaStore.Images.Media._ID + " =?";
            String[] args = {String.valueOf(image.id)};
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.LATITUDE, image.lat);
            values.put(MediaStore.Images.Media.LONGITUDE, image.lng);
            getActivity().getContentResolver().update(uri, values, where,
                    args);
        }
        return image;
    }

    /**
     * This raises the camera intent for clicking pictures.
     */
    private void takePicture() {
        if (hasPermissions(CAMERA_PERMISSIONS,
                CAMERA_PERMISSION_RESPONSE_CODE)) {
            Intent take_picture = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);
            if (take_picture.resolveActivity(getActivity()
                .getPackageManager()) != null) {
                startActivityForResult(take_picture, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Filling the current view with gridview.
     */
    private void createGallery() {
        if (hasPermissions(GALLERY_PERMISSIONS,
                GALLERY_PERMISSION_RESPONSE_CODE)) {
            gallery = (GridView) view.findViewById(R.id.galleryGridView);
            refreshImageAdapter();
        }
    }

    /**
     * Method of the Fragment class. It is run when we know a activity happened.
     * @param requestCode Remember we set these up earlier.
     * @param resultCode This is a android thingy
     * @param data What did we get out of it
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // So when Image Capture intent was completed
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
                resultCode == Activity.RESULT_OK) {
            // This will load the image in the gridview.
            refreshImageAdapter();
            // Let's geotag if not tagged.
            final ImageAdapter.ImageData image = geoTagImage(imageAdapter
                    .getItem(0));
            // Give info to the user as to where this is stored.
            Toast.makeText(getContext(), "Stored At: " + image.uri,
                    Toast.LENGTH_LONG).show();
            Uri file = Uri.fromFile(new File(image.uri));

            // Let's push this image to Firebase.
            StorageReference fileRef = mStorageRef.child("assignment3/" +
                    file.getLastPathSegment());
            fileRef.putFile(file).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.v(String.valueOf(image.id), image.title);
                    // Adding this to a database. I wonder if this makes
                    // things simpler.
                    mDatabase.child("images").child(String.valueOf(image.id))
                            .setValue(image);
                    Toast.makeText(getContext(), "Uploaded To: " +
                                    taskSnapshot.getDownloadUrl(),
                            Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Upload Failed!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        // Mapping permissions
        switch (requestCode) {
            case CAMERA_PERMISSION_RESPONSE_CODE: {
                // So we were trying to take pictures, but did not have
                // camera permissions. We asked the user to grant us camera
                // permissions. Now we have the camera permissions. Let's
                // continue taking pictures.
                takePicture();
            }
            case GALLERY_PERMISSION_RESPONSE_CODE: {
                createGallery();
            }
        }
        refreshImageAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        // Updating the toolbar
        getActivity().setTitle("Assignment3");

        // Setting up location services
        lm = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        // Taking Pictures
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(
                R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        // Generating Gallery.
        createGallery();
        return view;
    }
}
