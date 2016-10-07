package in.nishantarora.assignment2;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        // Setting Title.
        getActivity().setTitle(R.string.app_name);

        // Floating button.
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id
                .fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create new fragment and transaction.
                Fragment addMovie = new AddMovieFragment();
                FragmentTransaction transaction = getFragmentManager()
                        .beginTransaction();
                // Replace whatever is in the fragment_container view with this
                // fragment and add the transaction to the back stack.
                transaction.replace(R.id.fragment_container, addMovie);
                transaction.addToBackStack(null);
                // Commit the transaction.
                transaction.commit();
            }
        });

        // Getting movies from db.
        DBReadWrite dbRW = new DBReadWrite(getContext());
        // DB instance.
        SQLiteDatabase db = dbRW.getReadableDatabase();
        // Getting movies.
        Cursor c = db.rawQuery("SELECT * FROM " +
                DBReadWrite.MovieStore.TABLE_NAME + " ORDER BY " +
                DBReadWrite.MovieStore.COL_TITLE + " ASC;", null);

        ArrayList<MovieList.MovieInfo> movies = new ArrayList<>(c.getCount());
        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            String movieTitle = c.getString(c.getColumnIndex(DBReadWrite
                    .MovieStore.COL_TITLE));
            String movieActor = c.getString(c.getColumnIndex(DBReadWrite
                    .MovieStore.COL_ACTOR));
            String movieYear = c.getString(c.getColumnIndex(DBReadWrite
                    .MovieStore.COL_YEAR));
            int movieId = c.getInt(c.getColumnIndex(DBReadWrite
                    .MovieStore._ID));
            MovieList.MovieInfo movieInfo = new MovieList.MovieInfo
                    (movieId, movieTitle, movieActor, movieYear);
            movies.add(movieInfo);
            c.moveToNext();
        }
        // Terminating Connection
        c.close();
        // Setting Up listView.
        ListView list = (ListView) view.findViewById(R.id.movie_list);
        // Empty Message.
        list.setEmptyView(view.findViewById(R.id.welcome_msg));
        // Movie Data
        MovieList movieList = new MovieList(movies, this.getContext());
        // Magic
        list.setAdapter(movieList);

        return view;
    }

}
