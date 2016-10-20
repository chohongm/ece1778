package in.nishantarora.assignment3;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by whizzzkid on 18/10/16.
 */

class ImageAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<ImageData> images;
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;

    static class ImageData implements Serializable {
        long id;
        String uri, title;
        float lat, lng;
        ImageData(long id, String uri, String title, float latitude, float
                longitude) {
            this.id = id;
            this.uri = uri;
            this.title = title;
            this.lat = latitude;
            this.lng = longitude;
        }
    }

    private ArrayList<ImageData> getAllVisibleImages(Activity context) {
        ArrayList<ImageData> allImages = new ArrayList<>();

        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE
        };
        String sort = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
        String where = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] where_args = new String[]{
                "%" + Environment.DIRECTORY_DCIM + "%"};
        Cursor cursor = context.getContentResolver().query(uri, projection,
                where, where_args, sort);
        int img_id_col_id = 0;
        int img_uri_col_id = 0;
        int img_title_col_id = 0;
        int img_lat_col_id = 0;
        int img_long_col_id = 0;
        if (cursor != null) {
            img_id_col_id = cursor.getColumnIndexOrThrow(MediaStore.Images
                    .Media._ID);
            img_uri_col_id = cursor.getColumnIndexOrThrow(MediaStore.Images
                    .Media.DATA);
            img_title_col_id = cursor.getColumnIndexOrThrow(MediaStore
                    .Images.Media.TITLE);
            img_lat_col_id = cursor.getColumnIndexOrThrow(MediaStore
                    .Images.Media.LATITUDE);
            img_long_col_id = cursor.getColumnIndexOrThrow(MediaStore
                    .Images.Media.LONGITUDE);
        }

        while (cursor != null && cursor.moveToNext()) {
            ImageData img = new ImageData(
                cursor.getLong(img_id_col_id),
                cursor.getString(img_uri_col_id),
                cursor.getString(img_title_col_id),
                cursor.getFloat(img_lat_col_id),
                cursor.getFloat(img_long_col_id)
            );
            allImages.add(img);
        }

        if (cursor != null) {
            cursor.close();
        }

        return allImages;
    }

    ImageAdapter(Activity context) {
        activity = context;
        images = getAllVisibleImages(context);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public ImageData getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.picture);
        TextView text = (TextView) convertView.findViewById(R.id.text);

        final ImageData img = images.get(position);
        text.setText(img.title);

        Picasso.with(activity)
                .load("file:///" + img.uri)
                .fit()
                .placeholder(R.drawable.ic_camera_alt_black_250dp)
                .centerCrop()
                .into(image);
        return convertView;
    }
}
