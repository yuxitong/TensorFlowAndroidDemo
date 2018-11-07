/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

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

package com.tfcamerademo.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Trace;
import android.util.Log;
import android.widget.ImageView;

import com.tfcamerademo.Classifier;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

/**
 * 监测道路并绘制不规则道路图形
 */
public class TensorFlowImageClassifier2 implements Classifier {
    private static final String TAG = "TensorFlowImageClassifier";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize_W;
    private int inputSize_H;
    private int imageMean;
    private float imageStd;

    private static Context context1;
    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    private float[] floatValues;
    private float[] outputs;
    private String[] outputNames;

    private static ImageView iv12;
    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageClassifier2() {
    }

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize     The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean     The assumed mean of the image values.
     * @param imageStd      The assumed std of the image values.
     * @param inputName     The label of the image input node.
     * @param outputName    The label of the output node.
     * @throws IOException
     */
    public static Classifier create(
            Context context,
            ImageView iv1,
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int inputSize_W,
            int inputSize_H,
            int imageMean,
            float imageStd,
            String inputName,
            String outputName) {
        TensorFlowImageClassifier2 c = new TensorFlowImageClassifier2();
        c.inputName = inputName;
        c.outputName = outputName;
        context1 = context;
        iv12 = iv1;
        // Read the label names into memory.
        // TODO(andrewharp): make this handle non-assets.
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                c.labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        // The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        final Operation operation = c.inferenceInterface.graphOperation(outputName);


        Log.i(TAG, "operation.output(0).shape() " + operation.output(0).shape());

//    int[] list={80,160,a};
        final int numClasses = inputSize_W * inputSize_H;
        //    final int numClasses = 12800;
//    final int numClasses = (int) operation.output(0).shape().size(a);
//    final int numClasses = (int) operation.output(0).shape().size(a);


        Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + numClasses);

        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        c.inputSize_W = inputSize_W;
        c.inputSize_H = inputSize_H;
        c.imageMean = imageMean;
        c.imageStd = imageStd;

        // Pre-allocate buffers.
        c.outputNames = new String[]{outputName};
        c.intValues = new int[inputSize_W * inputSize_H];
        c.floatValues = new float[inputSize_W * inputSize_H * 3];
        c.outputs = new float[numClasses];

        return c;
    }

    @SuppressLint("LongLogTag")
    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
//            Log.e("123",val+"");
//            floatValues[i * 3 + 0] = ((val >> 16) - imageMean) / imageStd;
            //以下为中心回归波纹
//            floatValues[i * 3 + 0] = ((val >> 16) - imageMean) / imageStd; //R
//            floatValues[i * 3 + 1] = ((val >> 8) - imageMean) / imageStd;  //G
//            floatValues[i * 3 + 2] = (val - imageMean) / imageStd;         //B

            //以下为改纯RGB输入
            floatValues[i * 3 + 0] = (byte)(val >> 16); //R
            floatValues[i * 3 + 1] = (byte)(val >> 8) ;  //G
            floatValues[i * 3 + 2] = (byte)val ;         //B
        }

        Trace.endSection();


        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, floatValues, 1, inputSize_W, inputSize_H, 3);
        Trace.endSection();

        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, logStats);
        Trace.endSection();

        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);


        int[] b = new int[inputSize_W * inputSize_H];

        for (int j = 0; j < outputs.length; j++) {
            int c = (int) (outputs[j] * 255);
            //4通道输出  分别是 ARGB  其中 A通道C<<24是为了让有颜色的地方透明度低些 让没有颜色的地方透明度高一些
            b[j] = c<<24 | (0 << 16) | (c << 8);
        }

        final Bitmap bitmap1 = Bitmap.createBitmap(b, inputSize_W, inputSize_H, Bitmap.Config.ARGB_8888);
//
        if (iv12 != null) {
            iv12.post(new Runnable() {
                @Override
                public void run() {
                    iv12.setImageBitmap(bitmap1);
                }
            });

        }


        int res_len = outputs.length;
        Log.i(TAG, "inferenceInterface.fetch(outputName, outputs); " + " " + outputs[res_len - 4] + " " + outputs[res_len - 3] + " " + outputs[res_len - 2] + " " + outputs[res_len - 1]);

        Trace.endSection();

        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        3,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });
        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > THRESHOLD) {
                pq.add(
                        new Recognition(
                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
            }
        }
        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }

    @Override
    public void enableStatLogging(boolean logStats) {
        this.logStats = logStats;
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }
}
