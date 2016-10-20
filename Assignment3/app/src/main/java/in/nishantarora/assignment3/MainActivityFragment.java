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
 * A placeholder fragment containing a simple view.
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivityFragment extends Fragment {

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final String[] GALLERY_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int GALLERY_PERMISSION_RESPONSE_CODE = 101;
    private static final int CAMERA_PERMISSION_RESPONSE_CODE = 102;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private StorageReference mStorageRef = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://assignment3-fa123.appspot.com");
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;
    private LocationManager lm;
    private GridView gallery;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://assignment3-fa123.firebaseio.com/");
    private ImageAdapter imageAdapter;
    private View view;

    public MainActivityFragment() {
    }

    private boolean hasPermissions(String[] permissions, int responseCode) {
        boolean hasPermissions = true;
        for (String permission : permissions) {
            if (getContext().checkSelfPermission(permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                hasPermissions = false;
            }
        }
        if (!hasPermissions) {
            Log.v(TAG, "Requesting permissions.");
            requestPermissions(permissions, responseCode);
        }
        return hasPermissions;
    }

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

    private Location getLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return lm.getLastKnownLocation(lm.getBestProvider(criteria, true));
    }

    private ImageAdapter.ImageData geoTagImage(ImageAdapter.ImageData image) {
        if (image.lat == 0 && image.lng == 0) {
            // I guess the camera fucked up, it's not reliable.

            Location location = getLocation();
            image.lng = (float) location.getLongitude();
            image.lat = (float) location.getLatitude();
            Log.v(Float.toString(image.lat), Float.toString(image.lng));
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

    private void takePicture() {
        Intent take_picture = new Intent(MediaStore
                .ACTION_IMAGE_CAPTURE);
        if (take_picture.resolveActivity(getActivity()
                .getPackageManager()) != null) {
            startActivityForResult(take_picture, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void createGallery() {
        gallery = (GridView) view.findViewById(R.id.galleryGridView);
        refreshImageAdapter();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode ==
                Activity.RESULT_OK) {
            refreshImageAdapter();
            final ImageAdapter.ImageData image = geoTagImage(imageAdapter
                    .getItem(0));
            Toast.makeText(getContext(), "Stored At: " +
                    image.uri, Toast
                    .LENGTH_LONG)
                    .show();
            Uri file = Uri.fromFile(new File(image.uri));
            StorageReference fileRef = mStorageRef.child("assignment3/" +
                    file.getLastPathSegment());
            fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.v(String.valueOf(image.id), image.title);

                    //Firebase
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_RESPONSE_CODE: {
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
                if (hasPermissions(CAMERA_PERMISSIONS,
                        CAMERA_PERMISSION_RESPONSE_CODE)) {
                    takePicture();
                }
            }
        });

        // Showing Gallery
        if (hasPermissions(GALLERY_PERMISSIONS,
                GALLERY_PERMISSION_RESPONSE_CODE)) {
            createGallery();
        }
        return view;
    }
}
