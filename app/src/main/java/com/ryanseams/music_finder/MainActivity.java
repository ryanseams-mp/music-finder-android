package com.ryanseams.music_finder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.OnMixpanelUpdatesReceivedListener;
import com.mixpanel.android.mpmetrics.Tweak;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {

    // Create a MixpanelAPI object for usage later
    public MixpanelAPI mixpanel;

    // Set your Mixpanel project token, this can be found under Mixpanel project settings
    private static final String MIXPANEL_TOKEN = "ryanios";

    // Create tweak variables for usage with Mixpanel mobile A/B testing
    private static Tweak<Double> gameSpeed = MixpanelAPI.doubleTweak("Game speed", 1.0);
    private static Tweak<Boolean> showAds = MixpanelAPI.booleanTweak("Show ads", false);

    // Include your Android GCM sender ID, can be found in Google Developer Console
    public static final String ANDROID_PUSH_SENDER_ID = "530582007670";

    // We will need the application context later, so create it now
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize an instance of Mixpanel using context and your project token
        mixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);

        // Create super properties for sending with all Mixpanel events
        JSONObject superprops = new JSONObject();
        try {
            superprops.put("Test", "True");
            superprops.put("Logged In", false);
            superprops.put("Distinct Id", mixpanel.getDistinctId());
        } catch (JSONException e) {
            Log.e("Send", "Unable to add super properties to JSONObject", e);
        }
        mixpanel.registerSuperProperties(superprops);

        // Create people profile updates for the current user
        JSONObject props = new JSONObject();
        try {
            props.put("Screen", "Home");
            props.put("Image", "iPod");
        } catch (JSONException e) {
            Log.e("Send", "Unable to add properties to JSONObject", e);
        }

        // Note the above will NOT send the profile information to Mixpanel, the below is still needed to flush people updates
        mixpanel.getPeople().identify(mixpanel.getDistinctId());

        // Start session timer for tracking app session lengths
        mixpanel.timeEvent("$app_open");

        // Track that the user viewed the home screen
        mixpanel.track("Viewed Screen", props);

        // Initialize Mixpanel to grab the push token of this user so we can use push notifications
        mixpanel.getPeople().initPushHandling(ANDROID_PUSH_SENDER_ID);

        // If using A/B testing, in-app notifications, or surveys, the below code will apply any received
        // updates from Mixpanel to your app immediately upon receiving
        mixpanel.getPeople().addOnMixpanelUpdatesReceivedListener(new OnMixpanelUpdatesReceivedListener() {
            @Override
            public void onMixpanelUpdatesReceived() {
                Log.d("Decide", "Got an update from Mixpanel");
                renderNotifications();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Send the session tracking information to Mixpanel
        mixpanel.track("$app_open");

        // Force all queued Mixpanel data to be sent to Mixpanel
        mixpanel.flush();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // Restart session timer for tracking app session lengths
        mixpanel.timeEvent("$app_open");
    }

    public void renderNotifications() {
        // Joins any A/B tests or shows any available notifications or surveys upon receipt of data from Mixpanel
        mixpanel.getPeople().joinExperimentIfAvailable();
        mixpanel.getPeople().showNotificationIfAvailable(this);
        mixpanel.getPeople().showSurveyIfAvailable(this);
    }

    /** Called when the user clicks the Login button */
    public void goToLogin(View view) {

        // Track that the user clicked the login button
        JSONObject props = new JSONObject();
        try {
            props.put("Screen", "Home");
        } catch (JSONException e) {
            Log.e("Send", "Unable to add properties to JSONObject", e);
        }
        mixpanel.track("Login Button Clicked", props);

        // Go to the Login Activity and switch screens
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    /** Called when the user clicks the Signup button */
    public void goToSignup(View view) {

        // Track that the user clicked the signup button
        JSONObject props = new JSONObject();
        try {
            props.put("Screen", "Home");
        } catch (JSONException e) {
            Log.e("Send", "Unable to add properties to JSONObject", e);
        }
        mixpanel.track("Sign Up Button Clicked", props);

        // Go to the Signup Activity and switch screens
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }

    // Below is all system default config for options menu

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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
