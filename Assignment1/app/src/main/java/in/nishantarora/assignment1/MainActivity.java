package in.nishantarora.assignment1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;


/**
 * Implements a listener function which produces haptic feedback on touch.
 * This implementation is supposed to be faster than onClickListener as it does
 * not wait 300ms for click event to complete.
 */
class TouchHapticListener implements View.OnTouchListener {
    /**
     * Public method to be overridden in implementation.
     */
    public void performAction() {}

    /**
     * Implements the onTouch method which calls the performAction.
     *
     * @param v The view the touch event has been dispatched to.
     * @param e The MotionEvent object containing full info about the event.
     * @return True if we consume the event.
     */
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        /**
         * This tracks the touch event.
         */
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            /**
             * Generating a haptic feedback of constant duration: VIRTUAL_KEY.
             */
            v.performHapticFeedback(HapticFeedbackConstants
                    .VIRTUAL_KEY);

            /**
             * Performing the action.
             */
            performAction();
            return true;
        }
        return false;
    }
}


/**
 * Main Activity class.
 */
public class MainActivity extends AppCompatActivity {
    private ImageView image;
    private TextView counter;
    private Integer count = 0;
    private Animation fadeIn, fadeOut;

    /**
     * Generating Options Menu.
     * @param menu Menu Object.
     * @return true always.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.activity_menu, menu);
        return true;
    }

    /**
     * Handling Option Selection.
     * @param item which was selected.
     * @return true if event was handled or the super class
     * onOptionsItemSelected, which always defaults to false.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.reset){
            count = 0;
            image.setVisibility(View.INVISIBLE);
            counter.setText(R.string.counter);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Creating the content view.
     * @param savedInstanceState Activities have the ability, under special
     *                           circumstances, to restore themselves to a
     *                           previous state using the data stored in
     *                           this bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Animations.
         */
        fadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);


        /**
         * Image Container
         */
        image = (ImageView) findViewById(R.id.imageView);

        /**
         * Toggle Button that toggles the image in the image container.
         */
        Button toggleButton = (Button) findViewById(R.id.toggle);

        /**
         * Do the action on touch event.
         */
        toggleButton.setOnTouchListener(new TouchHapticListener() {
            public void performAction() {
                if (image.getVisibility() == View.INVISIBLE) {
                    image.startAnimation(fadeIn);
                    image.setVisibility(View.VISIBLE);
                } else {
                    image.startAnimation(fadeOut);
                    image.setVisibility(View.INVISIBLE);
                }
            }
        });


        /**
         * Click Counter message container.
         */
        counter = (TextView) findViewById(R.id.counter);

        /**
         * Clicker button.
         */
        Button clicker = (Button) findViewById(R.id.clicker);

        /**
         * Perform action on clicks.
         */
        clicker.setOnTouchListener(new TouchHapticListener() {
            public void performAction() {

            /**
             * Incrementing...
             */
            count++;

            /**
             * Just for the sake of plurals.
             */
            if (count == 1) {
                counter.setText(
                        String.format(
                                Locale.getDefault(),
                                "I was clicked %d time",
                                count));
            } else {
                counter.setText(
                        String.format(
                                Locale.getDefault(),
                                "I was clicked %d times",
                                count));
            }
            }
        });
    }
}
