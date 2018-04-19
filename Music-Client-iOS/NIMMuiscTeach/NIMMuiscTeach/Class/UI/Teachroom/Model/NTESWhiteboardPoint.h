//
//  NTESWhiteboardPoint.h
//  NIMEducationDemo
//
//  Created by fenric on 16/10/26.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, NTESWhiteboardPointType){
    NTESWhiteboardPointTypeStart    = 1,
    NTESWhiteboardPointTypeMove     = 2,
    NTESWhiteboardPointTypeEnd      = 3,
};


@interface NTESWhiteboardPoint : NSObject

//点类型
@property(nonatomic, assign) NTESWhiteboardPointType type;

//x 轴比例
@property(nonatomic, assign) float xScale;
//y 轴比例
@property(nonatomic, assign) float yScale;

//颜色
@property(nonatomic, assign) int colorRGB;

//属于页面编号
@property(nonatomic, assign) int pageIndex;

@end
