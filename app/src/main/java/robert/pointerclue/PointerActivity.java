package robert.pointerclue;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class PointerActivity extends Activity {

    private ImageView arrow;
    private ProgressBar workingIndicator;
    private TextView imprecisionIndicator;
    private ImageView nearIndicator;
    private float currentAngle = 0f;

    private Location destination;

    private LocationChecker locationChecker;
    private RotationChecker rotationChecker;

    private Handler arrowUpdateHandler = new Handler();
    private long updateDelay = 50;
    Runnable arrowUpdateTask = new Runnable() {
        @Override
        public void run() {
            updateArrow();
            arrowUpdateHandler.postDelayed(this, updateDelay);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pointer);

        arrow = (ImageView) findViewById(R.id.arrow);
        workingIndicator = (ProgressBar) findViewById(R.id.workingIndicator);
        imprecisionIndicator = (TextView) findViewById(R.id.imprecisionIndicator);
        nearIndicator = (ImageView) findViewById(R.id.nearIndicator);
        locationChecker = new LocationChecker(this);
        rotationChecker = new RotationChecker(this);
        destination = new Location("");
//        destination.setLongitude(-77.6917419);
//        destination.setLatitude(43.0763321);
        destination.setLongitude(-111.894764);
        destination.setLatitude(40.562904);
    }

    @Override
    protected void onResume() {
        super.onResume();

        locationChecker.start();
        rotationChecker.start();
        arrowUpdateHandler.postDelayed(arrowUpdateTask, updateDelay);
    }

    @Override
    protected void onPause() {
        super.onPause();

        locationChecker.stop();
        rotationChecker.stop();
        arrowUpdateHandler.removeCallbacks(arrowUpdateTask);
    }

    private void updateArrow() {
        Location location = locationChecker.getLocation();
        float zOrientation = rotationChecker.getZOrientation();
        boolean imprecise = true;
        boolean stale = rotationChecker.isStale() || locationChecker.isStale();
        boolean near = false;
        if (location != null) {
            float precision = location.getAccuracy();
            float distance = location.distanceTo(destination);
            if (distance < 15 && precision < 15) {
                near = true;
            }
            if (precision < (distance / 4)) {
                imprecise = false;
            }
            float bearing = locationChecker.getLocation().bearingTo(destination);
            float displayHeading = ((zOrientation * 180) + bearing) % 360;

            // create a rotation animation (reverse turn degree degrees)
            if (Math.abs(displayHeading - currentAngle) > 180) {
                currentAngle -= 360;
            }
            RotateAnimation ra = new RotateAnimation(
                    currentAngle,
                    displayHeading,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);
            ra.setDuration(updateDelay);
            ra.setFillAfter(true);
            arrow.startAnimation(ra);
            currentAngle = displayHeading;
        }
        if (imprecise) {
            arrow.setAlpha(0.0f);
            if (near) {
                nearIndicator.setVisibility(View.VISIBLE);
                workingIndicator.setVisibility(View.INVISIBLE);
                imprecisionIndicator.setVisibility(View.INVISIBLE);
            } else {
                nearIndicator.setVisibility(View.INVISIBLE);
                workingIndicator.setVisibility(View.VISIBLE);
                imprecisionIndicator.setVisibility(View.VISIBLE);
            }
        } else {
            if (stale) {
                arrow.setAlpha(0.1f);
                workingIndicator.setVisibility(View.VISIBLE);
            } else {
                arrow.setAlpha(1.0f);
                workingIndicator.setVisibility(View.INVISIBLE);
            }
            nearIndicator.setVisibility(View.INVISIBLE);
            imprecisionIndicator.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pointer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
