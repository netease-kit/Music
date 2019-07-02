//
//  NTESControlPanel.m
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/2.
//  Copyright © 2018 netease. All rights reserved.
//

#import "NTESControlPanel.h"
#import "NTESTemplatePickerView.h"
#import "NTESWhiteboardDrawView.h"

#define VIDEO_PANEL_HEIGHT 100 * UIScreenWidthScale
#define COLOR_PANEL_HEIGHT 60 * UIScreenWidthScale

#define SWIPE_TO_PRE 1000
#define SWIPE_TO_NEXT 1001

@interface NTESControlPanel () <NTESTemplatePickerViewDelegate>

@property(nonatomic, strong) UIButton *switchModeBtn;
@property(nonatomic, strong) UIButton *muteBtn;
@property(nonatomic, assign) NSInteger currPage;

@property(nonatomic, strong) UIView *bottomView;

@property(nonatomic, strong) NTESTemplatePickerView *pickModeView;
@property(nonatomic, strong) NTESTemplatePickerView *pickColorView;

@end

@implementation NTESControlPanel

- (void)dealloc {
    [NSObject cancelPreviousPerformRequestsWithTarget:self];
    NSLog(@"NTESControlPanel dealloc");
}

- (instancetype)initWithFrame:(CGRect)frame andCurrentPage:(NSInteger)currPage {
    if (self = [super initWithFrame:frame]) {
        self.currPage = currPage;
        [self configSubviews];
    }
    return self;
}

- (void)configSubviews {
    self.clipsToBounds = YES;
    self.backgroundColor = [UIColor clearColor];
    [self addSubview:self.coverView];
    [self addSubview:self.bottomView];
    [@[self.switchModeBtn,
       self.paintBtn,
       self.muteBtn,
       self.prePageBtn,
       self.nextPageBtn] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
           [self.bottomView addSubview:view];
       }];
    [self addSubview:self.pickModeView];
    [self addSubview:self.pickColorView];
}

- (void)layoutSubviews {
    self.coverView.frame = CGRectMake(0, 0, self.width, self.height);
    self.bottomView.frame = CGRectMake(0, self.height - 60 * UIScreenWidthScale, self.width, 60 * UIScreenWidthScale);
    [@[self.switchModeBtn,
       self.paintBtn,
       self.muteBtn,
       self.prePageBtn,
       self.nextPageBtn] enumerateObjectsUsingBlock:^(UIView *view, NSUInteger idx, BOOL * _Nonnull stop) {
           view.centerY = self.bottomView.height / 2 + 3;
           view.centerX = self.width / 10 * (2 * idx + 1);
       }];
}

- (CGFloat)btnH {
    return 60 * UIScreenWidthScale;
}

#pragma mark - Touch

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    UIView *hitView = [super hitTest:point withEvent:event];
    if ([hitView isKindOfClass:[NTESWhiteboardDrawView class]]) {
        return nil;
    }
    else return hitView;
}

- (BOOL)rect:(CGRect)rect containPoint:(CGPoint)point
{
    return (point.x > rect.origin.x
            && point.x < rect.origin.x + rect.size.width
            && point.y > rect.origin.y
            && point.y < rect.origin.y + rect.size.height);
}
#pragma mark - Private

- (void)controlPanel:(NTESTemplatePickerView *)view isPopup:(BOOL)pop {
    if (pop) {//弹出
        if (view.templateType == NTESTemplatePickerTypeVideo && self.pickModeView.alpha == 0) {
            [UIView animateWithDuration:0.2 animations:^{
                CGRect rect = self.pickModeView.frame;
                rect.origin.y -= VIDEO_PANEL_HEIGHT;
                self.pickModeView.frame = rect;
                self.pickModeView.alpha = 1;
            }];
        }
        if (view.templateType == NTESTemplatePickerTypeColor && self.pickColorView.alpha == 0) {
            [UIView animateWithDuration:0.2 animations:^{
                CGRect rect = self.pickColorView.frame;
                rect.origin.y -= COLOR_PANEL_HEIGHT;
                self.pickColorView.frame = rect;
                self.pickColorView.alpha = 1;
            }];
        }
    }
    else {//收起
        if (view.templateType == NTESTemplatePickerTypeVideo && self.pickModeView.alpha == 1) {
            [UIView animateWithDuration:0.2 animations:^{
                CGRect rect = self.pickModeView.frame;
                rect.origin.y += VIDEO_PANEL_HEIGHT;
                self.pickModeView.frame = rect;
                self.pickModeView.alpha = 0;
            }];
        }
        if (view.templateType == NTESTemplatePickerTypeColor && self.pickColorView.alpha == 1) {
            [UIView animateWithDuration:0.2 animations:^{
                CGRect rect = self.pickColorView.frame;
                rect.origin.y += COLOR_PANEL_HEIGHT;
                self.pickColorView.frame = rect;
                self.pickColorView.alpha = 0;
            }];
        }
    }
    
}

- (void)pageWithDirect:(NSInteger)direction {
    switch (direction) {
        case SWIPE_TO_PRE:
        {
            self.nextPageBtn.enabled = YES;
            if ([self.delegate respondsToSelector:@selector(controlPanel:prePageBtnTapped:)]) {
                [self.delegate controlPanel:self prePageBtnTapped:self.prePageBtn];
            }
        }
            break;
        case SWIPE_TO_NEXT:
        {
            self.prePageBtn.enabled = YES;
            if ([self.delegate respondsToSelector:@selector(controlPanel:nextPageBtnTapped:)]) {
                [self.delegate controlPanel:self nextPageBtnTapped:self.nextPageBtn];
            }
        }
        default:
            break;
    }
}

#pragma mark - Action

- (void)onSwitchModeBtnTap:(UIButton *)sender {
    [self controlPanel:self.pickModeView isPopup:YES];
}

- (void)onPaintBtnTap:(UIButton *)sender {
    self.coverView.hidden = YES;
    [self controlPanel:self.pickColorView isPopup:YES];
}

- (void)onMuteBtnTap:(UIButton *)sender {
    sender.selected = !sender.selected;
    if ([self.delegate respondsToSelector:@selector(controlPanel:muteVoice:)]) {
        [self.delegate controlPanel:self muteVoice:sender.isSelected];
    }
}

- (void)onPrePageTap:(UIButton *)sender {
    self.nextPageBtn.enabled = YES;
    if ([self.delegate respondsToSelector:@selector(controlPanel:prePageBtnTapped:)]) {
        [self.delegate controlPanel:self prePageBtnTapped:self.prePageBtn];
    }
}

- (void)onNextPageTap:(UIButton *)sender {
    self.prePageBtn.enabled = YES;
    if ([self.delegate respondsToSelector:@selector(controlPanel:nextPageBtnTapped:)]) {
        [self.delegate controlPanel:self nextPageBtnTapped:self.nextPageBtn];
    }
}

- (void)coverViewTapped:(UITapGestureRecognizer *)recognizer {
    [self controlPanel:self.pickModeView isPopup:NO];
}

#pragma mark - Delegate

- (void)templatePickerView:(NTESTemplatePickerView *)pickerView didSelectModeAtIndex:(NSInteger)index {
    //选择不同视频模式
    if ([self.delegate respondsToSelector:@selector(controlPanel:selectVideoMode:)]) {
        [self.delegate controlPanel:self selectVideoMode:index + 1];
    }
}

- (void)templatePickerView:(NTESTemplatePickerView *)pickerView didSelectColorAtIndex:(NSInteger)index {
    //选择不同画笔颜色
    switch (index) {
        case 0:
        {
            //返回按钮
            self.coverView.hidden = NO;
            [self controlPanel:self.pickColorView isPopup:NO];
        }
            break;
        case 1: case 2: case 3: case 4: case 5:
        {
            self.coverView.hidden = YES;
            if ([self.delegate respondsToSelector:@selector(controlPanel:drawWtihColor:)]) {
                [self.delegate controlPanel:self drawWtihColor:index];
            }
        }
            break;
        case 6:
        {
            self.coverView.hidden = YES;
            if ([self.delegate respondsToSelector:@selector(cancelLastLineWithControlPanel:)]) {
                [self.delegate cancelLastLineWithControlPanel:self];
            }
        }
            break;
        case 7:
        {
            self.coverView.hidden = YES;
            if ([self.delegate respondsToSelector:@selector(clearAlllineWithControlPanel:)]) {
                [self.delegate clearAlllineWithControlPanel:self];
            }
        }
            break;
        default:
            break;
    }
}

#pragma mark - Getter

- (UIButton *)switchModeBtn {
    if (!_switchModeBtn) {
        _switchModeBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.frame = CGRectMake(0, 0, 48, 48);
            [btn setImage:[UIImage imageNamed:@"room_video_mode"] forState:UIControlStateNormal];
            [btn setTitle:@"视频模式" forState:UIControlStateNormal];
            btn.titleLabel.textColor = [UIColor whiteColor];
            btn.titleLabel.font = font(10.f);
            [btn centerVerticallyWithPadding:6];
            [btn addTarget:self action:@selector(onSwitchModeBtnTap:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _switchModeBtn;
}

- (UIButton *)paintBtn {
    if (!_paintBtn) {
        _paintBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.frame = CGRectMake(0, 0, 48, 48);
            [btn setImage:[UIImage imageNamed:@"room_paint"] forState:UIControlStateNormal];
            [btn setTitle:@"画笔" forState:UIControlStateNormal];
            btn.titleLabel.textColor = [UIColor whiteColor];
            btn.titleLabel.font = font(10.f);
            [btn centerVerticallyWithPadding:6];
            [btn addTarget:self action:@selector(onPaintBtnTap:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _paintBtn;
}

- (UIButton *)muteBtn {
    if (!_muteBtn) {
        _muteBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.frame = CGRectMake(0, 0, 48, 48);
            [btn setImage:[UIImage imageNamed:@"room_unmute"] forState:UIControlStateNormal];
            [btn setImage:[UIImage imageNamed:@"room_mute"] forState:UIControlStateSelected];
            [btn setTitle:@"声音" forState:UIControlStateNormal];
            btn.titleLabel.textColor = [UIColor whiteColor];
            btn.titleLabel.font = font(10.f);
            [btn centerVerticallyWithPadding:6];
            [btn addTarget:self action:@selector(onMuteBtnTap:) forControlEvents:UIControlEventTouchUpInside];
            btn;
        });
    }
    return _muteBtn;
}

- (UIButton *)prePageBtn {
    if (!_prePageBtn) {
        _prePageBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.frame = CGRectMake(0, 0, 48, 48);
            [btn setImage:[UIImage imageNamed:@"room_pre"] forState:UIControlStateNormal];
            [btn setTitle:@"左翻" forState:UIControlStateNormal];
            btn.titleLabel.textColor = [UIColor whiteColor];
            btn.titleLabel.font = font(10.f);
            [btn centerVerticallyWithPadding:6];
            [btn addTarget:self action:@selector(onPrePageTap:) forControlEvents:UIControlEventTouchUpInside];
            [btn setEnabled:self.currPage != 1];
            btn;
        });
    }
    return _prePageBtn;
}

- (UIButton *)nextPageBtn {
    if (!_nextPageBtn) {
        _nextPageBtn = ({
            UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
            btn.frame = CGRectMake(0, 0, 48, 48);
            [btn setImage:[UIImage imageNamed:@"room_next"] forState:UIControlStateNormal];
            [btn setTitle:@"右翻" forState:UIControlStateNormal];
            btn.titleLabel.textColor = [UIColor whiteColor];
            btn.titleLabel.font = font(10.f);
            [btn centerVerticallyWithPadding:6];
            [btn addTarget:self action:@selector(onNextPageTap:) forControlEvents:UIControlEventTouchUpInside];
            btn.enabled = (self.currPage != 5);
            btn;
        });
    }
    return _nextPageBtn;
}

- (UIView *)coverView {
    if (!_coverView) {
        _coverView = ({
            UIView *view = [UIView new];
            view.backgroundColor = [UIColor clearColor];
            UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(coverViewTapped:)];
            [view addGestureRecognizer:tap];
            view;
        });
    }
    return _coverView;
}

- (UIView *)bottomView {
    if (!_bottomView) {
        _bottomView = ({
            UIView *view = [UIView new];
            view.backgroundColor = UIColorFromRGBA(0x1E1F20, 0.9);
            view;
        });
    }
    return _bottomView;
}

- (NTESTemplatePickerView *)pickModeView {
    if (!_pickModeView) {
        _pickModeView = ({
            NTESTemplatePickerView *pickView = [[NTESTemplatePickerView alloc] initWithFrame:CGRectMake(0, self.height, self.width, VIDEO_PANEL_HEIGHT) templateType:NTESTemplatePickerTypeVideo];
            pickView.delegate = self;
            pickView.alpha = 0;
            pickView;
        });
    }
    return _pickModeView;
}

- (NTESTemplatePickerView *)pickColorView {
    if (!_pickColorView) {
        _pickColorView = ({
            NTESTemplatePickerView *pickView = [[NTESTemplatePickerView alloc] initWithFrame:CGRectMake(0, self.height, self.width, COLOR_PANEL_HEIGHT) templateType:NTESTemplatePickerTypeColor];
            pickView.delegate = self;
            pickView.alpha = 0;
            pickView;
        });
    }
    return _pickColorView;
}

@end
