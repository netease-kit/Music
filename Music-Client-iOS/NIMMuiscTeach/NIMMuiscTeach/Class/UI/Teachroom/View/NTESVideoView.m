//
//  NTESVideoView.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/9.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESVideoView.h"
#import "NTESGLView.h"

@interface NTESVideoView ()

@property(nonatomic, strong) NTESGLView *glView;
@property(nonatomic, strong) UIImageView *imgView;

@end

@implementation NTESVideoView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.imgView];
        [self addSubview:self.glView];
    }
    return self;
}

- (void)setupSubviews {
    self.imgView.frame = self.bounds;
    self.glView.frame = self.bounds;
}

- (void)renderWhenYUVReady:(NSData *)yuvData width:(NSUInteger)width height:(NSUInteger)height from:(NSString *)user {
    [self.glView render:yuvData width:width height:height];
}


- (NTESGLView *)glView {
    if (!_glView) {
        _glView = [[NTESGLView alloc] initWithFrame:self.bounds];
        _glView.contentMode = UIViewContentModeScaleAspectFit;
    }
    return _glView;
}

- (UIImageView *)imgView {
    if (!_imgView) {
        _imgView = ({
            UIImageView *imgView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"room_bitmap"]];
            imgView.contentMode = UIViewContentModeScaleAspectFill;
            imgView;
        });
    }
    return _imgView;
}

@end
