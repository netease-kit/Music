//
//  NTESBasicDefine.h
//  NIMSolutionTemplate
//
//  Created by chris on 2018/1/8.
//  Copyright © 2018年 chris. All rights reserved.
//

#ifndef NTESBasicDefine_h
#define NTESBasicDefine_h


#define SuppressPerformSelectorLeakWarning(Stuff) \
do { \
_Pragma("clang diagnostic push") \
_Pragma("clang diagnostic ignored \"-Warc-performSelector-leaks\"") \
Stuff; \
_Pragma("clang diagnostic pop") \
} while (0)


#pragma mark - UIColor宏定义
#define UIColorFromRGBA(rgbValue, alphaValue) [UIColor \
colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
green:((float)((rgbValue & 0x00FF00) >> 8))/255.0 \
blue:((float)(rgbValue & 0x0000FF))/255.0 \
alpha:alphaValue]

#define UIColorFromRGB(rgbValue) UIColorFromRGBA(rgbValue, 1.0)

#define font(float) [UIFont systemFontOfSize:float]

#define StatusBarHeight [[UIApplication sharedApplication] statusBarFrame].size.height
#define NavBarHeight 44.0
#define TopHeight (StatusBarHeight + NavBarHeight)


#define UIScreenWidth [UIScreen mainScreen].bounds.size.width
#define UIScreenHeight [UIScreen mainScreen].bounds.size.height
#define UIScreenWidthScale   UIScreenWidth / 320

#define dispatch_sync_main_safe(block)\
if ([NSThread isMainThread]) {\
block();\
} else {\
dispatch_sync(dispatch_get_main_queue(), block);\
}

#define dispatch_async_main_safe(block)\
if ([NSThread isMainThread]) {\
block();\
} else {\
dispatch_async(dispatch_get_main_queue(), block);\
}

#endif /* NTESBasicDefine_h */
