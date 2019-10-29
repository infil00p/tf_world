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

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetHandler {

    Context mCtx;
    String LOGTAG = "NPAssetHandler";

    AssetHandler(Context ctx) {
        mCtx = ctx;
        try {
            Init();
        } catch (IOException e ) {
            Log.d(LOGTAG, "Unable to get models from storage");
        }
    }

    public class ModelFileInit
    {
        File mDataDir;
        AssetManager mAssetManager;
        String mModelName;
        File mTopLevelFolder;
        File mResourcesFolder;
        File mModelFolder;
        String[] mModelFiles;
        String[] mResourceFiles;


        ModelFileInit(String modelName, File dataDir, AssetManager assetManager, String[] modelFiles,
                      String[] resourceFiles) throws IOException
        {
            mDataDir = dataDir;
            mAssetManager = assetManager;
            mModelFiles = modelFiles;
            mResourceFiles = resourceFiles;
            mModelName = modelName;
            createTopLevelDir();
            InitModelFiles();
            InitResourcesFiles();
        }

        private void InitModelFiles() throws IOException
        {
            createModelDir();
            copyModelFiles();
        }

        private void copyFileUtil(String[] files, File dir) throws IOException
        {
            // For this example, we're using the internal storage
            for (String file : files) {
                InputStream inputFile = mAssetManager.open(mModelName+'/'+file);
                File outFile;
                outFile = new File(dir, file);
                OutputStream out = new FileOutputStream(outFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputFile.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
                inputFile.close();
                out.flush();
                out.close();
            }
        }

        private void copyModelFiles() throws IOException
        {
            copyFileUtil(mModelFiles,mModelFolder);
        }

        private void InitResourcesFiles() throws IOException
        {
            createResourcesDir();
            copyResourceFiles();
        }

        private void copyResourceFiles() throws IOException
        {
            copyFileUtil(mResourceFiles,mResourcesFolder);
        }

        private void createTopLevelDir()
        {
            mTopLevelFolder = new File(mDataDir.getAbsolutePath(), mModelName);
            mTopLevelFolder.mkdir();
        }

        private void createResourcesDir()
        {
            mResourcesFolder = new File(mTopLevelFolder.getAbsolutePath(), "resources");
            mResourcesFolder.mkdir();
        }

        private void createModelDir()
        {
            mModelFolder = new File(mTopLevelFolder.getAbsolutePath(), "model");
            mModelFolder.mkdir();
        }
    }

    private void Init() throws IOException
    {
        File dataDirectory = mCtx.getFilesDir();
        AssetManager assetManager = mCtx.getAssets();

        String[] modelFiles = {"style_predict_quantized_256.tflite", "style_transfer_quantized_dynamic.tflite"};
        String[] resFiles = {"Grand_Canyon_Image.png", "Lassen_Style_Image.png", "Zion_Style_Image.png"};

        ModelFileInit styleTransfer = new ModelFileInit("styleTransfer", dataDirectory, assetManager, modelFiles, resFiles);
    }

}
