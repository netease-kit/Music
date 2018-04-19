//
//  NTESAvatarView.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/8.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESAvatarView.h"

@interface NTESAvatarView() <NIMNetCallManagerDelegate>

@property(nonatomic, assign) NTESUserRole role;
@property(nonatomic, strong) NSString *avatarURL;
@property(nonatomic, strong) UIImageView *logo;

@end

@implementation NTESAvatarView

- (instancetype)initWithFrame:(CGRect)frame Role:(NTESUserRole)role andImageURL:(NSString *)imgURL {
    if (self = [super initWithFrame:frame]) {
        self.role = role;
        self.avatarURL = imgURL;
        [self setupSubviews];
    }
    return self;
}

- (void)setupSubviews {
    switch (self.role) {
        case NTESUserRoleTeacher:
        {
            __weak typeof(self) wself = self;
            [@[self.netStatusBtn,
               self.avatar,
               self.logo] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
                   [wself addSubview:view];
               }];
        }
            break;
        case NTESUserRoleStudent:
        {
            [self addSubview:self.netStatusBtn];
        }
            break;
        default:
            break;
    }
}

- (void)layoutSubviews {
    self.avatar.left = 0;
    self.avatar.centerY = self.height / 2;
    self.netStatusBtn.top = self.height / 2 - 13;
    self.netStatusBtn.left = self.avatar.centerX;
    self.netStatusBtn.height = 26;
    self.netStatusBtn.width = 70;
    self.logo.left = self.avatar.centerX;
    self.logo.top = self.avatar.centerY;
}

- (UIImageView *)avatar {
    if (!_avatar) {
        _avatar = ({
            UIImageView *imgView = [[UIImageView alloc] init];
            imgView.frame = CGRectMake(0, 0, 38, 38);
            if (self.avatarURL) {
                NSURL *url = [NSURL URLWithString:self.avatarURL];
                [imgView sd_setImageWithURL:url];
            }
            else {
                [imgView setImage:[UIImage imageNamed:@"room_avatar_user"]];
            }
            imgView.layer.cornerRadius = 19;
            imgView.clipsToBounds = YES;
            imgView;
        });
    }
    return _avatar;
}

- (UIButton *)netStatusBtn {
    if (!_netStatusBtn) {
        _netStatusBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            if (self.role == NTESUserRoleTeacher) {
                [btn setContentEdgeInsets:UIEdgeInsetsMake(0, 15, 0, 0)];
                btn.backgroundColor = UIColorFromRGB(0xd8d8d8);
            }
            [btn setTitle:@"离线" forState:UIControlStateNormal];
            btn.titleLabel.font = font(10.f);
            btn.titleLabel.adjustsFontSizeToFitWidth = YES;
            [btn setTitleColor:[UIColor lightGrayColor] forState:UIControlStateNormal];
            btn.layer.cornerRadius = 13.f;
            btn;
        });
    }
    return _netStatusBtn;
}

- (UIImageView *)logo {
    if (!_logo) {
        _logo = [UIImageView new];
        _logo.image = [UIImage imageNamed:@"room_logo"];
        _logo.size = CGSizeMake(16, 16);
        _logo.layer.cornerRadius = 8;
    }
    return _logo;
}

@end
