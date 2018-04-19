//
//  UIButton+NTESVerticalLayout.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/2.
//  Copyright © 2018 netease. All rights reserved.
//

#import "UIButton+NTESVerticalLayout.h"

const CGFloat defaultPadding = 3.f;

@implementation UIButton (NTESVerticalLayout)

- (void)centerVertically {
    [self centerVerticallyWithPadding:defaultPadding];
}


- (void)centerVerticallyWithPadding:(CGFloat)padding {
    CGSize imageSize = self.imageView.frame.size;
    CGSize titleSize = self.titleLabel.frame.size;
    CGFloat totalHeight = (imageSize.height + titleSize.height + padding);
    
    self.imageEdgeInsets = UIEdgeInsetsMake(- (totalHeight - imageSize.height), 0.f, 0.f, - titleSize.width);
    self.titleEdgeInsets = UIEdgeInsetsMake(0.f, - imageSize.width, - (totalHeight - titleSize.height), 0.f);
    self.contentEdgeInsets = UIEdgeInsetsMake(0.f, 0.f, titleSize.height, 0.f);
}

@end
