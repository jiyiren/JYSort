# JYSort

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

排序算法无需多说，主要说明界面的设计以及与排序算法的对应：

1. **柱状View绘制**:排序数据数组是随机生成的，我们需要将它们与界面各个柱状View对应起来，我们的柱状View采用**View类加设置背景色完成**，每个柱状View之间设置一定宽度间隔，柱状View的宽度根据屏幕宽度动态设置(便于数组大小的变换)。而柱状View的高则是根据屏幕的高度与数组中最大值的**比值**×**数组中的每个值**取得。初始界面设置完成。

2. **排序时对应界面变化**：在算法排序时，我们抓住**数组值交换时刻**，进行对应的界面层柱状View的**高度互换**设置，让数组排序过程得以展现在界面上。

3. **延迟设置**：我们默认生成100个数字进行排序，如果我们不设置延迟操作，整个界面几乎是毫秒级就排序好了，完全看不到过程的！因此要进行相应的延迟操作(所以本项目中的算法之间时间的对比可能存在误差，因为这涉及到延迟操作调用的次数)。本项目也就是在数组数字交换的瞬间进行10毫秒的延迟，这样给予界面较长的动态过程展示。


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
