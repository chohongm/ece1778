package in.nishantarora.assignment3;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Locale;

/**
 * This handles the image viewer.
 */
public class ImageFragment extends Fragment {

    // Just a key.
    public static final String IMAGE_ID = "image_id";
    // We run all contentResolver queries at this location.
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;
    // The image we're viewing.
    private ImageAdapter.ImageData image;
    // Firebase storage reference.
    private StorageReference mStorageRef = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://assignment3-fa123.appspot.com");
    // Firebase database. It does not work the way I wanted it to work, but ok.
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://assignment3-fa123.firebaseio.com/");
    // Just a flag to keep a check if this image is backed up online.
    private boolean exists_online = false;
    private StorageReference fileRef;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * This initializes the ImageFragment object.
     * @param imageData containing all relevant image info.
     * @return fragment object.
     */
    public static ImageFragment newInstance(ImageAdapter.ImageData imageData) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        // This is the reason we needed ImageData to implement a serializable
        // object.
        args.putSerializable(IMAGE_ID, imageData);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Raising Toasts. Always a long toast.
     * @param msg to be raised.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void RaiseToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    /**
     * Deletes the local file for the image.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void deleteLocal() {
        // Finding the image.
        String where = MediaStore.Images.Media._ID + " =?";
        String[] args = {String.valueOf(image.id)};
        // I love the way contentresolver works, it marks the file as deleted
        // and telecasts it everywhere. I think it deletes them later.
        int delete = getActivity().getContentResolver().delete(uri, where,
                args);
        // So we deleted file, maybe even two files, but ideally only one.
        // Let's not bother and let the user handle this
        if (delete > 0) {
            RaiseToast(String.format(Locale.US, "Success: Deleted %d " +
                            "Local files",
                    delete));
            // The file is deleted, take the user back to previous fragment.
            onBackPressed();
        } else {
            RaiseToast("Failed to delete any files. Try Again");
        }
    }

    /**
     * Deletes the file from local as well as online backup.
     */
    public void deleteAll() {
        // Finding file online.
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(Void aVoid) {
                RaiseToast("Success: Deleted Online Backup.");
                mDatabase.child("images").child(String.valueOf(image.id))
                        .setValue(null);
                // Now delete local.
                deleteLocal();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onFailure(@NonNull Exception e) {
                RaiseToast("Failed: Unable To Delete Online Backup. Try Again");
            }
        });
    }

    /**
     * Method of the Fragment class.
     * @param savedInstanceState just resuming info.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            image = (ImageAdapter.ImageData) getArguments().getSerializable
                    (IMAGE_ID);
        }
    }

    /**
     * This is how we can jump to the previous fragment provided we
     * maintained a stack when we first came here.
     */
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Method of the Fragment class. Generates view.
     * @param inflater to inflate the layout
     * @param container the parent, in this case the layout in main_activity
     * @param savedInstanceState resuming info
     * @return View.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Updating the toolbar.
        getActivity().setTitle(image.title);

        // Checking if image exists online. Now this is dicey, since I am not
        // maintaining any info offline if the image exists online or not. We
        // need to check every time the image is loaded in the view if this
        // exists online. So on poor internet connections this will be
        // reported as not existing.
        Uri file = Uri.fromFile(new File(image.uri));
        fileRef = mStorageRef.child("assignment3/" + file.getLastPathSegment());
        // If we can get the url, that means we have this online.
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                exists_online = true;
            }
        });

        // Inflate the layout for this fragment. This is self explanatory.
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        TextView latLong = (TextView) view.findViewById(R.id.lat_long);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        latLong.setText(String.format(Locale.US, "Lat: %f Long: %f",
                image.lat, image.lng));
        // Loading image
        Picasso.with(getContext())
                .load("file:///" + image.uri)
                .placeholder(R.drawable.ic_camera_alt_black_250dp)
                .fit()
                .centerInside()
                .into(imageView);
        // Delete button
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // We'll show an alert dialog on what the user wants to do.
                AlertDialog.Builder builder = new AlertDialog.Builder
                        (getContext());
                String message = "Select if you want to delete just the " +
                        "local or the online backups too.";
                if (!exists_online) {
                    message = "I did not find any online backups for this.";
                }
                builder.setTitle(R.string.dialog_title).setMessage(message);
                if (exists_online) {
                    builder.setPositiveButton(R.string.delete_all, new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    deleteAll();
                                }
                            });
                }
                builder.setNeutralButton(R.string.delete_local, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteLocal();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });
        return view;
    }
}
