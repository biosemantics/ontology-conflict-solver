package edu.arizona.biosemantics.conflictsolver;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by egurses on 3/13/18.
 */

public class NotificationsActivity extends AppCompatActivity {

    private TextView mTextMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        if(!SharedPreferencesManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }
        mTextMessage = (TextView) findViewById(R.id.message);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);

        // Set active the selected navigation icon in the new activity
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(2);
        menuItem.setChecked(true);

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case  R.id.navigation_home:
                    startActivity(new Intent(NotificationsActivity.this, HomeActivity.class));
                    finish();
                    return true;
                case R.id.navigation_dashboard:
                    startActivity(new Intent(NotificationsActivity.this, DashboardActivity.class));
                    finish();
                    return true;
                case R.id.navigation_notifications:
                    startActivity(new Intent(NotificationsActivity.this, NotificationsActivity.class));
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuLogout:
                SharedPreferencesManager.getInstance(this).logout();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.menuSettings:
                Toast.makeText(this, "You clicked settings", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }
}