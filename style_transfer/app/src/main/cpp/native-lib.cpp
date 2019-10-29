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

#include <jni.h>
#include <string>
#include <android/log.h>

#include <opencv2/opencv.hpp>
#include "StyleTransfer.h"


extern "C" JNIEXPORT jstring JNICALL
Java_org_infil00p_nationalparkstyle_MainActivity_doStyleTransform(
        JNIEnv *env,
        jobject instance,
        jint styleSelected
        ) {

    StyleTransfer myStyle;

    std::string outputUri = myStyle.getRenderedStyle(styleSelected);

    return env->NewStringUTF(outputUri.c_str());
}
