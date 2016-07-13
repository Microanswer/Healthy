package android.microanswer.healthy;

import android.animation.Animator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.microanswer.healthy.bean.User;
import android.microanswer.healthy.fragment.HealthyFragment;
import android.microanswer.healthy.fragment.LifeFragment;
import android.microanswer.healthy.fragment.MoreFragment;
import android.microanswer.healthy.tools.InternetServiceTool;
import android.microanswer.healthy.view.MActionBarDrawerToggle;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.internal.NavigationMenuPresenter;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Map;

public class MainActivity extends BaseActivity implements BottomNavigationBar.OnTabSelectedListener, NavigationView.OnNavigationItemSelectedListener, ViewPager.OnPageChangeListener {
    public static final String SHAREDPREFERENCES_KEY_WEALTHY = "skw";
    public static final String SHAREDPREFERENCES_KEY_WEALTHY_TIME = "skwt";

    private static final int THREAD_WEALTHY_LOAD = 1;
    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private TextView wealther_tv, nick_name_tv, singnature_tv;
    private NavigationView navigationView;
    private BottomNavigationBar bottomNavigationBar;
    private SharedPreferences sharedPreferences;
    private FragmentManager fragmentManager;
    private Fragment[] fragments;
    MActionBarDrawerToggle mActionBarDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initview();
    }

    /**
     * 初始化控件
     */
    private void initview() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        sharedPreferences = getSharedPreferences("mainactivity", MODE_PRIVATE);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.activity_main_drawerlayout);
        navigationView = (NavigationView) findViewById(R.id.activity_main_navigationview);
        navigationView.setNavigationItemSelectedListener(this);
        try {
            Field f = navigationView.getClass().getDeclaredField("mPresenter");
            f.setAccessible(true);
            NavigationMenuPresenter menuPresenter = (NavigationMenuPresenter) f.get(navigationView);
            ((View) menuPresenter.getMenuView(navigationView)).setBackgroundResource(R.color.whilte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        singnature_tv = (TextView) navigationView.getHeaderView(0).findViewById(R.id.view_sliding_menu_singnature);
        nick_name_tv = (TextView) navigationView.getHeaderView(0).findViewById(R.id.view_sliding_menu_nickname);
        wealther_tv = (TextView) navigationView.getHeaderView(0).findViewById(R.id.view_sliding_menu_textview_wealther);
        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.activity_main_bottomnavigationbar);

        fragmentManager = getSupportFragmentManager();
        fragments = new Fragment[3];

        mActionBarDrawerToggle = new MActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_navigation, R.string.close_navigation);
        mActionBarDrawerToggle.setNavigationView(navigationView);
        mActionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(mActionBarDrawerToggle);
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.healthy, R.string.app_name).setActiveColorResource(R.color.whilte).setInActiveColorResource(R.color.colorPrimary));
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.life, R.string.life).setActiveColorResource(R.color.whilte).setInActiveColorResource(R.color.colorPrimary));
        bottomNavigationBar.addItem(new BottomNavigationItem(R.mipmap.more, R.string.more).setActiveColorResource(R.color.whilte).setInActiveColorResource(R.color.colorPrimary));
        bottomNavigationBar.setTabSelectedListener(this);
        bottomNavigationBar.initialise();

    }

    /**
     * 初始化数据[onResume里调用]
     */
    private void initdataa() {
        wealther_tv.setText(sharedPreferences.getString(SHAREDPREFERENCES_KEY_WEALTHY, "重试"));
        if ((System.currentTimeMillis() - sharedPreferences.getLong(SHAREDPREFERENCES_KEY_WEALTHY_TIME, 0)) >= 1000 * 60 * 30
                || sharedPreferences.getString(SHAREDPREFERENCES_KEY_WEALTHY, "重试").equals("重试")
                ) {//半小时自动刷新天气一次
            runOnOtherThread(new WealtherLoader(), THREAD_WEALTHY_LOAD);
        }
        if (TextUtils.isEmpty(User.getUser().getAccess_token()) && TextUtils.isEmpty(User.getUser().getAccount())) {
            nick_name_tv.setText(getString(R.string.defultnickname));
            singnature_tv.setVisibility(View.GONE);
        } else {
            nick_name_tv.setText(User.getUser().getAccount());
            singnature_tv.setText(User.getUser().getSignature());
            singnature_tv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mActionBarDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_toolbar_search://搜索框
                break;
            case R.id.view_sliding_menu_head://侧滑菜单登录点击
            case R.id.view_sliding_menu_nickname:
//                drawerLayout.closeDrawers();
                if (TextUtils.isEmpty(User.getUser().getAccount())) {
                    jumpForResultTo(LoginActivity.class, true, LoginActivity.REQUEST_LOGIN);
                } else {
                    jumpTo(UserCenterActivity.class, true);
                }
                break;
            case R.id.view_sliding_menu_textview_wealther://天气
                wealther_tv.setText(getString(R.string.wealther_load));
                runOnOtherThread(new WealtherLoader(), THREAD_WEALTHY_LOAD);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == LoginActivity.REQUEST_LOGIN) {
            nick_name_tv.setText(User.getUser().getAccount());
            toast("登录成功", POSOTION_TOP);
        }
    }

    @Override
    public void onOtherThreadRunEnd(int id, Message msg) {
        super.onOtherThreadRunEnd(id, msg);
        if (id == THREAD_WEALTHY_LOAD) {
            String info = msg.obj.toString();
            wealther_tv.setText(info);
            sharedPreferences.edit().putString(SHAREDPREFERENCES_KEY_WEALTHY, info).putLong(SHAREDPREFERENCES_KEY_WEALTHY_TIME, System.currentTimeMillis()).apply();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        initdataa();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {// 侧滑菜单如果已经开启，关闭侧滑
            drawerLayout.closeDrawer(Gravity.LEFT);
            if (Build.VERSION.SDK_INT >= 21) {
                Animator animator = ViewAnimationUtils.createCircularReveal(navigationView, 0, 0, (int) Math.hypot(getScreenHeight(), getScreenWidth()), 0);
                animator.setDuration(400);
                animator.start();
            }
            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onTabSelected(int position) {
        if (fragments[position] == null) {
            fragments[position] = (position == 0 ? new HealthyFragment() : (position == 1 ? new LifeFragment() : new MoreFragment()));
        }
        fragmentManager.beginTransaction().replace(R.id.activity_main_fragment_content, fragments[position]).commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        drawerLayout.closeDrawers();
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(300);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (item.getItemId()) {
                            case R.id.menu_exit:
                                finish();
                                break;
                            case R.id.menu_set:
                                jumpTo(SetActivity.class,true);
                                break;
                            case R.id.menu_about:

                                break;
                            case R.id.menu_usercenter://个人中心
                                if (TextUtils.isEmpty(User.getUser().getAccount())) {
                                    jumpForResultTo(LoginActivity.class, true, LoginActivity.REQUEST_LOGIN);
                                } else {
                                    jumpTo(UserCenterActivity.class, true);
                                }
                                break;
                            case R.id.menu_givemeny:
                                break;
                            case R.id.menu_hint:
                                break;
                        }
                    }
                });

            }
        }).start();
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        bottomNavigationBar.selectTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class WealtherLoader implements OtherThreadTask {

        @Override
        public Map<Object, Object> getTaskParams() {
            return null;
        }

        @Override
        public Message run(Map params) {

            String weakther = "";
            try {
                String res = InternetServiceTool.Weather.get("chengdu");
                JSONObject jsonObject = new JSONObject(res);
                i(jsonObject.toString());
                JSONArray jas = jsonObject.getJSONArray("HeWeather data service 3.0");
                jsonObject = jas.getJSONObject(0);
                weakther = "  " + jsonObject.getJSONObject("basic").getString("city");
                jsonObject = jsonObject.getJSONObject("now");
                weakther += " " + jsonObject.getJSONObject("cond").getString("txt") + " ";
                weakther += jsonObject.getString("tmp") + "°";
            } catch (Exception e) {
                e.printStackTrace();
                weakther = "    重试";
            }

            Message message = new Message();
            message.obj = weakther;
            return message;
        }
    }

}
