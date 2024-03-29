/*
 * Copyright (c) 2015 Prat Tanapaisankit and Intellibins authors
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *  Neither the name of The Intern nor the names of its contributors may
 * be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE LISTED COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.pt2121.envi.ui;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pt2121.envi.R;
import com.pt2121.envi.model.Loc;
import com.pt2121.envi.model.LocType;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapActivity extends ActionBarActivity
        implements MapFragment.OnFragmentInteractionListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    private MaterialDialog mFilterDialog;

    private int mCurrentFlag = LocType.BIN;

    private int mTempFlag = LocType.BIN;

    private View mTutorialView;

    private WindowManager mWindowManager;

    // TODO: http://stackoverflow.com/questions/13904505/how-to-get-center-of-map-for-v2-android-maps
    // getCameraPosition().target;

    //Test Location : New York City Department of Health and Mental Hygiene
    private final Loc mUserLoc = new Loc.Builder("Your Location")
            .address("")
            .latitude(40.715522)
            .longitude(-74.002452)
            .type(LocType.USER)
            .build();

    private MapFragment mMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //TODO fix if condition
        if (savedInstanceState != null &&
                savedInstanceState.getBoolean("dialogShown", true)) {
            mCurrentFlag = savedInstanceState.getInt("currentFlag", LocType.BIN);
            mTempFlag = savedInstanceState.getInt("tempFlag", mCurrentFlag);
            //mFilterDialog = createFilterDialog(mTempFlag);
//            mFilterDialog.show();
            onFlagChanged(mCurrentFlag);
        } else {
            onFlagChanged(mCurrentFlag);
        }

        //TODO check first time
        mFilterDialog = createFilterDialog(mCurrentFlag);
        mFilterDialog.show();

        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        mTutorialView = addTutorialView(getResources().getString(R.string.tutorial_1));
    }

    private View addTutorialView(String text) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        LayoutInflater inflater = getLayoutInflater();

        //        final View view = inflater.inflate(R.layout.tutorial_layout, null);
        ViewGroup parent = (ViewGroup) findViewById(R.id.container);
        final View view = inflater.inflate(R.layout.tutorial_layout, parent, false);

        ((TextView) view.findViewById(R.id.tutorialTextView)).setText(text);
        (view.findViewById(R.id.closeButton)).setOnClickListener(v -> mWindowManager.removeView(view));
        mWindowManager.addView(view, params);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFilterDialog != null && mFilterDialog.isShowing()) {
            mFilterDialog.dismiss();
            outState.putBoolean("dialogShown", true);
            outState.putInt("tempFlag", mTempFlag);
        } else {
            outState.putBoolean("dialogShown", false);
        }
        outState.putInt("currentFlag", mCurrentFlag);
    }

    private MaterialDialog createFilterDialog(int flag) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.action_filter)
                .customView(R.layout.fragment_filter, true)
                .positiveText(R.string.apply)
                .negativeText(android.R.string.cancel)
                .positiveColorRes(R.color.primary)
                .negativeColorRes(R.color.primary)
                .disableDefaultFonts()
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Log.d(TAG, "apply");
                        mCurrentFlag = mTempFlag;
                        onFlagChanged(mCurrentFlag);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        Log.d(TAG, "cancel");
                    }
                }).build();
        SwitchCompat binSwitch = (SwitchCompat) dialog.getCustomView().findViewById(R.id.binSwitch);
        SwitchCompat dropOffSwitch = (SwitchCompat) dialog.getCustomView().findViewById(R.id.dropOffSwitch);
        SwitchCompat wholeFoodsSwitch = (SwitchCompat) dialog.getCustomView().findViewById(R.id.wholeFoodSwitch);
        binSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setStateFlag(isChecked, LocType.BIN));
        dropOffSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setStateFlag(isChecked, LocType.DROPOFF));
        wholeFoodsSwitch.setOnCheckedChangeListener(
                (buttonView, isChecked) -> setStateFlag(isChecked, LocType.WHOLE_FOODS));
        binSwitch.setChecked((flag & LocType.BIN) == LocType.BIN);
        dropOffSwitch.setChecked((flag & LocType.DROPOFF) == LocType.DROPOFF);
        wholeFoodsSwitch.setChecked((flag & LocType.WHOLE_FOODS) == LocType.WHOLE_FOODS);
        mTempFlag = flag;
        return dialog;
    }

    private void setStateFlag(boolean isChecked, int type) {
        if (isChecked) {
            mTempFlag |= type;
        } else {
            mTempFlag &= ~type;
        }
    }

    public void onFlagChanged(int flag) {
        if (mMapFragment == null) {
            mMapFragment = MapFragment.newInstance(mUserLoc, flag);
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container, mMapFragment)
                    .commit();
        } else {
            mMapFragment.refreshMap(mUserLoc, flag);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTutorialView != null) {
            mWindowManager.removeView(mTutorialView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_filter) {
            mFilterDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // TODO
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
