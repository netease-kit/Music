//
//  NTESModalSheet.m
//  NIMMuiscTeach
//
//  Created by Netease on 2019/5/31.
//  Copyright © 2019 netease. All rights reserved.
//

#import "NTESModalSheet.h"
#import "UIView+NTES.h"

@interface NTESModalSheetBar : UIView
@property (nonatomic, copy) NTESModalSheetSelectBlock selectBlock;
@end

@interface NTESModalSheet ()

@property (nonatomic, weak) UIView *superView;
@property (nonatomic, strong) UIControl *mask;
@property (nonatomic, weak) UIView *onView;
@property (nonatomic, strong) NTESModalSheetBar *sheet;
@property (nonatomic, copy) NTESModalSheetSelectBlock selectBlock;
@end

@implementation NTESModalSheet

+ (void)showOnView:(UIView *)view
          selected:(NTESModalSheetSelectBlock)selected {
    NTESModalSheet *sheet = [[NTESModalSheet alloc] initWithFrame:view.bounds];
    [sheet showOnView:view selected:selected];
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self addSubview:self.mask];
        [self addSubview:self.sheet];
    }
    return self;
}

- (void)layoutContents {
    _sheet.center = CGPointMake(self.width/2, self.height/2);
    _mask.frame = self.bounds;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    if (_superView && !CGRectEqualToRect(self.frame, _superView.bounds)) {
        self.bounds = _superView.bounds;
        [self layoutContents];
    }
}

#pragma mark - Function
- (void)showOnView:(UIView *)view
          selected:(NTESModalSheetSelectBlock)selected {
    
    _superView = view;
    _selectBlock = selected;
    
    self.frame = view.bounds;
    [self layoutContents];
    [view addSubview:self];
}

- (void)dismiss {
    [self removeFromSuperview];
}

- (void)maskTapAction:(id)sender {
    [self dismiss];
}

#pragma mark - Getter
- (UIControl *)mask {
    if (!_mask) {
        _mask = [[UIControl alloc] init];
        _mask.backgroundColor = UIColorFromRGBA(0xffffff, 0.8);
        [_mask addTarget:self action:@selector(maskTapAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _mask;
}

- (NTESModalSheetBar *)sheet {
    if (!_sheet) {
        _sheet = [[NTESModalSheetBar alloc] init];
        [_sheet sizeToFit];
        _sheet.layer.borderColor = [UIColor groupTableViewBackgroundColor].CGColor;
        _sheet.layer.borderWidth = 1.0 * [UIScreen mainScreen].scale;;
        _sheet.layer.cornerRadius = 8.0;
        _sheet.clipsToBounds = YES;
        __weak typeof(self) weakSelf = self;
        _sheet.selectBlock = ^(NSInteger index) {
            [weakSelf dismiss];
            if (weakSelf.selectBlock) {
                weakSelf.selectBlock(index);
            }
        };
    }
    return _sheet;
}

@end


@interface NTESModalSheetBar () <UITableViewDelegate, UITableViewDataSource>
@property (nonatomic, strong) UILabel *titleLab;
@property (nonatomic, strong) UILabel *msgLab;
@property (nonatomic, strong) UIView *line;
@property (nonatomic, strong) UITableView *lists;
@property (nonatomic, strong) NSArray *datas;
@end

@implementation NTESModalSheetBar

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupDatas];
        self.backgroundColor = [UIColor whiteColor];
        [self addSubview:self.titleLab];
        [self addSubview:self.msgLab];
        [self addSubview:self.line];
        [self addSubview:self.lists];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _titleLab.centerX = self.width/2;
    _titleLab.top = 32.0;
    _msgLab.centerX = self.width/2;
    _msgLab.top = _titleLab.bottom + 16.0;
    CGFloat height = 1.0 * [UIScreen mainScreen].scale;
    _line.frame = CGRectMake(0, _msgLab.bottom + 32.0, self.width, height);
    _lists.frame = CGRectMake(0, _line.bottom, self.width, self.height - _line.bottom);
}

- (void)setupDatas {
    _datas = @[@"普通语音", @"高清语音", @"高清音乐（推荐）"];
}

- (void)sizeToFit {
    [super sizeToFit];
    
    CGFloat W = [UIScreen mainScreen].bounds.size.width;
    
    CGFloat height =  32.0 + self.titleLab.height +
                      16.0 + self.msgLab.height + 32.0 +
                      self.lists.rowHeight * MAX(_datas.count, 3) + 1.0 *  (MAX(_datas.count, 3)-1);
    CGFloat width = [UIScreen mainScreen].bounds.size.width - 2*(W * 64.0)/375;
    self.size = CGSizeMake(width, height);
}

#pragma mark - <UITableViewDelegate, UITableViewDataSource>
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return _datas.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"cell" forIndexPath:indexPath];
    if (indexPath.row == _datas.count - 1) {
        cell.separatorInset = UIEdgeInsetsMake(0, 0, 0, cell.bounds.size.width);
    } else {
        cell.separatorInset = UIEdgeInsetsMake(0, 0, 0, 0);
    }
    cell.textLabel.textAlignment = NSTextAlignmentCenter;
    cell.textLabel.textColor = UIColorFromRGB(0x1fa7f0);
    cell.textLabel.font = [UIFont systemFontOfSize:14.0];
    cell.textLabel.text = _datas[indexPath.row];
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    if (_selectBlock) {
        _selectBlock(indexPath.row);
    }
}

#pragma mark - Getter
- (UILabel *)titleLab {
    if (!_titleLab) {
        _titleLab = [[UILabel alloc] init];
        _titleLab.textColor = [UIColor blackColor];
        _titleLab.font = [UIFont systemFontOfSize:17.0];
        _titleLab.text = @"教室音质选择";
        [_titleLab sizeToFit];
    }
    return _titleLab;
}

- (UILabel *)msgLab {
    if (!_msgLab) {
        _msgLab = [[UILabel alloc] init];
        _msgLab.textColor = [UIColor lightGrayColor];
        _msgLab.font = [UIFont systemFontOfSize:11.0];
        _msgLab.text = @"音质越高网络要求越高";
        [_msgLab sizeToFit];
    }
    return _msgLab;
}

- (UIView *)line {
    if (!_line) {
        _line = [[UIView alloc] init];
        _line.backgroundColor = [UIColor groupTableViewBackgroundColor];
    }
    return _line;
}

- (UITableView *)lists {
    if (!_lists) {
        _lists = [[UITableView alloc] initWithFrame:CGRectMake(0, 0, 100, 100)
                                              style:UITableViewStylePlain];
        _lists.bounces = NO;
        _lists.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
        _lists.rowHeight = 44.0;
        _lists.delegate = self;
        _lists.dataSource = self;
        if (@available(iOS 11.0, *)) {
            _lists.contentInsetAdjustmentBehavior = UIScrollViewContentInsetAdjustmentNever;
        }
        [_lists registerClass:[UITableViewCell class] forCellReuseIdentifier:@"cell"];
    }
    return _lists;
}

@end


