package com.robertwarrengilmore.pointerclue;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class PointerFragment extends Fragment {

    private ImageView arrow;
    private ProgressBar workingIndicator;
    private TextView imprecisionIndicator;
    private ImageView nearIndicator;
    private float currentAngle = 0f;

    private Location destination = new Location("");

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

    public PointerFragment() {
        // UR
        destination.setLatitude(43.130278);
        destination.setLongitude(-77.625);

        // house
//        destination.setLongitude(-77.6917419);
//        destination.setLatitude(43.0763321);

        // Utah
//        destination.setLongitude(-111.894764);
//        destination.setLatitude(40.562904);
    }

    @Override
    public void onResume() {
        super.onResume();

        locationChecker.start();
        rotationChecker.start();
        arrowUpdateHandler.postDelayed(arrowUpdateTask, updateDelay);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        locationChecker = new LocationChecker(getActivity());
        rotationChecker = new RotationChecker(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();

        locationChecker.stop();
        rotationChecker.stop();
        arrowUpdateHandler.removeCallbacks(arrowUpdateTask);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pointer, container, false);

        arrow = (ImageView) rootView.findViewById(R.id.arrow);
        workingIndicator = (ProgressBar) rootView.findViewById(R.id.workingIndicator);
        imprecisionIndicator = (TextView) rootView.findViewById(R.id.imprecisionIndicator);
        nearIndicator = (ImageView) rootView.findViewById(R.id.nearIndicator);

        return rootView;
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
            float displayHeading = (bearing - zOrientation) % 360;

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
            arrow.setAlpha(0f);
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
                arrow.setAlpha(1f);
                workingIndicator.setVisibility(View.INVISIBLE);
            }
            nearIndicator.setVisibility(View.INVISIBLE);
            imprecisionIndicator.setVisibility(View.INVISIBLE);
        }

    }
}
