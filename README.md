本Demo 是为了在android上跑TensorFlow模型的
方便那些想把TensorFlow官网上的demo集成到自己项目里却又找不到头绪的人使用

正所谓前人栽树后人乘凉
[https://github.com/tensorflow/tensorflow](https://github.com/tensorflow/tensorflow)
[https://github.com/ildoonet/tf-pose-estimation](https://github.com/ildoonet/tf-pose-estimation)
Denis Tome, Chris Russell, Lourdes Agapito提出的Convolutional 3D Pose Estimation from a Single Image论文

在此特别感谢上述作者，喜欢原作的可以去使用原项目。同时欢迎大家下载体验本项目，如果使用过程中遇到什么问题，欢迎反馈。


本项目使用的是Camera2的api

过段时间我还会陆续公布一些更好玩的模型


目前本Demo模型能识别出 抽烟 打电话 闭眼 睁眼

TensorFlowObjectDetectionAPIModel 为检测规则画框

TensorFlowImageClassifier2   为车道检测之后不规则绘制（因时间仓促 还没有进行绘图优化）
识别道路的测试方法请自行百度寻找图片或者视频都可以

TensorFlowImageClassifier3  是用来识别人体骨架的  这个模型是有特定输入和特定输出的  需要经过3层转换 才能使用
接下来准备上线道路障碍物识别...

![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/face.gif) ![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/road.gif)![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/body.gif)