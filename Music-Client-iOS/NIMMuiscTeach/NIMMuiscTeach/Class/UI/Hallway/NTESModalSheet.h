//
//  NTESModalSheet.h
//  NIMMuiscTeach
//
//  Created by Netease on 2019/5/31.
//  Copyright © 2019 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, NTESAudioQuality){
    NTESAudioQualityNormal = 0,
    NTESAudioQualityHD,
    NTESAudioQualityHDMusic,
};

typedef void(^NTESModalSheetSelectBlock)(NSInteger index);

@interface NTESModalSheet : UIView

+ (void)showOnView:(UIView *)view
          selected:(NTESModalSheetSelectBlock)selected;


@end

NS_ASSUME_NONNULL_END
