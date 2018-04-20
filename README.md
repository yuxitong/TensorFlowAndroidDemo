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
最新版骨架识别目前支持区分各个身体部位具体情况请看注释

Camera2BasicFragment4
这是一个用检测来识别车道和前车
里面增加了点逻辑来判断是否是车道偏离或者前车过近
具体做法是
如果检测出线则判断斜率k = (y2-y1)/(x2-x1)然后设定一个固定斜率来判断是否是车道偏离
如果是检测出前面的车辆中心点在横屏8分之2到8分之6的范围内则判断中心点居上距离大于一定范围则算前车过近
         或者如果车的高度大于一定级别则算前车过近
         
另外： 有人私下问我本项目在他们的手机上跑起来卡顿严重
       这是算力的问题，目前tensorFlow在移动设备上貌似不支持GPU，而CPU的浮点运算速度比较慢导致的
       推荐使用华为P10 或者 骁龙845 635之类的U来跑跑看 
       一般P10的话 1能一秒4帧  2能1秒8帧   3能一秒1帧  4能一秒6帧左右
       当然以上数据仅供产考
         
         
         
![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/face.gif)  ![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/road.gif)  ![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/body.gif)

![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/carAndLine.gif)