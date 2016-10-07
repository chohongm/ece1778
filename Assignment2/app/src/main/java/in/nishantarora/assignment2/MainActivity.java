// ** Checklist **
// Haptic Feedback
// CSV writer
// CSV reader
package in.nishantarora.assignment2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Adding back button if we're in a fragment other than main fragment.
        getFragmentManager().addOnBackStackChangedListener(new FragmentManager
                .OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            MainActivityFragment mainFragment = new MainActivityFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            mainFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Export Fragment
        if (id == R.id.action_export) {
            // Create new fragment and transaction.
            Fragment exportFragment = new exportFragment();
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            // Replace whatever is in the fragment_container view with this
            // fragment and add the transaction to the back stack.
            transaction.replace(R.id.fragment_container, exportFragment);
            transaction.addToBackStack(null);
            // Commit the transaction.
            transaction.commit();
        }

        // Import Fragment
        if (id == R.id.action_import) {
            // Create new fragment and transaction.
            Fragment importFragment = new importFragment();
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            // Replace whatever is in the fragment_container view with this
            // fragment and add the transaction to the back stack.
            transaction.replace(R.id.fragment_container, importFragment);
            transaction.addToBackStack(null);
            // Commit the transaction.
            transaction.commit();
        }

        // Exit App
        if (id == R.id.action_exit) {
            finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }
}
