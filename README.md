# JYSort

**详情请查看这篇**：[http://jiyiren.github.io/2016/11/24/android_sort/](http://jiyiren.github.io/2016/11/24/android_sort/)

**C语言实现各个排序算法**: [https://github.com/jiyiren/CSort](https://github.com/jiyiren/CSort)

> Android图形化展示排序算法

* 快速排序
* 堆排序
* 归并排序
* 插入排序
* 冒泡排序
* 选择排序

## 界面

![main](http://7xknpe.com1.z0.glb.clouddn.com/jysorthome360.png)
![maingif](http://7xknpe.com1.z0.glb.clouddn.com/jysortwelcome.gif)


## 设计思路

### 数据与界面的初始化

1. 数据是随机生成100个数组成一个数组，当然这个长度我们定义成全局变量，可以自行修改。
2. 界面的初始化由上到下分别为Toolbar菜单栏、主体排序可视窗口、时间复杂度等。
3. 重点在于中间主体排序可视窗口的绘制：因为可视的View就是要表示数组中各个数据的大小，因而我们就将每个柱状View的高度用于表示数组中各个数据的大小，但是由于手机界面有限，如果有的数据过大那么绘制将超出屏幕。因此我们采取将3/5个屏幕像素与数组中最大值的**比值**作为每个数转为高度的一个因素，也就是说数组中数据不管多大，其高度最高最大为3/5个屏幕大小。而其宽度则是根据数组长度由屏幕宽度计算得出

### 排序同时界面更新

1. 界面的中每个柱状View与数组一一对应，这样我们只需要记住排序数组的下标就可以同步View数组了。
2. 排序是耗时操作，我们需要开启线程进行排序，因此需要用到消息传递来通知界面的变化，这里主要使用**Handler**来进行处理线程消息。
3. 整个流程为：在开启排序时，算法中的数组在进行数据交换时，我们会记录此时交换数据的两个下标，同时将这些数据包装成消息发送给**Handler**,**Handler**将界面柱状View数组中两个相同下标的View高度互换，达到界面显示与排序一致的效果。

### 排序延迟操作

* 由于排序算法只有在对数以万计的数据时才会有可见的时间长度，因而我们如果像正常一样设置排序和界面更新时，每个排序算法都几乎在毫秒级别完成，并且界面变化几乎不可见。
* 因此，我们需要让排序算法尽可能地延长一定时间，达到界面的变化可视化级别。
* 我们在数组交换数据时发送消息给**Handler**处进行了一定的时间延迟，当然不会过长，此处设置了10ms。(也同时因为这样的设置，界面显示的耗时操作实际上并非算法真正的排序时间，而应该减去延迟时间乘以延迟操作的次数。)


## 快速排序

![fast](http://7xknpe.com1.z0.glb.clouddn.com/jysortfast.gif)

## 堆排序

![heap](http://7xknpe.com1.z0.glb.clouddn.com/jysortheap.gif)

## 归并排序

![merge](http://7xknpe.com1.z0.glb.clouddn.com/jysortmerge.gif)

## 插入排序

![insert](http://7xknpe.com1.z0.glb.clouddn.com/jysortinsert.gif)

## 冒泡排序

![bubble](http://7xknpe.com1.z0.glb.clouddn.com/jysortbubble.gif)

## 选择排序

![select](http://7xknpe.com1.z0.glb.clouddn.com/jysortselect.gif)

## 关于

* [About Me](http://jiyiren.github.io/about/)
* [我的博客](http://jiyiren.github.io/)
* [新浪微博](http://weibo.com/jiyi1459050189)
