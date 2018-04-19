//
//  NTESDocumentShareInfo.h
//  NIMEducationDemo
//
//  Created by Simon Blue on 16/12/16.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, NTESDocShareType){
    NTESDocShareTypeStatusNotification = 0,
    NTESDocShareTypeTurnThePage = 1,
};


@interface NTESDocumentShareInfo : NSObject

//当前页数
@property(nonatomic, assign) int currentPage;

//总页数
@property(nonatomic, assign) int pageCount;

//文档操作类型
@property(nonatomic, assign) NTESDocShareType type;

//文档id
@property(nonatomic, strong) NSString *docId;

@end
