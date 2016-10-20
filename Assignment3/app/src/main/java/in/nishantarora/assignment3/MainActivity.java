package in.nishantarora.assignment3;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance()
            .getReferenceFromUrl("https://assignment3-fa123.firebaseio.com/");

    @RequiresApi(api = Build.VERSION_CODES.M)
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
                   toolbar.setNavigationOnClickListener(new View
                           .OnClickListener() {
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
        // Firebase
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d("Firebase", "onAuthStateChanged:signed_in:" + user
                            .getUid());
                } else {
                    // User is signed out
                    Log.d("Firebase", "onAuthStateChanged:signed_out");
                }
            }
        };
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("firebase", "signInAnonymously:onComplete:" + task
                                .isSuccessful());
                        if (!task.isSuccessful()) {
                            Log.w("firebase", "signInAnonymously", task
                                    .getException());
                            Toast.makeText(getApplicationContext(),
                                    "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
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
            MainActivityFragment fragment = new MainActivityFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            fragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment).commit();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_restore) {
            DatabaseReference storedData = mDatabase.child("images").getRef();
            storedData.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot child: dataSnapshot.getChildren()) {
                        Map<String, Object> image= (Map<String, Object>) child.getValue();
                        System.out.println("HASH MAP DUMP: " + image.toString
                                ());
                        Log.v("test", (String) image.get("title"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Log.v("store", String.valueOf(storedData));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
