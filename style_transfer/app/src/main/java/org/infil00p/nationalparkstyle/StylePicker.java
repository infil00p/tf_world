/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~ Copyright 2019 Adobe
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~     http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

package org.infil00p.nationalparkstyle;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageButton;

public class StylePicker extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_picker);

        ImageButton Lassen = findViewById(R.id.LassenButton);
        ImageButton Zion = findViewById(R.id.ZionButton);
        ImageButton GrandCanyon = findViewById(R.id.GrandCanyonButton);

        GrandCanyon.setOnClickListener(new StyleClickListener(0));

        Lassen.setOnClickListener(new StyleClickListener(1));

        Zion.setOnClickListener(new StyleClickListener(2));

    }

    class StyleClickListener implements View.OnClickListener {

        int styleVal;

        StyleClickListener(int val) {
            styleVal = val;
        }

        @Override
        public void onClick(View v) {
            Intent output = new Intent();
            output.putExtra(MainActivity.StyleCode, styleVal);
            setResult(RESULT_OK, output);
            finish();
        }
    }

}
