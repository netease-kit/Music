package com.netease.nim.musiceducation.business.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.netease.nim.musiceducation.BuildConfig;
import com.netease.nim.musiceducation.R;
import com.netease.nim.musiceducation.common.ui.ToolBarOptions;
import com.netease.nim.musiceducation.common.ui.UI;


public class AboutActivity extends UI {

    private TextView versionGit;
    private TextView versionDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);

        ToolBarOptions options = new ToolBarOptions();
        options.titleString = "关于";
        options.isNeedNavigate = true;
        options.navigateId = R.mipmap.ic_white_back;
        setToolBar(R.id.toolbar, options);

        findViews();
        initViewData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void findViews() {
        versionGit = findViewById(R.id.version_detail_git);
        versionDate = findViewById(R.id.version_detail_date);
    }

    private void initViewData() {
        versionGit.setText("Git Version: " + BuildConfig.GIT_REVISION);
        versionDate.setText("Build Date:" + BuildConfig.BUILD_DATE);
    }
}
