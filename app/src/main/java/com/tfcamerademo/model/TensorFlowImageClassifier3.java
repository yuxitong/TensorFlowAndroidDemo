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
import android.graphics.Canvas;
import android.widget.ImageView;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.Log;

import com.tfcamerademo.Classifier;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.List;
import java.util.Vector;
import java.lang.Math;

/**
 * 骨架识别 并绘制关键点包括 头 鼻子 耳朵 眼睛  胳膊肘  肩膀 脖子 腿 膝盖 等等
 */
public class TensorFlowImageClassifier3 implements Classifier {

    private static final String TAG = "TensorFlowImageClassifier";
    private static Context context_instance;
    private static ImageView imageview_instance;
    private Vector<String> labels = new Vector<String>();
    private TensorFlowInferenceInterface inferenceInterface;
    private static final float NMS_Threshold = (float) 0.15;
    private static final float Local_PAF_Threshold = (float) 0.2;
    private static final int PAF_Count_Threshold = 5;
    private static final int Part_Count_Threshold = 4;
    private static final float Part_Score_Threshold = (float) 4.5;
    private static final int MapHeight = 46;
    private static final int MapWidth = 46;
    private static final int HeatMapCount = 19;
    private static final int MaxPairCount = 19;
    private static final int PafMapCount = 38;
    private static final int MaximumFilterSize = 5;
    private static final int NumPafIter = 10;
    private static final int[][] CocoPairs = {{1, 2}, {1, 5}, {2, 3}, {3, 4}, {5, 6}, {6, 7}, {1, 8}, {8, 9}, {9, 10}, {1, 11}, {11, 12}, {12, 13}, {1, 0}, {0, 14}, {14, 16}, {0, 15}, {15, 17}, {2, 16}, {5, 17}};
    private static final int[][] CocoPairsNetwork = {{12, 13}, {20, 21}, {14, 15}, {16, 17}, {22, 23}, {24, 25}, {0, 1}, {2, 3}, {4, 5}, {6, 7}, {8, 9}, {10, 11}, {28, 29}, {30, 31}, {34, 35}, {32, 33}, {36, 37}, {18, 19}, {26, 27}};
    private static final int[] CocoPartColor = {Color.RED, Color.MAGENTA, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.CYAN, Color.YELLOW, Color.BLUE, Color.BLUE, Color.YELLOW, Color.BLUE, Color.BLUE, Color.RED, Color.RED, Color.RED, Color.RED};
    private static final int DrawScale = 8;
    private String inputName;
    private String outputName;
    private int inputSize_W;
    private int inputSize_H;
    private int imageMean;
    private float imageStd;
    private int[] raw_input_image;
    private float[] rgb_input_image;
    private float[] output_tensor;
    private String[] outputNames;

    private boolean logStats = false;

    public static Classifier create(Context context, ImageView imageview, AssetManager assetManager, String modelFilename, String labelFilename,
                                    int inputSize_W, int inputSize_H, int imageMean, float imageStd, String inputName, String outputName) {
        //实例化
        TensorFlowImageClassifier3 classifier = new TensorFlowImageClassifier3();
        classifier.inputName = inputName;
        classifier.outputName = outputName;
        classifier.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
        classifier.inputSize_W = inputSize_W;
        classifier.inputSize_H = inputSize_H;
        classifier.imageMean = imageMean;
        classifier.imageStd = imageStd;
        classifier.outputNames = new String[]{outputName};
        classifier.raw_input_image = new int[inputSize_W * inputSize_H];
        classifier.rgb_input_image = new float[inputSize_W * inputSize_H * 3];
        classifier.output_tensor = new float[MapHeight * MapWidth * (HeatMapCount + PafMapCount)];
        context_instance = context;
        imageview_instance = imageview;
        return classifier;
    }

    private void change_raw_to_rgb_inplace() {
        //相机读取到的图像解释为三通道位图
        for (int i = 0; i < raw_input_image.length; ++i) {
            final int val = raw_input_image[i];
            rgb_input_image[i * 3 + 2] = (val >> 16); //R
            rgb_input_image[i * 3 + 1] = (val >> 8) ;  //G
            rgb_input_image[i * 3 + 0] = val ;        //B
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // 处理相机图像
        // Preprocess image from camera
        bitmap.getPixels(raw_input_image, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        change_raw_to_rgb_inplace();

        // 将图像输入到tensorflow模型中运算，并将结果放到 outputName 里
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, rgb_input_image, 1, inputSize_W, inputSize_H, 3);
        // Run the inference call.
        inferenceInterface.run(outputNames, logStats);
        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(outputName, output_tensor);

        // 定义一个vector数组存放所有的部位坐标（不同的人一起，相同部位放在一个vector里）
        Vector<int[]> coords[] = new Vector[HeatMapCount - 1];

        // 用最大滤波和非极大值抑制来过滤重复的点，尽量使得某一个人的某一个部位只会被取到一次
        for (int i = 0; i < (HeatMapCount - 1); i++) {
            coords[i] = new Vector<int[]>();
            for (int j = 0; j < MapHeight; j++) {
                for (int k = 0; k < MapWidth; k++) {
                    int[] coord = {j, k};
                    float max_value = 0;
                    for (int dj = -(MaximumFilterSize - 1) / 2; dj < (MaximumFilterSize + 1) / 2; dj++) {
                        if ((j + dj) >= MapHeight || (j + dj) < 0) {
                            break;
                        }
                        for (int dk = -(MaximumFilterSize - 1) / 2; dk < (MaximumFilterSize + 1) / 2; dk++) {
                            if ((k + dk) >= MapWidth || (k + dk) < 0) {
                                break;
                            }
                            float value = output_tensor[(HeatMapCount + PafMapCount) * MapWidth * (j + dj) + (HeatMapCount + PafMapCount) * (k + dk) + i];
                            if (value > max_value) {
                                max_value = value;
                            }
                        }
                    }
                    if (max_value > NMS_Threshold) {
                        if (max_value == output_tensor[(HeatMapCount + PafMapCount) * MapWidth * j + (HeatMapCount + PafMapCount) * k + i]) {
                            coords[i].addElement(coord);
                        }
                    }
                }
            }
        }

        // 用paf算分数，并用贪心法来剔除不合理或者重复的连线
        Vector<int[]> pairs[] = new Vector[MaxPairCount];
        Vector<int[]> pairs_final[] = new Vector[MaxPairCount];
        Vector<Float> pairs_scores[] = new Vector[MaxPairCount];
        Vector<Float> pairs_scores_final[] = new Vector[MaxPairCount];
        for (int i = 0; i < MaxPairCount; i++) {
            pairs[i] = new Vector<int[]>();
            pairs_scores[i] = new Vector<Float>();
            pairs_final[i] = new Vector<int[]>();
            pairs_scores_final[i] = new Vector<Float>();
            Vector<Integer> part_set = new Vector<Integer>();
            for (int p1 = 0; p1 < coords[CocoPairs[i][0]].size(); p1++) {
                for (int p2 = 0; p2 < coords[CocoPairs[i][1]].size(); p2++) {
                    int count = 0;
                    float score = 0.0f;
                    float scores[] = new float[10];
                    int p1x = coords[CocoPairs[i][0]].get(p1)[0];
                    int p1y = coords[CocoPairs[i][0]].get(p1)[1];
                    int p2x = coords[CocoPairs[i][1]].get(p2)[0];
                    int p2y = coords[CocoPairs[i][1]].get(p2)[1];
                    float dx = p2x - p1x;
                    float dy = p2y - p1y;
                    float normVec = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));

                    if (normVec < 0.0001f) {
                        break;
                    }
                    float vx = dx / normVec;
                    float vy = dy / normVec;
                    for (int t = 0; t < 10; t++) {
                        int tx = (int) ((float) p1x + (t * ((float) dx) / 9) + 0.5);
                        int ty = (int) ((float) p1y + (t * ((float) dy) / 9) + 0.5);
                        int location=tx * (HeatMapCount + PafMapCount) * MapWidth + ty * (HeatMapCount + PafMapCount) + HeatMapCount;
                        scores[t] = vy * output_tensor[location + CocoPairsNetwork[i][0]];
                        scores[t] += vx * output_tensor[location + CocoPairsNetwork[i][1]];
                    }
                    for(int h=0;h<10;h++)
                    {
                        if(scores[h]>Local_PAF_Threshold)
                        {
                            count+=1;
                            score+=scores[h];
                        }
                    }
                    if(score>0.0f && count>=PAF_Count_Threshold)
                    {
                        boolean inserted=false;
                        int pair[]={p1,p2};
                        for(int l=0;l<pairs[i].size();l++)
                        {
                            if (score>pairs_scores[i].get(l))
                            {
                                pairs[i].insertElementAt(pair,l);
                                pairs_scores[i].insertElementAt(score,l);
                                inserted=true;
                                break;
                            }
                        }
                        if (!inserted)
                        {
                            pairs[i].addElement(pair);
                            pairs_scores[i].addElement(score);
                        }
                    }
                }
            }
            for (int m=0;m<pairs[i].size();m++)
            {
                boolean conflict=false;
                for(int n=0;n<part_set.size();n++)
                {
                    if (pairs[i].get(m)[0] == part_set.get(n) || pairs[i].get(m)[1] == part_set.get(n))
                    {
                        conflict=true;
                        break;
                    }
                }
                if (!conflict)
                {
                    pairs_final[i].addElement(pairs[i].get(m));
                    pairs_scores_final[i].addElement(pairs_scores[i].get(m));
                    part_set.addElement(pairs[i].get(m)[0]);
                    part_set.addElement(pairs[i].get(m)[1]);
                }
            }
        }

        // 得到所有的连线集合后，用并查集算法，尽可能合并所有的连线，无法合并的多个部分即为多个人
        class Human
        {
            /*Nose = 0
            Neck = 1
            RShoulder = 2
            RElbow = 3
            RWrist = 4
            LShoulder = 5
            LElbow = 6
            LWrist = 7
            RHip = 8
            RKnee = 9
            RAnkle = 10
            LHip = 11
            LKnee = 12
            LAnkle = 13
            REye = 14
            LEye = 15
            REar = 16
            LEar = 17*/
            public int parts_coords[][]=new int[18][2];
            // not important
            public int part_count=0;
            public int coords_index_set[]=new int[18];
            public boolean coords_index_asigned[]=new boolean[18];
        }

        Vector<Human> humans=new Vector<Human>();
        Vector<Human> humans_final=new Vector<Human>();
        for(int i=0;i<MaxPairCount;i++){
            for(int j=0;j<pairs_final[i].size();j++)
            {
                boolean merged=false;
                int p1=CocoPairs[i][0];
                int p2=CocoPairs[i][1];
                int ip1=pairs_final[i].get(j)[0];
                int ip2=pairs_final[i].get(j)[1];
                for(int k=0;k<humans.size();k++)
                {
                    Human human=humans.get(k);
                    if((ip1 == human.coords_index_set[p1] && human.coords_index_asigned[p1]) || (ip2 == human.coords_index_set[p2] && human.coords_index_asigned[p2]))
                    {
                        human.parts_coords[p1]=coords[p1].get(ip1);
                        human.parts_coords[p2]=coords[p2].get(ip2);
                        human.coords_index_set[p1]=ip1;
                        human.coords_index_set[p2]=ip2;
                        human.coords_index_asigned[p1]=true;
                        human.coords_index_asigned[p2]=true;
                        merged=true;
                        break;
                    }
                }
                if(!merged)
                {
                    Human human=new Human();
                    human.parts_coords[p1]=coords[p1].get(ip1);
                    human.parts_coords[p2]=coords[p2].get(ip2);
                    human.coords_index_set[p1]=ip1;
                    human.coords_index_set[p2]=ip2;
                    human.coords_index_asigned[p1]=true;
                    human.coords_index_asigned[p2]=true;
                    humans.addElement(human);
                }
            }
        }


        // 去掉部位数量过少的人
        for(int i=0;i<humans.size();i++)
        {
            int human_part_count=0;
            for(int j=0;j<HeatMapCount-1;j++)
            {
                if(humans.get(i).coords_index_asigned[j])
                {
                    human_part_count+=1;
                }
            }
            if (human_part_count>Part_Count_Threshold)
            {
                humans_final.addElement(humans.get(i));
            }
        }

        // 这里获得所有的人 保存在humans_final vector数组里，每一个human类都有一个长度为部位个数（18）的parts属性，第一个维度代表部位，解释在human类里，第二个维度是该部位的坐标。

        Log.v("Number of Human in Screen",String.valueOf(humans_final.size()));

        // 画出所有的人
        Bitmap bitmap_result = Bitmap.createBitmap(46*DrawScale, 46*DrawScale, Bitmap.Config.ARGB_8888);


        Canvas canvas = new Canvas(bitmap_result);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);  //线的宽度
        for (int i = 0; i < (HeatMapCount - 1); i++) {
            paint.setColor(CocoPartColor[i]);
            for (int j = 0; j < coords[i].size(); j++) {
                canvas.drawCircle(coords[i].get(j)[1]*DrawScale, coords[i].get(j)[0]*DrawScale,3, paint);
            }
        }

        for (int i = 0; i < MaxPairCount; i++) {
            paint.setColor(Color.GREEN);
            for (int j = 0; j < pairs_final[i].size(); j++) {
                int x1=coords[CocoPairs[i][0]].get(pairs_final[i].get(j)[0])[1]*DrawScale;
                int y1=coords[CocoPairs[i][0]].get(pairs_final[i].get(j)[0])[0]*DrawScale;
                int x2=coords[CocoPairs[i][1]].get(pairs_final[i].get(j)[1])[1]*DrawScale;
                int y2=coords[CocoPairs[i][1]].get(pairs_final[i].get(j)[1])[0]*DrawScale;
                canvas.drawLine(x1,y1,x2,y2,paint);
            }
        }

        if (imageview_instance != null) {
            imageview_instance.post(new Runnable() {
                @Override
                public void run() {
                    imageview_instance.setImageBitmap(bitmap_result);
                }
            });

        }

        return null;
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
