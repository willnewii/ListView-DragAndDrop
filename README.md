ListView-DragAndDrop
====================
基于 https://github.com/teslacoil/Shared_DragAndDrop 项目做了一些功能上的添加.

原项目实现了ListView的拖动排序功能.
在此基础上添加了
1.单选/多选 功能.

2.手势滑动删除.

![Screenshot](https://github.com/willnewii/ListView-DragAndDrop/blob/master/Screenshot.jpg?raw=true)

其实这些功能都比较零散,只是那些天碰巧把这些功能都在这个项目里做了实现.
可以适当选择使用.


###问题###
滑动删除,对手势的识别不太理想.我将这些值都调到尽可能小,只是实现区分的作用.可能根据项目情况,再进行调节.

``` java
DisplayMetrics dm = getResources().getDisplayMetrics();
//Y轴最大偏移量.比对该值,太偏,忽略该滑动动作.
REL_SWIPE_MAX_OFF_PATH = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
//X轴最小偏移量.比对该值,太小,忽略该滑动动作.
REL_SWIPE_MIN_DISTANCE = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
//X轴移动速率.比对该值,太慢,忽略该滑动动作.
REL_SWIPE_THRESHOLD_VELOCITY = (int) (25.0f * dm.densityDpi / 160.0f + 0.5);
```


