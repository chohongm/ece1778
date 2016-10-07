package in.nishantarora.assignment2;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileOutputStream;

import static android.content.ContentValues.TAG;


public class exportFragment extends Fragment {
    public exportFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_export, container,
                false);

        // Updating the toolbar
        getActivity().setTitle(R.string.export_activity);
        // Export button.
        ImageButton button = (ImageButton) view.findViewById(R.id
                .export_button);
        final EditText filename = (EditText) view.findViewById(R.id.file_name);
        button.setOnClickListener(new View.OnClickListener() {
            /* Checks if external storage is available for read and write */
            boolean isStoragePermissionGranted() {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(android.Manifest
                            .permission
                            .WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Log.v(TAG, "Permission is granted");
                        return true;
                    } else {

                        Log.v(TAG, "Permission is revoked");
                        ActivityCompat.requestPermissions(getActivity(), new
                                String[]{Manifest.permission
                                .WRITE_EXTERNAL_STORAGE}, 1);
                        return false;
                    }
                } else {
                    Log.v(TAG, "Permission is granted");
                    return true;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (isStoragePermissionGranted()) {
                    // Checking filename
                    if (filename.length() == 0) {
                        filename.setError("Filename Required!");
                    } else {
                        // get the path to sdcard
                        File mem = Environment.getExternalStorageDirectory();
                        // to this path add a new directory path
                        File dir = new File(mem.getAbsolutePath() +
                                "/my-movies");
                        // create this directory if not already created
                        dir.mkdirs();
                        // create the file in which we will write the contents
                        File file = new File(dir, filename.getText() +
                                ".csv");
                        if (file.exists()) {
                            filename.setError("File Already Exists, enter a " +
                                    "new name.");
                        } else {
                            // File Output
                            FileOutputStream os = null;
                            // Getting movies from db.
                            DBReadWrite dbRW = new DBReadWrite(getContext());
                            // DB instance.
                            SQLiteDatabase db = dbRW.getReadableDatabase();
                            // Getting movies.
                            Cursor c = db.rawQuery("SELECT * FROM " +
                                    DBReadWrite.MovieStore.TABLE_NAME +
                                    " ORDER BY " +
                                    DBReadWrite.MovieStore.COL_TITLE + " ASC;",
                                    null);

                            if (c.getCount() == 0) {
                                filename.setError("You Do Not Have Any " +
                                        "Movies!");
                            } else {
                                c.moveToFirst();

                                String csvRow = "%s, %s, %s\n";
                                String header = String.format(csvRow,
                                        "Movie Title", "Movie Actor", "Movie " +
                                                "Year");

                                try {
                                    os = new FileOutputStream(file);
                                    os.write(header.getBytes());
                                    for (int i = 0; i < c.getCount(); i++) {
                                        String movieTitle = c.getString(c
                                                .getColumnIndex(DBReadWrite
                                                        .MovieStore.COL_TITLE));
                                        String movieActor = c.getString(c
                                                .getColumnIndex(DBReadWrite
                                                        .MovieStore.COL_ACTOR));
                                        String movieYear = c.getString(c
                                                .getColumnIndex(DBReadWrite
                                                        .MovieStore.COL_YEAR));
                                        String row = String.format(csvRow,
                                                movieTitle, movieActor,
                                                movieYear);
                                        os.write(row.getBytes());
                                        c.moveToNext();
                                    }
                                    // Terminating File.
                                    os.close();
                                    // Terminating DB Connection.
                                    c.close();
                                    // Opening file Once Done
                                    Intent intent = new Intent();
                                    intent.setAction(android.content.Intent
                                            .ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(file),
                                            "text/csv");
                                    startActivityForResult(intent, 10);
                                    // Closing fragment
                                    getFragmentManager()
                                            .popBackStackImmediate();
                                } catch (java.io.IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }

            }
        });

        return view;
    }
}
