package in.nishantarora.assignment2;

import android.Manifest;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;


public class importFragment extends Fragment {
    static final int ACTIVITY_CHOOSE_FILE = 1;

    public importFragment() {
    }

    /* Checks if external storage is available for read and write */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getContext().checkSelfPermission(android.Manifest
                    .permission
                    .WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission revoked");
                ActivityCompat.requestPermissions(getActivity(), new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void selectCSVFile() {
        if (isStoragePermissionGranted()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            // get the path to sdcard
            File mem = Environment.getExternalStorageDirectory();
            // to this path add a new directory path
            File dir = new File(mem.getAbsolutePath() +
                    "/my-movies");
            // create this directory if not already created
            dir.mkdirs();
            // Opening our folder by default.
            intent.setDataAndType(Uri.fromFile(dir), "text/csv");
            startActivityForResult(Intent.createChooser(intent, "Open CSV"),
                    ACTIVITY_CHOOSE_FILE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void runImport(File file) {
        FileInputStream is = null;
        SQLiteDatabase db = null;
        TextView error = (TextView) getView().findViewById(R.id.error);
        if (file.getName().substring(file.getName().lastIndexOf('.') + 1)
                .toLowerCase().equals("csv")) {
            try {
                is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (is));
                String row;
                int count = 0;
                // DB Instance
                DBReadWrite dbRW = new DBReadWrite(getContext());
                // DB
                db = dbRW.getReadableDatabase();
                while ((row = reader.readLine()) != null) {
                    String[] RowData = row.split(",");
                    if (RowData.length == 3) {
                        String movieTitle = RowData[0];
                        String movieActor = RowData[1];
                        String movieYear = RowData[2];
                        if (count > 0) {
                            Log.v("scan", movieActor + movieTitle + movieYear);
                            ContentValues values = new ContentValues();
                            values.put(DBReadWrite.MovieStore.COL_TITLE,
                                    movieTitle);
                            values.put(DBReadWrite.MovieStore.COL_ACTOR,
                                    movieActor);
                            values.put(DBReadWrite.MovieStore.COL_YEAR,
                                    movieYear);
                            // Inserting
                            long newRowId = db.insert(DBReadWrite.MovieStore
                                            .TABLE_NAME, null,
                                    values);
                            Log.v("new row", String.valueOf(newRowId));
                        }
                        count++;
                    } else {
                        error.setVisibility(View.VISIBLE);
                        Log.v("Row data mismatch", "j");
                    }
                }
            } catch (IOException e) {
                error.setVisibility(View.VISIBLE);
                e.printStackTrace();
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                    if (db != null) {
                        db.close();
                    }
                    getFragmentManager().popBackStackImmediate();
                } catch (IOException e) {
                    // handle exception
                }
            }
        } else {
            error.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == ACTIVITY_CHOOSE_FILE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                runImport(new File(data.getData().getPath()));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_import, container,
                false);

        // Updating the toolbar
        getActivity().setTitle(R.string.import_activity);

        // Import button.
        ImageButton button = (ImageButton) view.findViewById(R.id
                .export_button);

        // Starting activity on click.
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCSVFile();
            }
        });

        return view;
    }
}
