package edu.arizona.biosemantics.conflictsolver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



/**
 * Created by egurses on 3/13/18.
 */

public class DecisionActivity extends AppCompatActivity {

    private static final String TAG = "DecisionActivity";

    private String mChoice="";
    private int mPosition;
    private String mConflictId;
    private String mExpertId;
    private boolean mIsChecked;

    private EditText editTextWrittenComment;
    private ProgressDialog progressDialog;


    private TermOptions mTermOptions  = new TermOptions();


    //@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_decision);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        if(!SharedPreferencesManager.getInstance(this).isLoggedIn()){
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        mConflictId  = getIntent().getStringExtra("ConflictId");
        mExpertId    = String.valueOf(SharedPreferencesManager.getInstance(this).getExpertId());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submiting the answer...");

        // Call the getOptions method
        getOptions();

        // Call the setNavigation method
        setNavigation();
    }

    private void setLayout(){

        // Set the header for the confusing term
        final TextView textviewTerm = (TextView) findViewById(R.id.term);
        textviewTerm.setText(mTermOptions.getTerm());

        // Set the header for the confusing term
        final TextView textviewSentence = (TextView) findViewById(R.id.sentence);
        textviewSentence.setText(mTermOptions.getSentence());


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        TermOptionsAdapter adapter = new TermOptionsAdapter(this,
                mTermOptions.getImageLinks(),
                mTermOptions.getOptions(),
                mTermOptions.getDefinitions());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Set listener for SUBMIT button
        Button button = findViewById(R.id.submit);
        setButtonListener (button);

        editTextWrittenComment = (EditText) findViewById(R.id.editText);
        RelativeLayout relativeLayoutXML =(RelativeLayout)findViewById(R.id.relativeLayoutXML);
        EditText editText = new EditText(this);
        editText.setHint(R.string.Enter_Your_Definition_Hint);
    }


    private void setButtonListener (Button button) {

        button.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mPosition = SharedPreferencesManager.getInstance(getApplicationContext()).
                        getSelectedOption();

                if (mPosition != -1) {

                    mChoice = String.valueOf(mTermOptions.getOptions().get(mPosition));
                    submitDecision();
                    Intent intent = new Intent(DecisionActivity.this,
                            TasksActivity.class);
                    intent.putExtra("solvedFlag", true );
                    startActivity(intent);

                } else {

                    Toast.makeText(getApplicationContext(),"Please select one of the options ",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setNavigation(){

        //Set active the selected navigation icon in the new activity
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void getOptions(){

        Integer id = Integer.valueOf(getIntent().getStringExtra("TermId"));
        String uri = String.format(Constants.URL_GETOPTIONS+"?ID=%1$s",id);
        System.out.print(uri);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                uri,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try{
                            JSONObject root = new JSONObject(response);
                            JSONArray options_data = root.getJSONArray("options_data");

                            for (int i = 0; i < options_data.length(); i++) {

                                JSONObject jsonObject = options_data.getJSONObject(i);

                                mTermOptions.addOption(jsonObject.getString("option_"));
                                mTermOptions.addDefinition(jsonObject.getString("definition"));
                                mTermOptions.addImageLink(jsonObject.getString("image_link"));

                            }

                            mTermOptions.setTerm(getIntent().getStringExtra("Term"));
                            mTermOptions.setSentence(getIntent().getStringExtra("Sentence"));

                            // Call the layout method right after the data is fetched
                            setLayout();
                        }
                        catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(
                                getApplicationContext(),
                                error.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void submitDecision() {


        final String writtenComment = editTextWrittenComment.getText().toString().trim();
        //final String voiceComment   = editTextVoiceComment.getText().toString().trim();


        progressDialog.show();

        // Inner Class for string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.URL_PROCESSDECISION,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            Toast.makeText(getApplicationContext(),
                                    jsonObject.getString("message"),
                                    Toast.LENGTH_LONG).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("expertId", mExpertId);
                params.put("conflictId", mConflictId);
                params.put("choice", mChoice);
                params.put("writtenComment",writtenComment);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    startActivity(new Intent(DecisionActivity.this, HomeActivity.class));
                    finish();
                    return true;
                case R.id.navigation_dashboard:
                    return true;
                case R.id.navigation_notifications:
                    startActivity(new Intent(DecisionActivity.this, TasksActivity.class));
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