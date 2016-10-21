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
 * Custom image adapter class to handle gridviews more efficiently.
 */
class ImageAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<ImageData> images;
    private Uri uri = android.provider.MediaStore.Images.Media
            .EXTERNAL_CONTENT_URI;

    /**
     * Constructor.
     * @param context
     */
    ImageAdapter(Activity context) {
        activity = context;
        images = getAllVisibleImages(context);
    }

    /**
     * Get's all images on the device in the give directory.
     * @param context
     * @return
     */
    private ArrayList<ImageData> getAllVisibleImages(Activity context) {
        ArrayList<ImageData> allImages = new ArrayList<>();

        // The data we need to query from the storage.
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.TITLE,
                MediaStore.Images.Media.LATITUDE,
                MediaStore.Images.Media.LONGITUDE
        };

        // Sorting the data.
        String sort = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        // We need only the data in DCIM folder for now.
        String where = MediaStore.Images.Media.DATA + " LIKE ?";
        String[] where_args = new String[]{
                "%" + Environment.DIRECTORY_DCIM + "%"};

        // Running the query.
        Cursor cursor = context.getContentResolver().query(uri, projection,
                where, where_args, sort);

        // Some vars.
        int img_id_col_id = 0;
        int img_uri_col_id = 0;
        int img_title_col_id = 0;
        int img_lat_col_id = 0;
        int img_long_col_id = 0;

        // Mapping cursor cols to vars defined earlier.
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

        // Iterating over the cursor to build are image array.
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

        // We're done, close this to free up resources.
        if (cursor != null) {
            cursor.close();
        }

        return allImages;
    }

    /**
     * Get's us the count of images in the adapter.
     * @return int count.
     */
    @Override
    public int getCount() {
        return images.size();
    }

    /**
     * Get's item at a particular position in the image list.
     * @param position int index of the image required.
     * @return ImageData object of the image at position.
     */
    @Override
    public ImageData getItem(int position) {
        return images.get(position);
    }

    /**
     * Get's image id for the requested position.
     * @param position int index of the image required.
     * @return long id of the image.
     */
    @Override
    public long getItemId(int position) {
        return images.get(position).id;
    }

    /**
     * Generates view for the image. We can use these to populate the gridview.
     * @param position int index of the image.
     * @param convertView would use this to recycle views to save resources/
     * @param parent parent of the views, in this case gridview. can be any!
     * @return View object for the image
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // If we do not yet have a view, then let's create one. Else will use
        // the existing. They call this recycling :)
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Wrote this custom layout which is always square and resizes
            // the image to always fill the square.
            convertView = inflater.inflate(R.layout.grid_item, parent, false);
        }

        // Finding the image and text views in our layout.
        ImageView image = (ImageView) convertView.findViewById(R.id.picture);
        TextView text = (TextView) convertView.findViewById(R.id.text);

        // Getting image data
        final ImageData img = images.get(position);
        text.setText(img.title);

        // Picasso is an interesting image caching library it saves on memory
        // and speeds up things.
        Picasso.with(activity)
                .load("file:///" + img.uri)
                .fit()
                .placeholder(R.drawable.ic_camera_alt_black_250dp)
                .centerCrop()
                .into(image);

        // Return the view to be placed in gridview.
        return convertView;
    }

    /**
     * This is how I like to store the image data. Need to make it
     * serializable so that I can pass this between fragments.
     */
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
}
