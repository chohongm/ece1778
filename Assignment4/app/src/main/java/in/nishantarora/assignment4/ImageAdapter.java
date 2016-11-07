package in.nishantarora.assignment4;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by whizzzkid on 3/11/16.
 */

public class ImageAdapter extends BaseAdapter {

    private Activity activity;
    private RealmResults<ImageWithFace> images;

    /**
     * Constructor.
     * @param context for creating adapter.
     */
    ImageAdapter(Activity context) {
        activity = context;
        images = getAllImages(context);
    }

    /**
     * Get's all images on the device in the give directory.
     * @param context for creating adapter
     * @return RealmResults<ImageWithFace> list of all images.
     */
    private RealmResults<ImageWithFace> getAllImages(Activity context) {
        //ArrayList<ImageWithFace> allImages = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<ImageWithFace> results = realm.where(ImageWithFace.class).findAll();
        return results;
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
    public ImageWithFace getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(getItem(position).getId());
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
        final ImageWithFace img = getItem(position);
        text.setText(img.getTitle());

        // Picasso is an interesting image caching library it saves on memory
        // and speeds up things.
        Picasso.with(activity)
                .load("file:///" + img.getUri())
                .fit()
                .centerCrop()
                .into(image);

        // Return the view to be placed in gridview.
        return convertView;
    }
}
