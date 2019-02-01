package net.osdn.gokigen.pkremote.camera.vendor.olympus.myolycameraprops;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import net.osdn.gokigen.pkremote.R;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;
import androidx.viewpager.widget.ViewPager;

public class LoadSaveMyCameraPropertyDialog extends DialogFragment implements TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener, ILoadSaveMyCameraPropertyDialogDismiss
{
    private final String TAG = toString();
    private ILoadSaveCameraProperties propertyOperations = null;
    private FragmentTabHost tabHost = null;
    private ViewPager viewPager = null;

    public static LoadSaveMyCameraPropertyDialog newInstance(ILoadSaveCameraProperties holder)
    {
        LoadSaveMyCameraPropertyDialog instance = new LoadSaveMyCameraPropertyDialog();
        instance.setPropertyOperationsHolder(holder);
        return (instance);
    }

    private void setPropertyOperationsHolder(ILoadSaveCameraProperties holder)
    {
        propertyOperations = holder;
    }

    /**/
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        try
        {
            View view = inflater.inflate(R.layout.dialog_my_camera_properties, container);
            tabHost = view.findViewById(R.id.tabHost01);
            tabHost.setup(getActivity(), getChildFragmentManager());
            tabHost.addTab(tabHost.newTabSpec("Load").setIndicator(getString(R.string.title_tab_title_load)), Fragment.class, null);
            tabHost.addTab(tabHost.newTabSpec("Save").setIndicator(getString(R.string.title_tab_title_save)), Fragment.class, null);

            tabHost.setOnTabChangedListener(this);

            LoadSaveMyCameraPropertyPagerAdapter adapter = new LoadSaveMyCameraPropertyPagerAdapter(getChildFragmentManager(), this);
            adapter.setTitles(new String[]{getString(R.string.title_tab_title_load), getString(R.string.title_tab_title_save)});

            viewPager = view.findViewById(R.id.pager);
            viewPager.setAdapter(adapter);
            //viewPager.setOnPageChangeListener(this);
            viewPager.addOnPageChangeListener(this);

            getDialog().setTitle(getString(R.string.title_my_settings));
            return (view);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return (null);

    }

    @Override
    public void onTabChanged(String tabId)
    {
        Log.v(TAG, "CHANGED TAB : " + tabId);

        int i = tabHost.getCurrentTab();
        viewPager.setCurrentItem(i);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position)
    {
        Log.v(TAG, "CHANGED PAGER : " + position);
        tabHost.setCurrentTab(position);

    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void doDismissWithPropertyLoad(final String id, final String name)
    {
        dismiss();
        if (propertyOperations != null)
        {
            propertyOperations.loadCameraSettings(id, name);
        }
    }

    @Override
    public void doDismissWithPropertySave(final String id, final String name)
    {
        dismiss();
        if (propertyOperations != null)
        {
            propertyOperations.saveCameraSettings(id, name);
        }
    }
}
