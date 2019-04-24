本Demo 是为了在android上跑TensorFlow模型的
方便那些想把TensorFlow官网上的demo集成到自己项目里却又找不到头绪的人使用

正所谓前人栽树后人乘凉

[https://github.com/tensorflow/tensorflow](https://github.com/tensorflow/tensorflow)

[https://github.com/ildoonet/tf-pose-estimation](https://github.com/ildoonet/tf-pose-estimation)

Denis Tome, Chris Russell, Lourdes Agapito提出的Convolutional 3D Pose Estimation from a Single Image论文

在此特别感谢上述作者，喜欢原作的可以去使用原项目。同时欢迎大家下载体验本项目，如果使用过程中遇到什么问题，欢迎反馈。

如果需要自动打标请使用：[https://github.com/yuxitong/AutoMarKingTensorFlowPython](https://github.com/yuxitong/AutoMarKingTensorFlowPython)

另外特别感谢

	[zyxcambridge](https://github.com/zyxcambridge) 童鞋
	[manoshape](https://github.com/manoshape) 童鞋
	[seriouslyhao](https://github.com/seriouslyhao) 童鞋
        	本项目模型由zyxcambridge、manoshape、seriouslyhao提供

本项目使用的是Camera2的api

过段时间我还会陆续公布一些更好玩的模型

如果想要体验本项目，请直接下载apk：[点击下载](https://github.com/yuxitong/TensorFlowAndroidDemo/raw/master/apk/app-debug.apk)

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
         


目前人体骨架识别有了新突破：具体算法已经有了，但是暂时没工夫写。过段时间在上线。（把所有点连接成一个一个的人体）

具体算法如下：
 
W*H*19每一层的解释

             Nose = 0
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
             LEar = 17
             Background = 18
             
需要连线的身体部位，共19个连线

        CocoPairs = [
            (1, 2), (1, 5), (2, 3), (3, 4), (5, 6), (6, 7), (1, 8), (8, 9), (9, 10), (1, 11),
            (11, 12), (12, 13), (1, 0), (0, 14), (14, 16), (0, 15), (15, 17), (2, 16), (5, 17)
        ]   # = 19
             
对应每一个连线到 W*H*38 里找到两页取两组10个点

            CocoPairsNetwork = [
                (12, 13), (20, 21), (14, 15), (16, 17), (22, 23), (24, 25), (0, 1), (2, 3), (4, 5),
                (6, 7), (8, 9), (10, 11), (28, 29), (30, 31), (34, 35), (32, 33), (36, 37), (18, 19), (26, 27)
             ]  # = 19
从头开始按照顺序连接 到脖子 到右臂等等

        求出Dx = x1 - x2
            Dy = y1 - y2
            
计算两点间距离公式

        k = 根号下（Dx平方-Dy平方）

如果两点间距离小于0.0001 则分数为0 个数为0

计算    Vx = Dx / k
        Vy = Dy / k

连线的过程中每个线均分10个点  分出2个数组 分别是X数组 Y数组 每个数组都是10个数字
均分公式如下

        for(i = x1; i <= x2; i += dx/10)
                x[n] = i;
        Y同理
        
计算每个连线的分数：
        CocoPairs 与 CocoPairsNetwork 一一对应
        每一组对应的CocoPairs连线方式可以在CocoPairsNetwork里取出2层
        （如：1和2之间连线 可以在12层和13层里分别取出10个均等值）
        并把均等值存到2个新的长度为10的组里
        Px[10] 和 Py[10]
        
建立一个分数数组 = Px[10]乘Vx与Py[10]乘Vy对应相加
        
        分数>0.2的个数乘 >0.2的分数相加 得到最终分数，并且保留有多少个>0.2的
        之前两点间距离小于0.0001分数为0的个数也为0
        
如果个数小于5或者分数小于等于0则这个连线不要

从分数最高的开始遍历所有要的连线放到最终连线的集合（放的时候任何与这个连线集合的点重复都直接舍弃连线）

重复以上所有算法直到没点可连

然后拿出所有连线的集合 找出相同点的连线连接 就出现了人体骨架

![image](https://github.com/yuxitong/TensorFlowAndroidDemo/blob/master/image/face.gif)  ![image](https://github.com/yuxitong/TensorFlowAndroidDemo/blob/master/image/road.gif)  ![image](https://github.com/yuxitong/TensorFlowDemo/blob/master/image/body.gif)

![image](https://github.com/yuxitong/TensorFlowAndroidDemo/blob/master/image/carAndLine.gif)