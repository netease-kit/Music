//
//  NTESTemplatePickerView.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/3.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESTemplatePickerView.h"

@interface NTESTemplatePickerView ()

@property(nonatomic, strong) UIScrollView *containerView;

@property(nonatomic, assign) CGFloat totalWidth;

@property(nonatomic, strong) UIButton *templateBtn;

@end

@implementation NTESTemplatePickerView

- (instancetype)initWithFrame:(CGRect)frame templateType:(NTESTemplatePickerType)type {
    if (self = [super initWithFrame:frame]) {
        self.templateType = type;
        [self configSubviews];
    }
    return self;
}

- (void)configSubviews {
    self.backgroundColor = UIColorFromRGB(0x1E1F20);
    switch (self.templateType) {
        case NTESTemplatePickerTypeVideo:
        {
            [self addVideoModePanel];
        }
            break;
        case NTESTemplatePickerTypeColor:
        {
            [self addColorPanel];
        }
            break;
        default:
            break;
    }
}

- (void)addVideoModePanel {
    CGFloat offset = 0;
    [self addSubview:self.containerView];
    self.containerView.frame = self.bounds;

    NSArray *textList = @[@"语音模式", @"学生画面", @"双向视频"];
    CGFloat templateWidth = 60 * UIScreenWidthScale;
    CGFloat templateHeight = 84 * UIScreenWidthScale;
    CGFloat templateSpace = (self.containerView.width - templateWidth * 3) / 4;
    for (int i = 0; i < 3; ++i) {
        offset += templateSpace;
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.frame = CGRectMake(offset, 0, templateWidth, templateHeight);
        btn.tag = i + 10 * self.templateType;
        UIImage *img = [UIImage imageNamed:self.imgList[i]];
        [btn setImage:img forState:UIControlStateNormal];
        [btn setImage:[UIImage imageNamed:self.imgSelectList[i]] forState:UIControlStateSelected];

        [btn setTitle:textList[i] forState:UIControlStateNormal];
        btn.titleLabel.font = font(13);
        [btn setTitleColor:[UIColor lightGrayColor] forState:UIControlStateNormal];
        [btn setTitleColor:[UIColor whiteColor] forState:UIControlStateSelected];
        
        btn.titleLabel.adjustsFontSizeToFitWidth = YES;
        [btn centerVerticallyWithPadding:3];
        [self.containerView addSubview:btn];
        btn.centerY = self.containerView.height / 2 + 3;
        [btn addTarget:self action:@selector(videoModeBtnTapped:) forControlEvents:UIControlEventTouchUpInside];

        offset += templateWidth;
        if (i == 2) self.totalWidth = offset + templateSpace;
        if (i == 0) {
            btn.selected = YES;
        }
    }
    self.containerView.contentSize = CGSizeMake(self.totalWidth, self.containerView.height);
    self.containerView.bounces = NO;
}

- (void)addColorPanel {
    UIButton *backBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [backBtn setImage:[UIImage imageNamed:@"panel_back"] forState:UIControlStateNormal];
    backBtn.frame = CGRectMake(10, 0, 22, 22);
    [self addSubview:backBtn];
    backBtn.tag = 10 * self.templateType;
    
    [self addSubview:self.containerView];
    self.containerView.frame = CGRectMake(backBtn.right, 0, self.width * 0.65, self.height);
    CGFloat offset = 0.f;
    NSArray *colorList = @[@(0xFF0000), @(0xEDB400), @(0x62A515), @(0xB322FB), @(0x4691E8)];
    CGFloat templateWidth = 25 * UIScreenWidthScale;
    CGFloat templateSpace = 10.f * UIScreenWidthScale;
    
    for (int i = 1; i < 6; ++i) {
        offset += templateSpace;
        UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
        btn.tag = i + 10 * self.templateType;
        btn.frame = CGRectMake(offset, 0, templateWidth, templateWidth);
        btn.layer.cornerRadius = templateWidth / 2;
        btn.backgroundColor = UIColorFromRGB([colorList[i - 1] integerValue]);
        btn.centerY = self.containerView.height / 2;
        [self.containerView addSubview:btn];
        offset += templateWidth;
        
        if (i == 5) self.totalWidth = offset + templateSpace;
        if (i == 1) {
            btn.selected = YES;
            btn.layer.borderColor = [UIColor whiteColor].CGColor;
            btn.layer.borderWidth = 2;
        }
        [btn addTarget:self action:@selector(colorBtnTapped:) forControlEvents:UIControlEventTouchUpInside];
    }
    self.containerView.contentSize = CGSizeMake(self.totalWidth, self.containerView.height);
    self.containerView.bounces = NO;
    
    UIButton *withdrawBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [withdrawBtn setImage:[UIImage imageNamed:@"panel_undo"] forState:UIControlStateNormal];
    withdrawBtn.frame = CGRectMake(self.right - 80 * UIScreenWidthScale, 0, templateWidth, templateWidth);
    withdrawBtn.tag = 6 + 10 * self.templateType;
    UIButton *clearBtn = [UIButton buttonWithType:UIButtonTypeCustom];
    [clearBtn setImage:[UIImage imageNamed:@"panel_clear"] forState:UIControlStateNormal];
    clearBtn.frame = CGRectMake(self.right -  35 * UIScreenWidthScale, 0, templateWidth, templateWidth);
    clearBtn.tag = 7 + 10 * self.templateType;
    [self addSubview:withdrawBtn];
    [self addSubview:clearBtn];
    backBtn.centerY = self.containerView.centerY;
    withdrawBtn.centerY = self.containerView.centerY;
    clearBtn.centerY = self.containerView.centerY;
    [@[backBtn,
       withdrawBtn,
       clearBtn] enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
           [btn addTarget:self action:@selector(colorBtnTapped:) forControlEvents:UIControlEventTouchUpInside];
       }];
}

#pragma mark - Actions

- (void)videoModeBtnTapped:(UIButton *)sender {
    sender.selected = YES;
    [self changeSelectBtnState:sender];
    NSInteger index = sender.tag - 10 * self.templateType;
    if ([self.delegate respondsToSelector:@selector(templatePickerView:didSelectModeAtIndex:)]) {
        [self.delegate templatePickerView:self didSelectModeAtIndex:index];
    }
}

- (void)colorBtnTapped:(UIButton *)sender {
    sender.selected = YES;
    NSInteger index = sender.tag - 10 * self.templateType;
    if (index > 0 && index < 6) {
        [self changeSelectBtnState:sender];
    }
    if ([self.delegate respondsToSelector:@selector(templatePickerView:didSelectColorAtIndex:)]) {
        [self.delegate templatePickerView:self didSelectColorAtIndex:index];
    }
}

- (void)changeSelectBtnState:(UIButton *)btn {
    NSInteger offset = 10 * self.templateType;
    switch (self.templateType) {
        case NTESTemplatePickerTypeVideo:
        {
            for (int i = 0; i < 3; ++i) {
                if (btn.tag != i + offset) {
                    UIButton *otherBtn = [self viewWithTag:i + offset];
                    otherBtn.selected = NO;
                }
            }
        }
            break;
        case NTESTemplatePickerTypeColor:
        {
            btn.layer.borderColor = [UIColor whiteColor].CGColor;
            btn.layer.borderWidth = 2;
            for (int i = 1; i < 6; ++i) {
                if (btn.tag != i + offset) {
                    [self viewWithTag:i + offset].layer.borderColor = nil;
                    [self viewWithTag:i + offset].layer.borderWidth = 0;
                }
            }
        }
            break;
        default:
            break;
    }
}

#pragma mark - Getter

- (UIScrollView *)containerView {
    if (!_containerView) {
        _containerView = ({
            UIScrollView *view = [UIScrollView new];
            view.showsVerticalScrollIndicator = NO;
            view.showsHorizontalScrollIndicator = NO;
            view;
        });
    }
    return _containerView;
}

- (NSArray *)imgList {
    return @[@"video_voice", @"video_student", @"video_bio"];
}

- (NSArray *)imgSelectList {
    return @[@"video_voice_pressed", @"video_student_pressed", @"video_bio_pressed"];

}

@end
