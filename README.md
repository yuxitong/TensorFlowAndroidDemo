本Demo 是为了在android上跑TensorFlow模型的
方便那些想把TensorFlow官网上的demo集成到自己项目里却又找不到头绪的人使用

本项目使用的是Camera2的api

过段时间我还会陆续公布一些更好玩的模型


目前本Demo模型能识别出 抽烟 打电话 闭眼 睁眼

TensorFlowObjectDetectionAPIModel 为检测规则画框

TensorFlowImageClassifier2   为车道检测之后不规则绘制（因时间仓促 还没有进行绘图优化）
识别道路的测试方法请自行百度寻找图片或者视频都可以


接下来准备上线人体识别或者道路障碍物识别...

![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/face.gif) ![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/road.gif)