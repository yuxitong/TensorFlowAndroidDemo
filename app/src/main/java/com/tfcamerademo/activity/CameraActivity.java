/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.tfcamerademo.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

import com.tfcamerademo.Camera2BasicFragment;
import com.tfcamerademo.Camera2BasicFragment2;
import com.tfcamerademo.Camera2BasicFragment3;
import com.tfcamerademo.Camera2BasicFragment4;
import com.tfcamerademo.R;

/**
 * Main {@code Activity} class for the Camera app.
 */
public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (null == savedInstanceState) {
            int flag = getIntent().getIntExtra("Flag", 1);
            Fragment fragment = null;
            switch (flag) {
                case 1:
                    fragment = Camera2BasicFragment.newInstance();
                    break;
                case 2:
                    fragment = Camera2BasicFragment2.newInstance();
                    break;
                case 3:
                    fragment = Camera2BasicFragment3.newInstance();
                    break;
                case 4:
                    fragment = Camera2BasicFragment4.newInstance();
                    break;
            }
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        }
    }
}
