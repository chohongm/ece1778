package in.nishantarora.assignment1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
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
    private Integer count;

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
                    image.setVisibility(View.VISIBLE);
                } else {
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
         * Setting default count to zero.
         */
        count = 0;

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
