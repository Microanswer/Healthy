package android.microanswer.healthy.fragment;


import android.microanswer.healthy.R;
import android.microanswer.healthy.adapter.RecyclerViewAdapter;
import android.microanswer.healthy.tools.BaseTools;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 由 Micro 创建于 2016/6/30.
 */

public class HealthyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, RecyclerViewAdapter.RefreshListener {
    private View root = null;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_healthy, null, true);

        }
        initData();
        initView();
        return root;
    }

    private void initView() {
        if (recyclerView == null) {
            recyclerView = (RecyclerView) root.findViewById(R.id.viewpager_healthy_recyclerview);
        }
        if (swipeRefreshLayout == null) {
            swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.viewpager_healthy_swiperefreshlayout);
        }
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent, R.color.blue_light);
        swipeRefreshLayout.setOnRefreshListener(this);
        recyclerView.addOnScrollListener(scrollListener);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.generateOnlineData();
        Log.i("HealthyFragment", "设置适配器完成");
    }

    /**
     * 滑动监听器
     */
    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            Log.i("HealthyFragment", "newState=" + newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isSlideToBottom(recyclerView)) {
                recyclerViewAdapter.appendHealthyInfo();
            }
        }
    };

    /**
     * 是否滑动到底部【这里做了修改，只要达到距离底部400dp的位置就开始加载更多内容】
     * @param recyclerView
     * @return
     */
    protected boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        return recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= (recyclerView.computeVerticalScrollRange() - BaseTools.Dp2Px(getActivity(), 400));
    }


    private void initData() {
        recyclerViewAdapter = new RecyclerViewAdapter(getActivity(), this);
        recyclerViewAdapter.setRefreshListener(this);
        linearLayoutManager = new LinearLayoutManager(getActivity());
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        recyclerViewAdapter.generateOnlineData();
    }

    @Override
    public void onRefreshEnd() {
        swipeRefreshLayout.setRefreshing(false);
    }

}
