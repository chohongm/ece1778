package in.nishantarora.assignment2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * This is a custom Movie Container Class.
 * This generates the structure for movie storage as well as provides them to
 * the listView.
 */
class MovieList extends BaseAdapter implements ListAdapter {
    private ArrayList<MovieInfo> list = new ArrayList<>();
    private Context context;

    MovieList(ArrayList<MovieInfo> list, Context
            context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.movie_list_row, null);
        }

        //Handle TextView and display string from your list
        final MovieInfo movie = list.get(position);
        TextView movieTitle = (TextView) view.findViewById(R.id.movie);
        TextView movieActor = (TextView) view.findViewById(R.id.actor);
        movieTitle.setText(movie.title + " [" + movie.year + "]");
        movieActor.setText(movie.actor);

        //Handle buttons and add onClickListeners
        ImageButton delete = (ImageButton) view.findViewById(R.id.delete_button);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Getting movies from db.
                DBReadWrite dbRW = new DBReadWrite(context);
                // DB instance.
                SQLiteDatabase db = dbRW.getReadableDatabase();
                // Getting movies.
                db.execSQL("DELETE FROM " + DBReadWrite.MovieStore.TABLE_NAME +
                        " WHERE " + DBReadWrite.MovieStore._ID + "=" +
                        movie.id + ";");
                list.remove(position);
                notifyDataSetChanged();
            }
        });

        return view;
    }

    static class MovieInfo {
        private final int id;
        private final String title;
        private final String actor;
        private final String year;

        MovieInfo(int movieId, String movieTitle, String movieActor, String
                movieYear) {
            this.id = movieId;
            this.title = movieTitle;
            this.actor = movieActor;
            this.year = movieYear;
        }
    }
}
