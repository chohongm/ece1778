package in.nishantarora.assignment2;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Fragment Class to handle Add Movie Form.
 */
public class AddMovieFragment extends Fragment {

    public AddMovieFragment() {
    }

    /**
     * Enables Keyboard hide on lost focus on a field.
     * @param field to which this functionality is added.
     */
    private void enableKeyboardHide(EditText field) {
        field.setOnFocusChangeListener(new hideKeyboardEventListener());
    }

    /**
     * Gets the year range in reverse order.
     * @return years in reverse
     */
    private ArrayList<String> yearRange() {
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int firstYear = 1880;
        ArrayList<String> result = new ArrayList<>();
        for (int i = thisYear; i >= firstYear; i--) {
            result.add(Integer.toString(i));
        }
        return result;
    }

    /**
     * Main View class for add movie.
     * @param inflater loads the layout XML.
     * @param container contains the fragment
     * @param savedInstanceState loads saved states
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_movie, container,
                false);

        // Updating the toolbar
        getActivity().setTitle(R.string.add_movie_activity);

        // Movie Info form.
        final EditText movieTitle = (EditText) view.findViewById(R.id
                .movie_title);
        final EditText movieActor = (EditText) view.findViewById(R.id
                .movie_actor);
        // Adding Spinner (Dropdown List) for year.
        final Spinner movieYear = (Spinner) view.findViewById(R.id.year);
        // So that this field can get focus and others can loose it.
        // To solve the keyboard not hiding problem, it is very important.
        movieYear.setFocusable(true);
        movieYear.setFocusableInTouchMode(true);
        // But if this field get's focus then the click event does not go
        // through and requires a double click to access the drop down.
        movieYear.setOnTouchListener(new solveDoubleClick());
        // Create an ArrayAdapter using the string array and a default
        // spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(),
                android.R.layout.simple_spinner_dropdown_item, yearRange());
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        movieYear.setAdapter(adapter);

        // Hiding the keyboard on loosing focus. I know this is stupid, but
        // needs to be done for every button. Sucks!
        enableKeyboardHide(movieTitle);
        enableKeyboardHide(movieActor);
        // Fuck! this ate up my entire day. Using JAVA to write such things
        // is ample proof that GOD is a lie. May he R.I.P. who proposed JAVA
        // to be used to build android.

        Button done = (Button) view.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                String movie = movieTitle.getText().toString();
                String actor = movieActor.getText().toString();
                String year = movieYear.getSelectedItem().toString();

                // validation
                if (movie.length() == 0) {
                    movieTitle.setError("Movie Name Required");
                } else if (actor.length() == 0) {
                    movieActor.setError("Actor Name Required");
                } else {
                    // DB Instance
                    DBReadWrite dbRW = new DBReadWrite(getContext());
                    // DB
                    SQLiteDatabase db = dbRW.getReadableDatabase();
                    // Setting Values
                    ContentValues values = new ContentValues();
                    values.put(DBReadWrite.MovieStore.COL_TITLE, movie);
                    values.put(DBReadWrite.MovieStore.COL_ACTOR, actor);
                    values.put(DBReadWrite.MovieStore.COL_YEAR, year);
                    // Inserting
                    long newRowId = db.insert(DBReadWrite.MovieStore
                                    .TABLE_NAME, null,
                            values);
                    Log.v("new row", String.valueOf(newRowId));
                    getFragmentManager().popBackStackImmediate();
                }
            }
        });

        return view;
    }

    /**
     * Helps hide keyboard.
     */
    private class hideKeyboardEventListener implements View
            .OnFocusChangeListener {

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) v.getContext()
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    /**
     * Solving the double click on spinner.
     */
    private class solveDoubleClick implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP && !v.hasFocus()) {
                v.performClick();
            }
            return false;
        }
    }
}