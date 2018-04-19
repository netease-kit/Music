//
//  NTESAvatarView.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/8.
//  Copyright © 2018 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

#define kNetStatus @"kNetStatus"


typedef NS_ENUM(NSInteger, NTESNetStatus) {
    NTESNetStatusBad,
    NTESNetStatusNormal,
    NTESNetStatusGood,
};

@interface NTESAvatarView : UIView

@property(nonatomic, strong) UIButton *netStatusBtn;
@property(nonatomic, strong) UIImageView *avatar;

- (instancetype)initWithFrame:(CGRect)frame Role:(NTESUserRole)role andImageURL:(NSString *)imgURL;

@end
