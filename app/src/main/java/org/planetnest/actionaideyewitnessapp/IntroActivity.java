package org.planetnest.actionaideyewitnessapp;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.github.paolorotolo.appintro.AppIntro2;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         23/04/2017 20:34
 */

public class IntroActivity extends AppIntro2 {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(SimpleSlide.newInstance(R.layout.slide_1));
        addSlide(SimpleSlide.newInstance(R.layout.slide_2));
        addSlide(SimpleSlide.newInstance(R.layout.slide_3 ));
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        die();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        die();
    }

    @Override
    public void onBackPressed() {
        MainActivity.self.finish();
        super.onBackPressed();
    }

    private void die() {
        finish();
        MainActivity.self.doLogin();
    }
}
