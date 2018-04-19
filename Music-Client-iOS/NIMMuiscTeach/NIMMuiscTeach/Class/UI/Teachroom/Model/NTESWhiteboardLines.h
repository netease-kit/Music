//
//  NTESWhiteboardLines.h
//  NIMEducationDemo
//
//  Created by fenric on 16/10/26.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "NTESWhiteboardPoint.h"
#import "NTESWhiteboardDrawView.h"

@interface NTESWhiteboardLines : NSObject<NTESWhiteboardDrawViewDataSource>

- (void)addPoint:(NTESWhiteboardPoint *)point uid:(NSString *)uid;

- (void)cancelLastLine:(NSString *)uid inPage:(NSInteger)pageNum;

- (void)clear;

- (void)clearUser:(NSString *)uid currentPagelines:(NSInteger)pageNum;

- (NSDictionary *)allLines;

- (BOOL)hasLines;

- (void)changeCurrentPage:(NSInteger)page;

@end
