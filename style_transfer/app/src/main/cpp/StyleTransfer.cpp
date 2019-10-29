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

#include "StyleTransfer.h"
#include <opencv2/opencv.hpp>
#include <opencv2/imgcodecs/legacy/constants_c.h>
#include <opencv2/imgproc/types_c.h>
#include <android/log.h>

#define LOG_TAG "StyleTransform"

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

std::string APP_PATH = "/data/data/org.infil00p.nationalparkstyle/files/";
std::string INPUT_IMAGE = APP_PATH + "input.jpg";
std::string style_predict_model = APP_PATH + "styleTransfer/model/style_predict_quantized_256.tflite";
std::string style_transfer_model = APP_PATH + "styleTransfer/model/style_transfer_quantized_dynamic.tflite";
std::string GRAND_CANYON = APP_PATH + "styleTransfer/resources/Grand_Canyon_Image.png";
std::string LASSEN = APP_PATH + "styleTransfer/resources/Lassen_Style_Image.png";
std::string ZION = APP_PATH + "styleTransfer/resources/Zion_Style_Image.png";

StyleTransfer::StyleTransfer() {

    // Spin up the interpreter
    style_predict_model_ = ::tflite::FlatBufferModel::BuildFromFile(style_predict_model.c_str());
    transfer_model_ = ::tflite::FlatBufferModel::BuildFromFile(style_transfer_model.c_str());
    ::tflite::ops::builtin::BuiltinOpResolver resolver;
    ::tflite::InterpreterBuilder style_builder(*style_predict_model_, resolver);
    ::tflite::InterpreterBuilder transform_builder(*transfer_model_, resolver);


    if (style_builder(&style_interpreter_) != kTfLiteOk) {
    }
    if (transform_builder(&transfer_interpreter_) != kTfLiteOk) {
    }

}

/*
 * This will run the inference on both, so that we can get the transformed image.
 *
 * Input: An OpenCV Mat
 * Input: A pre-defined style
 *
 * Output: A String going to where the stored image is on private storage
 */
std::string StyleTransfer::getRenderedStyle(int styleChosen) {

    // Predict the style
    // Resize the image to the shape and do pre-processing
    // Do the style transfert
    // Do the post-processing to get this image back into a jpeg format

    // Do the style transfer code
    std::vector<float> styleVec = getStyle(styleChosen);
    if(styleVec.size() > 0) {
        cv::Mat image = cv::imread(INPUT_IMAGE, CV_LOAD_IMAGE_COLOR);

        cv::Mat processedImage = preProcessImage(image);

        transfer_interpreter_->AllocateTensors();

        auto contentImageIndex = fromNameToIndex("content_image", true, false);
        auto styleInputIndex = fromNameToIndex("mobilenet_conv/Conv/BiasAdd", true, false);

        auto contentBuffer = transfer_interpreter_->typed_tensor<float>(contentImageIndex);
        auto styleBuffer = transfer_interpreter_->typed_tensor<float>(styleInputIndex);
        TfLiteIntArray* styleDims = transfer_interpreter_->tensor(styleInputIndex)->dims;
        TfLiteIntArray* contentDims = transfer_interpreter_->tensor(contentImageIndex)->dims;
        unsigned int styleSize = sizeof(float);
        unsigned int contentSize = sizeof(float);
        for(int i = 1; i < styleDims->size; ++i) {
            styleSize = styleSize * styleDims->data[i];
        }
        for(int j = 1; j < contentDims->size; ++j) {
            contentSize = contentSize * contentDims->data[j];
        }
        memcpy(contentBuffer, processedImage.data, contentSize);
        memcpy(styleBuffer, styleVec.data(), styleSize);

        if(transfer_interpreter_->Invoke() == kTfLiteOk) {
            auto outputIndex = fromNameToIndex("transformer/expand/conv3/conv/Sigmoid", false, false);
            TfLiteIntArray* dims = transfer_interpreter_->tensor(outputIndex)->dims;
            int outputSize = 1;
            for(int i = 1; i < dims->size; ++i) {
                outputSize = outputSize * dims->data[i];
            }

            int width = dims->data[1];
            int height = dims->data[2];

            cv::Size outputImageSize = cv::Size(width, height);
            // Get the data out of the outputBuffer
            const float * outputBuffer = transfer_interpreter_->typed_tensor<float>(outputIndex);

            auto tensorMat = cv::Mat(outputImageSize, CV_32FC3, (void *) outputBuffer);
            cv::Mat outputImage;
            const cv::Scalar maxVal = cv::Scalar(255, 255, 255);
            cv::multiply(tensorMat, maxVal, outputImage);
            outputImage.convertTo(outputImage, CV_8UC3);
            std::string outputString = APP_PATH + "/output.jpg";
            cv::imwrite(outputString, outputImage);

            return outputString;
        } else {
            return "";
        }
    }
    return "";

}


std::vector<float> StyleTransfer::getStyle(int styleVal) {
    std::string styleImage;
    switch(styleVal) {
        case(0) :
            styleImage = GRAND_CANYON;
            break;
        case(1) :
            styleImage = LASSEN;
            break;
        case(2) :
        default :
            styleImage = ZION;
            break;
    }
    cv::Mat styleMat =  cv::imread(styleImage, CV_LOAD_IMAGE_COLOR);
    cv::cvtColor(styleMat, styleMat, cv::COLOR_BGR2RGB);

    styleMat.convertTo(styleMat, CV_32F, 1.f/255);

    std::string inputName = "style_image";
    auto inputIndex = fromNameToIndex(inputName, true, true);

    style_interpreter_->AllocateTensors();

    auto tensorBuffer = style_interpreter_->typed_tensor<float>(inputIndex);
    unsigned int tensorSize = styleMat.total() * styleMat.elemSize();
    memcpy((void *) tensorBuffer, (void *)styleMat.data, tensorSize);

    if(style_interpreter_->Invoke() != kTfLiteOk) {
        // Return the empty vector
        std::vector<float> emptyVec;
        return emptyVec;
    } else {
        auto outputIndex = fromNameToIndex("mobilenet_conv/Conv/BiasAdd", false, true);

        // First element in the output shape is the batch size.
        TfLiteIntArray* dims = style_interpreter_->tensor(outputIndex)->dims;
        int outputSize = 1;
        for(int i = 1; i < dims->size; ++i) {
            outputSize = outputSize * dims->data[i];
        }
        size_t outputByteSize = outputSize * sizeof(float);

        std::vector<float> outputFloat;
        outputFloat.resize(outputSize);

        // Get the data out of the outputBuffer (I don't think we actually need to do a memcpy here)
        const float * outputBuffer = style_interpreter_->typed_tensor<float>(outputIndex);
        memcpy(outputFloat.data(), outputBuffer, outputByteSize);


        return outputFloat;
    }

}

int StyleTransfer::fromNameToIndex(std::string stdName, bool isInput, bool isStylePredict) const
{
    ::tflite::Interpreter * interpreter;

    if(isStylePredict)
        interpreter = style_interpreter_.get();
    else
        interpreter = transfer_interpreter_.get();

    int len = isInput ? interpreter->inputs().size()
                      : interpreter->outputs().size();


    for (int tIndex = 0; tIndex < len; ++tIndex)
    {
        std::string tName = std::string(isInput ? interpreter->GetInputName(tIndex)
                                                : interpreter->GetOutputName(tIndex));
        if (tName == stdName)
        {
            return isInput ? interpreter->inputs()[tIndex]
                           : interpreter->outputs()[tIndex];
        }
    }
    return -1;
}


StyleTransfer::~StyleTransfer() {

    // Do the tidying up, disposing of GPU delegates and things like that

}

cv::Mat StyleTransfer::preProcessImage(cv::Mat input) {

    std::string firstImageStr = APP_PATH + "/raw_input.jpg";
    cv::imwrite(firstImageStr, input);

    cv::Mat resizedImage;
    cv::Size imageSize(384, 384);
    cv::resize(input, resizedImage, imageSize);
    // I don't want to lose the alpha channel of the image coming in
    cv::cvtColor(resizedImage, resizedImage, cv::COLOR_BGR2RGB);

    std::string outputString = APP_PATH + "/test_input.jpg";
    cv::imwrite(outputString, resizedImage);

    resizedImage.convertTo(resizedImage, CV_32F, 1.f/255);

    return resizedImage;

}
