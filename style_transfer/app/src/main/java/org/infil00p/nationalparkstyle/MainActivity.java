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

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    static String StyleCode = "STYLE";

    int lastPickedStyle;
    int REQUEST_LOAD_IMAGE = 1;
    int REQUEST_PICK_STYLE = 2;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AssetHandler manager = new AssetHandler(this);

        Button button = findViewById(R.id.getImage);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, REQUEST_LOAD_IMAGE);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (reqCode == REQUEST_LOAD_IMAGE) {
                try {
                    final Uri imageUri = data.getData();

                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final InputStream exifStream = getContentResolver().openInputStream(imageUri);

                    ExifInterface exif = new ExifInterface(exifStream);
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);

                    Matrix rotMatrix = new Matrix();
                    switch(orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            rotMatrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            rotMatrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            rotMatrix.postRotate(270);
                            break;
                    }

                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                    Bitmap rotatedBitmap = Bitmap.createBitmap(selectedImage, 0, 0,
                            selectedImage.getWidth(), selectedImage.getHeight(),
                            rotMatrix, true);

                    FileOutputStream fos = this.openFileOutput("input.jpg", Context.MODE_PRIVATE);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    Intent i = new Intent(this, StylePicker.class);
                    startActivityForResult(i, REQUEST_PICK_STYLE);
                } catch (Exception e) {
                    Log.e(StyleCode, "Unable to get the image in Java");
                }

            } else if (reqCode == REQUEST_PICK_STYLE && data != null) {
                lastPickedStyle = data.getIntExtra(MainActivity.StyleCode, 0);
                String uri = doStyleTransform(lastPickedStyle);
                ImageView view = findViewById(R.id.imageView);
                Bitmap output = BitmapFactory.decodeFile(uri);
                view.setImageBitmap(output);
            }
        } else {

        }
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String doStyleTransform(int styleSelected);

}
