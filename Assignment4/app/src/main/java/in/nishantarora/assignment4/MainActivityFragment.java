package in.nishantarora.assignment4;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import io.realm.Realm;
import io.realm.RealmResults;

import static android.content.ContentValues.TAG;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    // I believe this is is an easy way to manage permissions.
    private static final String[] GALLERY_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // Will use these to check which permissions where granted and what the
    // wanna do next.
    private static final int GALLERY_PERMISSION_RESPONSE_CODE = 101;
    private GridView gallery;
    private ImageAdapter imageAdapter;
    private View view;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initializing Realm
        Realm.init(getActivity());
    }

    /**
     * Checks if we have all the permissions required, else will request them.
     *
     * @param permissions  array of the required permissions.
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[]
            permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
        // Mapping permissions
        switch (requestCode) {
            case GALLERY_PERMISSION_RESPONSE_CODE: {
                     // manageGallery();
            }
        }
    }

    /**
     * Filling the current view with gridview.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void manageGallery() {
        if (hasPermissions(GALLERY_PERMISSIONS,
                GALLERY_PERMISSION_RESPONSE_CODE)) {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<ImageWithFace> results = realm.where(ImageWithFace.class).findAll();
            if (results.size() == 0) {
                getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ImportFragment()).addToBackStack(null).commit();
            } else {
                gallery = (GridView) view.findViewById(R.id.galleryGridView);
                imageAdapter = new ImageAdapter(getActivity());
                gallery.setAdapter(imageAdapter);
                gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int
                            position, long id) {
                        ImageWithFace image = imageAdapter.getItem(position);
                        Fragment fragment = ImageFragment.newInstance(image.getId());
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                fragment).addToBackStack(null).commit();
                    }
                });
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().setTitle(R.string.app_name);
        manageGallery();
        return view;
    }
}