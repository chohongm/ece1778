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
import android.util.Log;
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

public class ImageFragment extends Fragment {
    public static final String IMAGE_ID = "image_id";
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;
    private ImageAdapter.ImageData image;
    private StorageReference mStorageRef = FirebaseStorage.getInstance()
            .getReferenceFromUrl("gs://assignment3-fa123.appspot.com");
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://assignment3-fa123.firebaseio.com/");
    private boolean exists_online = false;
    private StorageReference fileRef;
    private Uri file;

    public ImageFragment() {
        // Required empty public constructor
    }

    public static ImageFragment newInstance(ImageAdapter.ImageData imageData) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMAGE_ID, imageData);
        fragment.setArguments(args);
        return fragment;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void RaiseToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void deleteLocal () {
        String where = MediaStore.Images.Media._ID + " =?";
        String[] args = {String.valueOf(image.id)};
        int delete = getActivity().getContentResolver().delete(uri, where,
                args);
        if (delete >0) {
            RaiseToast(String.format(Locale.US, "Success: Deleted %d " +
                    "Local files",
                    delete));
            onBackPressed();
        } else {
            RaiseToast("Failed to delete any files. Try Again");
        }
    }

    public void deleteAll () {
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onSuccess(Void aVoid) {
                RaiseToast("Success: Deleted Online Backup.");
                mDatabase.child("images").child(String.valueOf(image.id))
                        .setValue(null);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            image = (ImageAdapter.ImageData) getArguments().getSerializable
                    (IMAGE_ID);
        }
    }

    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Updating the toolbar
        getActivity().setTitle(image.title);

        // Checking if image exists online
        file = Uri.fromFile(new File(image.uri));
        fileRef = mStorageRef.child("assignment3/" + file.getLastPathSegment());
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                exists_online = true;
            }
        });

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);
        TextView latLong = (TextView) view.findViewById(R.id.lat_long);
        ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
        Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        latLong.setText(String.format(Locale.US, "Lat: %f Long: %f",
                image.lat, image.lng));
        Picasso.with(getContext())
                .load("file:///" + image.uri)
                .placeholder(R.drawable.ic_camera_alt_black_250dp)
                .fit()
                .centerInside()
                .into(imageView);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("uh", "I was");
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
                            public void onClick(DialogInterface dialog, int which) {
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
