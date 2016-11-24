package com.jiyiren.jysort;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jiyiren.jysort.atys.BaseCompatActivity;
import com.jiyiren.jysort.utils.DensityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends BaseCompatActivity {

    //下面四个int为Handler处理Message时的区别标志
    private static final int WHAT_COUNT_TIME=0x1;        //开始计时标志
    private static final int WHAT_SORT_SWAP_VIEW=0x2;    //交换数据时请求界面View交换高度标志
    private static final int WHAT_SORT_NOT_SWAP_VIEW=0x3;//单项赋值时请求界面View设置高度值标志
    private static final int WHAT_SORT_FINISH=0x4;       //停止计时标志

    //下面两个值是为界面便于根据数组大小动态设置每个柱状View宽度定义的
    public static final int paddingLR=6;     //单位：dp 这个是柱状View外层LinearLayout的左右padding大小
    public static final int intervalColumn=1;//单位：dp 这个是每个柱状View相互间的间隔

    //这个是数组的大小
    private static final int SIEZ_ARRAY=100;

    //这个为当前的排序方法类型
    int currentSortKind=0;//0->快速排序(默认)，1->堆排序，2->归并排序,3->插入排序，4->冒泡排序,5->选择排序

    //数组，包括柱状View数组，以及两个int数组
    List<View> mViews=new ArrayList<View>();
    int[] mArray=new int[SIEZ_ARRAY];
    int[] mBackArray=new int[SIEZ_ARRAY];

    //界面控件
    LinearLayout ll_root=null,ll_sortpart=null;//ll_root为根布局控件，ll_sortpart为柱状图外包裹的布局控件
    Toolbar toolbar=null;                      //菜单栏
    int screenWidth=0,screenHeight=0;          //单位：px，屏幕的宽、高
    int columnWidth=0;                         //单位：px，柱状View的宽度,计算一次用全局变量存储下次就不需要再计算了
    double columnPixPerNum=0.0;                //单位：px/1  这个是高度上单位数字所表示的像素。
    TextView[] tv_time_complex=new TextView[5];

    //下面为计时器用到的变量以及控件
    TextView tvTime;
    private int mlCount;          //总时间，是精确到毫秒的整数
    protected int sec;            //秒级数，对mlCount/1000所得总秒数再取60余数
    protected String time;        //这个是最终显示的时间格式
    private Timer timer = null;   //计时器
    private TimerTask task = null;//计时器线程
    private Message msg = null;   //计时器通知消息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolBar();

        //这是获得屏幕宽高
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth=dm.widthPixels;
        screenHeight=dm.heightPixels;

        initControls();
        randomData();
        initView();
    }

    //初始化各个控件
    private void initControls(){
        tvTime= (TextView) findViewById(R.id.timeshow);
        tv_time_complex[0]= (TextView) findViewById(R.id.tv_best_time);
        tv_time_complex[1]= (TextView) findViewById(R.id.tv_average_time);
        tv_time_complex[2]= (TextView) findViewById(R.id.tv_worst_time);
        tv_time_complex[3]= (TextView) findViewById(R.id.tv_extra_space);
        tv_time_complex[4]= (TextView) findViewById(R.id.tv_stability);
        selectTimeComplex(0);
    }

    //随机生成数组，并存在两个数组里，一个为重置时的备份数据
    private void randomData(){
        Random random=new Random();
        for(int i=0;i<SIEZ_ARRAY;i++){
            mBackArray[i]=mArray[i]=random.nextInt(200)+10;//10-200
        }
//        arrayToString("arraybefore");//输出原始数组
    }


    //初始化ToolBar菜单栏
    public void initToolBar(){
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.fast_sort);
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.black));
        toolbar.setNavigationContentDescription(R.string.sort);

        toolbar.inflateMenu(R.menu.main_menu_layout);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_reset://重置
                        resetView();
                        break;
                    case R.id.menu_sort://排序
                        if(mlCount!=0){
                            Toast.makeText(MainActivity.this,R.string.sorted, Toast.LENGTH_SHORT);
                        }else{
                            start();
                            new SortThread().start();
                        }
                        break;
                    case R.id.fast_sort:
                        currentSortKind=0;
                        selectTimeComplex(0);
                        resetView();
                        toolbar.setTitle(R.string.fast_sort);
                        break;
                    case R.id.heap_sort:
                        currentSortKind=1;
                        selectTimeComplex(1);
                        resetView();
                        toolbar.setTitle(R.string.heap_sort);
                        break;
                    case R.id.merge_sort:
                        currentSortKind=2;
                        selectTimeComplex(2);
                        resetView();
                        toolbar.setTitle(R.string.merge_sort);
                        break;
                    case R.id.insert_sort:
                        currentSortKind=3;
                        selectTimeComplex(3);
                        resetView();
                        toolbar.setTitle(R.string.insert_sort);
                        break;
                    case R.id.bubble_sort:
                        currentSortKind=4;
                        selectTimeComplex(4);
                        resetView();
                        toolbar.setTitle(R.string.bubble_sort);
                        break;
                    case R.id.select_sort:
                        currentSortKind=5;
                        selectTimeComplex(5);
                        resetView();
                        toolbar.setTitle(R.string.select_sort);
                        break;
                }
                return true;
            }
        });

    }

    //初始化界面的柱状图
    private void initView() {
        if(ll_root==null){
            ll_root= (LinearLayout) findViewById(R.id.ll_root);
        }
        if(ll_sortpart==null) {
            ll_sortpart = (LinearLayout) ll_root.findViewById(R.id.ll_sortpart);
        }
        if(mViews.size()<=0) {
            for (int i = 0; i < mArray.length; i++) {
                View view = new View(this);
                ll_sortpart.addView(view);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                columnWidth = (screenWidth - DensityUtil.dp2px(this, paddingLR * 2)) / mArray.length
                            -DensityUtil.dp2px(this, intervalColumn);
                layoutParams.setMargins(DensityUtil.dp2px(this, intervalColumn), 0, 0, 0);
                layoutParams.height = (int) (mArray[i] * pixPerNum());
                layoutParams.width = columnWidth;
                view.setLayoutParams(layoutParams);
                view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
                mViews.add(view);
            }
        }
    }

    //重置方法，包括数组、界面、计时器等都要重置为原来数据形态
    private void resetView(){
        //不等于0时才进行重置
        if(mlCount!=0) {
            if (mViews.size() == mBackArray.length) {
                for (int i = 0; i < mBackArray.length; i++) {
                    mArray[i]=mBackArray[i];
                    View view = mViews.get(i);
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.height = (int) (mBackArray[i] * columnPixPerNum);
                    view.setLayoutParams(layoutParams);
                }
            }
            mlCount = 0;
            tvTime.setText(R.string.time_default);
        }
    }


    //获得在高度上，单位数字所代表的像素，由于屏幕高度是像素，而我们的排序为int数字，要想形象化绘制成柱状图
    //就要计算出单位数字的像素，然后通过数组中的数字相乘即可得到柱状view的高度了
    private double pixPerNum(){
        columnPixPerNum=(double) screenHeight*0.6/(Max(mArray));
        return columnPixPerNum;
    }

    //获得数组中最大数字，仅仅用于@pixPerNum方法中
    private int Max(int array[]){
        int max=0;
        for(int i=0;i<array.length;i++){
            if(max<array[i]){
                max=array[i];
            }
        }
        return max;
    }

    /**
     * Handler，与主线程交互处理
     */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            switch (msg.what) {
                case WHAT_COUNT_TIME:
                    mlCount++;
                    int totalSec = 0;
                    int yushu = 0;

                    // 精确到0.001秒
                    totalSec = (int) (mlCount / 1000);
                    yushu = (int) (mlCount % 1000);

                    // 计算分、秒
                    int min = (totalSec / 60);
                    sec = (totalSec % 60);
                    // 格式化算出来的时间为String格式“00:00:000”
                    //%n$md：代表输出的是整数，n代表是第几个参数，设置m的值可以在输出之前放置空格，也可以设为0m,在输出之前放置m个0
                    time = String.format("%1$02d:%2$02d:%3$03d", min,sec, yushu);
                    tvTime.setText(time);
                    break;
                case WHAT_SORT_SWAP_VIEW:
                    //排序中存在两数交换情况，则调用View交换高度方法
                    swapColumHeight(mViews.get(msg.arg1),mViews.get(msg.arg2));
                    break;
                case WHAT_SORT_NOT_SWAP_VIEW:
                    //排序中仅仅是单项赋值，则调用View设置高度方法
                    setViewHeight(mViews.get(msg.arg1),msg.arg2);
                    break;
                case WHAT_SORT_FINISH:
                    //在排序算法结束后停止计时器
                    stop();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    //交换两个View的高度，这是在排序算法中存在两个值互换的情况下使用的界面直接绘制两个View的相互高度
    private void swapColumHeight(View viewone,View viewtwo){
        LinearLayout.LayoutParams paramsone= (LinearLayout.LayoutParams) viewone.getLayoutParams();
        LinearLayout.LayoutParams paramstwo= (LinearLayout.LayoutParams) viewtwo.getLayoutParams();

        int temp=paramsone.height;
        paramsone.height=paramstwo.height;
        paramstwo.height=temp;
        viewone.setLayoutParams(paramsone);
        viewtwo.setLayoutParams(paramstwo);
    }
    //设置控件高度，因为有的排序算法不是两个值交换的，因此当只有在单向赋值时，可以用此方法通知界面重绘柱形高度
    private void setViewHeight(View view,int height){
        LinearLayout.LayoutParams params= (LinearLayout.LayoutParams) view.getLayoutParams();
        params.height=(int)(height*columnPixPerNum);
        view.setLayoutParams(params);
    }


    /**
     * 计时器开始计时
     */
    public void start() {
        if (null == timer) {
            if (null == task) {
                task = new TimerTask() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        if (null == msg) {
                            msg = new Message();
                        } else {
                            msg = Message.obtain();
                        }
                        msg.what = WHAT_COUNT_TIME;
                        handler.sendMessage(msg);
                    }
                };
            }
            timer = new Timer(true);
            // set timer duration每隔0.001秒刷新一次
            timer.schedule(task, 1, 1);
        }
    }

    /**
     * 计时器停止
     */
    public void stop() {
        task.cancel();
        task = null;
        timer.cancel(); // Cancel timer
        timer.purge();
        timer = null;
        handler.removeMessages(msg.what);
    }

    /**
     * 排序线程类
     */
    class SortThread extends Thread{
        @Override
        public void run() {
            switch (currentSortKind){
                case 0://快速
                    quicksort(mArray,0,mArray.length-1);
                    break;
                case 1://堆
                    HeadSort(mArray,mArray.length);
                    break;
                case 2://归并
                    int[] mtemp=new int[SIEZ_ARRAY];
                    MergeSort(mArray,mtemp,0,mArray.length-1);
                    break;
                case 3://插入
                    insertsort(mArray,0,mArray.length-1);
                    break;
                case 4://冒泡
                    bubblesort(mArray,0,mArray.length-1);
                    break;
                case 5://选择
                    selectsort(mArray,0,mArray.length-1);
                    break;
            }
//            arrayToString("arrayafter");
            handler.sendEmptyMessage(WHAT_SORT_FINISH);
        }
    }

    //交换的信号
    private void sleepAndSendMessage(int i,int j){
        try {
            Thread.sleep(10);
            Message message=new Message();
            message.what=WHAT_SORT_SWAP_VIEW;
            message.arg1=i;
            message.arg2=j;
            handler.sendMessage(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //非交换数据
    private void sleepAndSetViewHeight(int i,int height,boolean issleep){
        try {
            if(issleep) {
                Thread.sleep(10);
            }
            Message message=new Message();
            message.what=WHAT_SORT_NOT_SWAP_VIEW;
            message.arg1=i;
            message.arg2=height;
            handler.sendMessage(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //设置各个算法的时间复杂度
    private void selectTimeComplex(int choice){
        List<String> result =new ArrayList<String>();
        switch (choice){
            case 0:
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.n2));
                result.add(getString(R.string.logn));
                result.add(getString(R.string.unstable));
                break;
            case 1:
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.one));
                result.add(getString(R.string.unstable));
                break;
            case 2:
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.nlogn));
                result.add(getString(R.string.n));
                result.add(getString(R.string.stable));
                break;
            case 3:
            case 4:
                result.add(getString(R.string.n));
                result.add(getString(R.string.n2));
                result.add(getString(R.string.n2));
                result.add(getString(R.string.one));
                result.add(getString(R.string.stable));
                break;
            case 5:
                result.add(getString(R.string.n2));
                result.add(getString(R.string.n2));
                result.add(getString(R.string.n2));
                result.add(getString(R.string.one));
                result.add(getString(R.string.unstable));
                break;
        }
        for(int i=0;i<result.size();i++){
            tv_time_complex[i].setText(result.get(i));
        }
    }

    /**=====================================排序算法===========================================**/

    /**
     * 1快速排序
     * @param array
     * @param startIndex
     * @param endIndex
     */
    void quicksort(int array[],int startIndex,int endIndex){
        if(startIndex<endIndex){
            int i=startIndex,j=endIndex,x=array[startIndex];
            while(i<j){
                while(i<j&&array[j]>=x){
                    j--;
                }
                if(i<j){
                    array[i]=array[j];
                    sleepAndSetViewHeight(i,array[j],false);
                    i++;
                }
                while(i<j&&array[i]<x){
                    i++;
                }
                if(i<j){
                    array[j]=array[i];
                    sleepAndSetViewHeight(j,array[i],false);
                    j--;
                }
                array[i]=x;
                sleepAndSetViewHeight(i,x,true);
            }
            quicksort(array,startIndex,i-1);
            quicksort(array,i+1,endIndex);
        }
    }

    /**
     * 堆排序--分堆操作
     * @param array
     * @param i
     * @param nLength
     */
    void HeapAdjust(int array[],int i,int nLength){
        int nChild;
        int nTemp;
        for(;2*i+1<nLength;i=nChild){
            //子节点的位置=2*(父节点位置)+1
            nChild=2*i+1;
            //得到子节点中较大的节点
            if(nChild<nLength-1&&array[nChild+1]>array[nChild]){
                ++nChild;
            }
            //如果较大的子节点大于父节点那么把较大的子节点往上移动，替换它的父节点
            if(array[i]<array[nChild]){
                nTemp=array[i];
                array[i]=array[nChild];
                array[nChild]=nTemp;
                sleepAndSendMessage(i,nChild);
            }else{
                break;//否则退出循环
            }

        }
    }

    /**
     * 2堆排序
     * @param array
     * @param length
     */
    void HeadSort(int array[],int length){
        int i,temp;
        //调整序列的前半部分元素，调整完之后第一个元素是序列的最大元素
        //length/2-1是最后一个非叶节点，（length为下标加1）
        for(i=length/2-1;i>=0;--i){
            HeapAdjust(array,i,length);
        }

        //从最后一个元素开始对序列进行调整，不断缩小调整的范围直到第一个元素
        for(i=length-1;i>0;--i){
            temp=array[0];
            array[0]=array[i];
            array[i]=temp;
            sleepAndSendMessage(0,i);
            //不断缩小调整heap的范围，每一次调整完毕保证第一个元素是当前序列的最大值
            HeapAdjust(array,0,i);
        }

    }

    /**
     * 归并排序--归并操作
     * @param sourceArr
     * @param tempArr
     * @param startIndex
     * @param midIndex
     * @param endIndex
     */
    void Merge(int sourceArr[],int tempArr[],int startIndex,int midIndex,int endIndex){
        int i=startIndex,j=midIndex+1,k=startIndex;
        //分别从两个序列的起始开始，逐一比较两个序列中元素大小
        while(i!=midIndex+1 && j!=endIndex+1){
            if(sourceArr[i]>sourceArr[j]){
                //较小的存入合并序列
                tempArr[k++]=sourceArr[j++];
            }else{
                //较小的存入合并序列
                tempArr[k++]=sourceArr[i++];
            }
        }
        //将未合并的直接放到合并序列里
        while(i!=midIndex+1){
            tempArr[k++]=sourceArr[i++];
        }
        while(j!=endIndex+1){
            tempArr[k++]=sourceArr[j++];
        }
        //最后将合并序列在放入原始数列中
        for(i=startIndex;i<=endIndex;i++){
            sourceArr[i]=tempArr[i];
            sleepAndSetViewHeight(i,tempArr[i],true);
        }
    }

    /**
     * 3归并排序
     * @param sourceArr
     * @param tempArr
     * @param startIndex
     * @param endIndex
     */
    //sourceArr为原待排序列，tempArr为合并后序列，作为临时存放区
    //startIndex为数组开始下标，endIndex为结束下标
    void MergeSort(int sourceArr[],int tempArr[],int startIndex,int endIndex){
        int midIndex;
        if(startIndex<endIndex){
            //将原序列二分为两个序列
            midIndex=(startIndex+endIndex)/2;
            //分别对两个序列，再进行二分
            MergeSort(sourceArr,tempArr,startIndex,midIndex);
            MergeSort(sourceArr,tempArr,midIndex+1,endIndex);
            //对每个二分序列进行合并
            Merge(sourceArr,tempArr,startIndex,midIndex,endIndex);
        }
    }

    /**
     * 4插入排序
     * @param array
     * @param startIndex
     * @param endIndex
     */
    void insertsort(int array[],int startIndex,int endIndex){
        int i,j,item;
        if(endIndex>startIndex){
            for(j=startIndex+1;j<=endIndex;j++){
                item=array[j];
                i=j-1;
                while(i>=startIndex&&item<array[i]&&i>=startIndex){
                    array[i+1]=array[i];//将比item大的书依次后移
                    sleepAndSetViewHeight(i+1,array[i],false);
                    i--;
                }
                array[i+1]=item;
                sleepAndSetViewHeight(i+1,item,true);
            }
        }
    }

    /**
     * 5冒泡排序
     * @param array
     * @param startIndex
     * @param endIndex
     */
    void bubblesort(int array[],int startIndex,int endIndex){
        int i,j,t;
        for(i=startIndex;i<endIndex;i++){
            //逆序比较，就需要将最小值放在最前面
            for(j=endIndex;j>i;j--){
                if(array[j]<array[j-1]){
                    t=array[j];
                    array[j]=array[j-1];
                    array[j-1]=t;
                    sleepAndSendMessage(j,j-1);
                }
            }
        }
    }

    /**
     * 6选择排序
     * @param array
     * @param startIndex
     * @param endIndex
     */
    void selectsort(int array[],int startIndex,int endIndex){
        int i,j,minIndex;
        for(i=startIndex;i<endIndex;i++){
            minIndex=i;
            //选出最小下标，放在最前面
            for(j=i+1;j<=endIndex;j++){
                if(array[minIndex]>array[j]){
                    minIndex=j;
                }
            }
            if(minIndex!=i){
                int temp=array[i];
                array[i]=array[minIndex];
                array[minIndex]=temp;
                sleepAndSendMessage(i,minIndex);
            }
        }
    }

    /**
     * 输出数组
     * @param tag
     */
    private void arrayToString(String tag){
        String result="";
        for(int i=0;i<mArray.length;i++){
            result=result+" "+mArray[i];
            if(i%5==0){
                result=result+"\n";
            }
        }
        Log.i("jiyiren",tag+result);
    }


}
