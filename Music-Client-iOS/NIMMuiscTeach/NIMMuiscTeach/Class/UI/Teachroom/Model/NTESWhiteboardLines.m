//
//  NTESWhiteboardLines.m
//  NIMEducationDemo
//
//  Created by fenric on 16/10/26.
//  Copyright © 2016年 Netease. All rights reserved.
//

#import "NTESWhiteboardLines.h"

@interface NTESWhiteboardLines()

@property (nonatomic, assign) NSInteger currentPage;

//所有人的白板线信息，key 是 uid
@property(nonatomic, strong) NSMutableDictionary *allLines;

@property(nonatomic, assign) BOOL hasUpdate;

@end

@implementation NTESWhiteboardLines

- (instancetype)init
{
    if (self = [super init]) {
        _allLines = [[NSMutableDictionary alloc] init];
        _currentPage = 1;
    }
    return self;
}

- (NSDictionary *)allLines
{
    return _allLines;
}

- (void)addPoint:(NTESWhiteboardPoint *)point uid:(NSString *)uid
{
    if (!point || !uid) {
        return;
    }
    
    NSMutableDictionary *dict = [_allLines objectForKey:uid];
    
    if (!dict) {
        dict = [[NSMutableDictionary alloc] init];
        [_allLines setObject:dict forKey:uid];
    }
    
    NSMutableArray *lines = [dict objectForKey:@(point.pageIndex)];
    
    if (!lines)
    {
        lines = [[NSMutableArray alloc] init];
        [dict setObject:lines forKey:@(point.pageIndex)];
    }
    
    if (point.type == NTESWhiteboardPointTypeStart) {
        [lines addObject:[NSMutableArray arrayWithObject:point]];
    }
    else if (lines.count == 0){
        [lines addObject:[NSMutableArray arrayWithObject:point]];
    }
    else {
        NSMutableArray *lastLine = [lines lastObject];
        [lastLine addObject:point];
    }
    
    _hasUpdate = YES;
}

- (void)changeCurrentPage:(NSInteger)page {
    self.currentPage = page;
    _hasUpdate = YES;
}

- (void)cancelLastLine:(NSString *)uid inPage:(NSInteger)pageNum
{
    NSDictionary *dict = [_allLines objectForKey:uid];
    NSMutableArray *lines = [dict objectForKey:@(pageNum)];
    [lines removeLastObject];
    _hasUpdate = YES;
}

- (void)clear {
    [_allLines removeAllObjects];
    _hasUpdate = YES;
}

- (void)clearUser:(NSString *)uid currentPagelines:(NSInteger)pageNum
{
    NSMutableDictionary *dict = [_allLines objectForKey:uid];
    NSMutableArray *lines = [dict objectForKey:@(pageNum)];
    [lines removeAllObjects];
    _hasUpdate = YES;
}

#pragma  mark - NTESWhiteboardDrawViewDataSource
- (NSDictionary *)allLinesToDraw
{
    _hasUpdate = NO;
    NSMutableDictionary *drawLines = [[NSMutableDictionary alloc] init];
    
    for (NSString *uid in _allLines.allKeys)
    {
        NSDictionary *dict = [_allLines objectForKey:uid];
        NSArray *lines = [dict objectForKey:@(self.currentPage)];
        if (lines)
        {
            [drawLines setObject:lines forKey:uid];
        }
    }
    return drawLines;
}


- (BOOL)hasUpdate
{
    return _hasUpdate;
}

- (BOOL)hasLines
{
    BOOL has = NO;
    
    for (NSMutableArray *lines in _allLines.allValues) {
        if (lines.count > 0) {
            has = YES;
        }
    }
    return has;
}

@end
