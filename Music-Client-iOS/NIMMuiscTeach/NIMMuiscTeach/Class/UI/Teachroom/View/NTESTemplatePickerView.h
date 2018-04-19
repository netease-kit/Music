//
//  NTESTemplatePickerView.h
//  NIMMuiscTeach
//
//  Created by emily on 2018/4/3.
//  Copyright © 2018 netease. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, NTESTemplatePickerType) {
    NTESTemplatePickerTypeVideo = 1,
    NTESTemplatePickerTypeColor,
};

@class NTESTemplatePickerView;
@protocol NTESTemplatePickerViewDelegate <NSObject>

- (void)templatePickerView:(NTESTemplatePickerView *)pickerView didSelectModeAtIndex:(NSInteger)index;

- (void)templatePickerView:(NTESTemplatePickerView *)pickerView didSelectColorAtIndex:(NSInteger)index;

@end

@interface NTESTemplatePickerView : UIView

@property(nonatomic, assign) NTESTemplatePickerType templateType;

@property(nonatomic, weak) id<NTESTemplatePickerViewDelegate> delegate;

- (instancetype)initWithFrame:(CGRect)frame templateType:(NTESTemplatePickerType)type;

@end
