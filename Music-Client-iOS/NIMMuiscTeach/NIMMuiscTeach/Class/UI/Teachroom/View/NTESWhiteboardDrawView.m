//
//  WhiteBoardDrawView.m
//  NIM
//
//  Created by 高峰 on 15/7/1.
//  Copyright (c) 2015年 Netease. All rights reserved.
//

#import "NTESWhiteboardDrawView.h"
#import <QuartzCore/QuartzCore.h>
#import "NTESCADisplayLinkHolder.h"
#import "NTESWhiteboardPoint.h"

@interface NTESWhiteboardDrawView()<NTESCADisplayLinkHolderDelegate>

@property(nonatomic, strong)NTESCADisplayLinkHolder *displayLinkHolder;

@end

@implementation NTESWhiteboardDrawView

#pragma mark - public methods
- (id)initWithFrame:(CGRect)frame
{
    if (self = [super initWithFrame:frame]) {        
        CAShapeLayer *shapeLayer = (CAShapeLayer *)self.layer;
        shapeLayer.fillColor = [UIColor clearColor].CGColor;
        shapeLayer.masksToBounds = YES;
        
        _displayLinkHolder = [[NTESCADisplayLinkHolder alloc] init];
        [_displayLinkHolder setFrameInterval:3];
        [_displayLinkHolder startCADisplayLinkWithDelegate:self];
    }
    return self;
}

-(void)dealloc
{
    [_displayLinkHolder stop];
}

+ (Class)layerClass
{
    return [CAShapeLayer class];
}


- (void)onDisplayLinkFire:(NTESCADisplayLinkHolder *)holder
                 duration:(NSTimeInterval)duration
              displayLink:(CADisplayLink *)displayLink
{
    if (self.dataSource && [self.dataSource hasUpdate]) {
        [self setNeedsDisplay];
    }
}


- (void)drawRect:(CGRect)rect
{
    NSDictionary *allLines = [self.dataSource allLinesToDraw];
    
    for (NSString *uid in allLines.allKeys) {
        NSArray *lines = [allLines objectForKey:uid];
        
        NSUInteger linesCount = lines.count;

        for (NSUInteger i = 0 ; i < linesCount; i ++) {
            UIBezierPath *path = [[UIBezierPath alloc] init];
            path.lineWidth = 1.5;
            path.lineJoinStyle = kCGLineJoinRound;
            path.lineCapStyle = kCGLineCapRound;

            NSArray *line = [lines objectAtIndex:i];
            NSUInteger pointsCount = line.count;
            
            NTESWhiteboardPoint *firstPoint = [line objectAtIndex:0];

            UIColor *lineColor = UIColorFromRGB(firstPoint.colorRGB);
            
            for (NSUInteger j = 0 ; j < pointsCount; j ++) {
                NTESWhiteboardPoint *point = [line objectAtIndex:j];
                
                CGPoint p = CGPointMake(point.xScale * self.frame.size.width , point.yScale * self.frame.size.height);

                if (j == 0) {
                    [path moveToPoint:p];
                    [path addLineToPoint:p];
                }
                else {
                    [path addLineToPoint:p];
                }
            }
            
            [lineColor setStroke];
            
            [path stroke];
            
        }

    }
    
}

@end
