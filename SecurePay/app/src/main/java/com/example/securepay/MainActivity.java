package com.example.securepay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * The main activity of the SecurePay app.
 * Displays user measurement data and provides options for adding new measurements,
 * accessing user profile, and other navigation options.
 */

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver1;

    private FirebaseAuth mAuth;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    ImageView imageMenu;
    DatabaseReference databaseReference, databaseReference1;
    private String userid;
    public String fullname;
    private TextView usershowinnavigation;
    private Button balanceButton, balanceAmount;
    private TextView usernameTextView;
    private TextView accountNumberTextView;
    private CardView bankAccountCard, creditCardCard, transactionHistoryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProgressDialog progressDialog1 = new ProgressDialog(MainActivity.this);
        progressDialog1.setTitle("Loading");
        progressDialog1.setMessage("Please Wait");
        progressDialog1.show();
        getSupportActionBar().hide();
        broadcastReceiver1 = new Broadcaster();
        mAuth = FirebaseAuth.getInstance();
        registerReceiver(broadcastReceiver1, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        databaseReference1 = FirebaseDatabase.getInstance().getReference("userdata");
        balanceButton = findViewById(R.id.balanceShow);
        balanceAmount = findViewById(R.id.balanceAmount);
        usernameTextView = findViewById(R.id.usernameShow);
        accountNumberTextView = findViewById(R.id.accnumberShow);
        // Initialize CardViews
        bankAccountCard = findViewById(R.id.bank);
        creditCardCard = findViewById(R.id.card);
        transactionHistoryCard = findViewById(R.id.history);
        CardView bill = findViewById(R.id.bill);
// Set onClickListeners to navigate to different activities
        bankAccountCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BankAccountActivity.class);
                startActivity(intent);
            }
        });

        creditCardCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreditCardActivity.class);
                startActivity(intent);
            }
        });


        transactionHistoryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
                startActivity(intent);
            }
        });
        bill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BillPayment.class);
                startActivity(intent);

            }
        });

//load the data
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Reference to the user's profile in Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("userprofile").child(userId);
// Retrieve the user profile data
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if data exists
                if (dataSnapshot.exists()) {
                    // Retrieve the data
                    String username = dataSnapshot.child("name").getValue(String.class); // Assuming "name" is the field storing the username
                    String accountNumber = dataSnapshot.child("newAccountNumber").getValue(String.class); // Assuming "accountNumber" is the field storing the account number
                    String balance = dataSnapshot.child("balance").getValue(String.class); // Assuming "balance" is the field storing the balance
                    accountNumber = SimpleEncryptionDecryption.decrypt(accountNumber);
                    balance = SimpleEncryptionDecryption.decrypt(balance);
                    // Set the data to the TextViews
                    usernameTextView.setText(username);
                    accountNumberTextView.setText(accountNumber);
                    balanceAmount.setText(balance); // Show balance in the balance TextView
                    progressDialog1.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs while retrieving the data
                Toast.makeText(MainActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
            }
        });


        balanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animate the "Tap Here" button to the right
                TranslateAnimation animation = new TranslateAnimation(0, 0, 0, 300); // Move to the right by 300px
                animation.setDuration(500);  // Animation duration
                balanceButton.startAnimation(animation);

                // Hide the "Tap Here" button and show the balance after the animation
                balanceButton.setVisibility(View.GONE);  // Hide button
                balanceAmount.setVisibility(View.VISIBLE);  // Show balance

                // Use a Handler to wait for 4 seconds and then trigger the reverse action
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Check if the balance is still visible
                        if (balanceAmount.getVisibility() == View.VISIBLE) {
                            // Animate the "Tap Here" button back to the left after 4 seconds
                            TranslateAnimation reverseAnimation = new TranslateAnimation(0, 0, 300, 0); // Move back to original position
                            reverseAnimation.setDuration(500);  // Animation duration
                            balanceButton.startAnimation(reverseAnimation);

                            // Hide the balance text and show the "Tap Here" button
                            balanceButton.setVisibility(View.VISIBLE);  // Show button
                            balanceAmount.setVisibility(View.GONE);  // Hide balance
                        }
                    }
                }, 10000);  // Delay of 4 seconds (4000 milliseconds)
            }
        });

        balanceAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animate the "Tap Here" button to the left (reverse the original animation)
                TranslateAnimation animation = new TranslateAnimation(0, 0, 300, 0); // Move to the left
                animation.setDuration(500);  // Animation duration
                balanceButton.startAnimation(animation);

                // Hide the balance text and show the "Tap Here" button
                balanceButton.setVisibility(View.VISIBLE);  // Show button
                balanceAmount.setVisibility(View.GONE);  // Hide balance
            }
        });


        /**
         * Navigation Drawer contain user profile,signout,aboutus,share app,feedback.
         */
        //navigarion
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_View);
        navigationView.setCheckedItem(R.id.mHome);
        View header = navigationView.getHeaderView(0);
        usershowinnavigation = (TextView) header.findViewById(R.id.usershow);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("userprofile");
        if (firebaseUser != null) {
            userid = firebaseUser.getUid();
            Menu nav_Menu = navigationView.getMenu();
            nav_Menu.findItem(R.id.msignin).setVisible(false);
            nav_Menu.findItem(R.id.msignup).setVisible(false);
            usershowinnavigation.setVisibility(View.VISIBLE);
            databaseReference.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    if (userProfile != null) {
                        fullname = userProfile.name;
                        usershowinnavigation.setText("@" + fullname);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }


        imageMenu = findViewById(R.id.imageMenu);

        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            /**
             * Check where the user click in the navigation drawer.
             */
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if (item.getItemId() == R.id.mHome) {
                    Toast.makeText(MainActivity.this, "You Are in Home", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.Profile) {
                    Intent intentss = new Intent(MainActivity.this, UserProfileShow.class);
                    startActivity(intentss);
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.msignout) {
                    FirebaseAuth.getInstance().signOut();
                    finish();
                    Intent intent1 = new Intent(getApplicationContext(), UserLogin.class);
                    startActivity(intent1);
                } else if (item.getItemId() == R.id.msignin) {
                    Intent intent2 = new Intent(MainActivity.this, UserLogin.class);
                    startActivity(intent2);
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.msignup) {
                    Intent intent3 = new Intent(MainActivity.this, UserSignup.class);
                    startActivity(intent3);
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.aboutus) {
                    Toast.makeText(MainActivity.this, "About uS", Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setIcon(R.drawable.business);
                    alertBuilder.setTitle(Html.fromHtml("<b>" + getString(R.string.app_name) + "</b>"));
                    alertBuilder.setMessage("SecurePay provides a seamless and secure platform for managing financial transactions. " +
                            "Enjoy fast, reliable, and secure payment solutions at your fingertips.\n");
                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertBuilder.create();
                    alertDialog.show();
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.shareapp) {
                    Intent intents = new Intent(Intent.ACTION_SEND);
                    intents.setType("text/plain");
                    String subject = getString(R.string.app_name);
                    String body = "https://google.com";
                    intents.putExtra(Intent.EXTRA_SUBJECT, subject);
                    intents.putExtra(Intent.EXTRA_TEXT, body);
                    startActivity(Intent.createChooser(intents, "Share With"));
                    drawerLayout.closeDrawers();
                } else if (item.getItemId() == R.id.feedback) {
                    Toast.makeText(MainActivity.this, "Feedback", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/email");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"rashikbm@gmail.com"});
                    intent.putExtra(Intent.EXTRA_SUBJECT, "FEEDBACK FORM THE USER");
                    intent.putExtra(Intent.EXTRA_TEXT, "Name= " + fullname + "\n Feedback= ");
                    startActivity(Intent.createChooser(intent, "Feedback With"));
                    drawerLayout.closeDrawers();
                } else {
                    // Handle the case where no matching item ID is found (optional)
                }

                return false;
            }
        });


        imageMenu = findViewById(R.id.imageMenu);
        imageMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * Handles the back button press event.
     * Displays an alert dialog to confirm the exit action.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder alart = new AlertDialog.Builder(MainActivity.this);
        alart.setTitle("ALERT");
        alart.setMessage("Are you sure exit?");
        alart.setIcon(R.drawable.interrogation);

        alart.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alart.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "App not exit yet", Toast.LENGTH_SHORT).show();
            }
        });
        alart.setNeutralButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(MainActivity.this, "Continue your work", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog al = alart.create();
        al.show();


    }

    public boolean shouldShowBalance(boolean isBalanceVisible, long elapsedTime) {
        return !(isBalanceVisible && elapsedTime >= 10000); // Hide after 10 seconds
    }

    // Formats the account number with a prefix
    public String formatAccountNumber(String accountNumber) {
        if (accountNumber == null) {
            return null; // Return null for null input
        }
        return accountNumber;
    }
}