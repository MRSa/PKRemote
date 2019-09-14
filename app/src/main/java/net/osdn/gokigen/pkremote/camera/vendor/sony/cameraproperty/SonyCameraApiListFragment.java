package net.osdn.gokigen.pkremote.camera.vendor.sony.cameraproperty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import net.osdn.gokigen.pkremote.R;
import net.osdn.gokigen.pkremote.camera.interfaces.IInterfaceProvider;
import net.osdn.gokigen.pkremote.scene.ConfirmationDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SonyCameraApiListFragment extends ListFragment implements SendRequestDialog.Callback
{
    private final String TAG = toString();
    private ArrayAdapter<String> adapter;
    private List<String> dataItems = new ArrayList<>();
    private IInterfaceProvider interfaceProvider = null;


    /**
     *  カメラプロパティをやり取りするインタフェースを生成する
     *
     */
    public static SonyCameraApiListFragment newInstance(@NonNull IInterfaceProvider interfaceProvider)
    {
        SonyCameraApiListFragment instance = new SonyCameraApiListFragment();
        instance.prepare(interfaceProvider);

        // パラメータはBundleにまとめておく
        Bundle arguments = new Bundle();
        //arguments.putString("title", title);
        //arguments.putString("message", message);
        instance.setArguments(arguments);

        return (instance);
    }

    /**
     *
     *
     */
    private void prepare(@NonNull IInterfaceProvider interfaceProvider)
    {
        Log.v(TAG, "prepare()");
        this.interfaceProvider = interfaceProvider;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.api_view, menu);
        String title = getString(R.string.app_name) + " " + getString(R.string.pref_sony_api_list);
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null)
            {
                ActionBar bar = activity.getSupportActionBar();
                if (bar != null)
                {
                    bar.setTitle(title);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_refresh)
        {
            update();
            return (true);
        }
        if (item.getItemId() == R.id.action_share)
        {
            share();
            return (true);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *   API一覧の他アプリへ共有
     *
     */
    private void share()
    {
        if ((dataItems != null)&&(dataItems.size() > 0))
        {
            try
            {
                StringBuilder shareData = new StringBuilder();
                for (String item : dataItems)
                {
                    shareData.append(item);
                    shareData.append("\r\n");
                }
                String title = "; " + getString(R.string.pref_sony_api_list);
                Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, new String(shareData));
                FragmentActivity activity = getActivity();
                if (activity != null)
                {
                    // Intent発行(ACTION_SEND)
                    startActivity(sendIntent);
                    Log.v(TAG, "<<< SEND INTENT >>> : " + title);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     *   表示データの更新
     *
     */
    private void update()
    {
        try
        {
            if (dataItems != null)
            {
                dataItems.clear();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Log.v(TAG, "START GET API LIST");
                dataItems = interfaceProvider.getSonyInterface().getApiCommands();
                Log.v(TAG, "FINISH GET API LIST");
                try
                {
                    // 追加の追加する
                    String addMethodText = "(free)";
                    if (getActivity() != null)
                    {
                        addMethodText = getActivity().getString(R.string.free_method_name);
                    }
                    dataItems.add(addMethodText);

                    final FragmentActivity activity = getActivity();
                    if (activity != null)
                    {
                        activity.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    // 中身があったらクリアする
                                    if (adapter.getCount() > 0)
                                    {
                                        adapter.clear();
                                    }

                                    // リストの内容を更新する
                                    adapter.addAll(dataItems);

                                    // 最下部にカーソルを移したい
                                    ListView view = activity.findViewById(android.R.id.list);
                                    view.setSelection(dataItems.size());

                                    // 更新終了通知
                                    Toast.makeText(getActivity(), getString(R.string.finish_refresh), Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception ee)
                                {
                                    ee.printStackTrace();
                                }
                            }
                        });
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        try
        {
            // 本当は、ここでダイアログを出したい
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.v(TAG, "onResume()");

        update();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.v(TAG, "onPause()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "SonyCameraApiListFragment::onCreate()");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.v(TAG, "SonyCameraApiListFragment::onActivityCreated()");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        adapter = new ArrayAdapter<>(inflater.getContext(), android.R.layout.simple_list_item_1, dataItems);
        setListAdapter(adapter);
        return (super.onCreateView(inflater, container, savedInstanceState));
    }

    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
        try
        {
            ListAdapter listAdapter = l.getAdapter();
            final String apiName = (String) listAdapter.getItem(position);
            final SendRequestDialog.Callback apiCallback = this;
            Log.v(TAG, "onListItemClick() [" + position + "] " + apiName);
            Activity activity =  getActivity();
            if (activity != null)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        SendRequestDialog dialog = SendRequestDialog.newInstance(interfaceProvider.getSonyInterface().getCameraApi(), apiName, apiCallback);
                        FragmentManager manager = getFragmentManager();
                        String tag = "dialog";
                        if (manager != null)
                        {
                            dialog.show(manager, tag);
                        }
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     *   API のコマンドを発行する。
     *
     *   ※ 注意： 引数パラメータはカンマ区切りで複数個を入力してもらう
     *      key & Value  値 keyとvalueのあいだを : で区切る (key:value みたいな感じ)
     *      $T           Boolean値の True
     *      $F           Boolean値の False
     *      #xx          数値(integer)
     *      #xx.xx       数値(double)
     */
    @Override
    public void sendRequest(final String service, final String apiName, final String parameter, final String version)
    {
        String logValue = "sendRequest(" + service + ", " + apiName + ", [ " + parameter + "], " + version + ");";
        Log.v(TAG, logValue);
        try
        {
            Thread thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // parameterを parseして、メッセージを送信する
                        JSONArray params = parseParams(parameter);
                        receivedReply(interfaceProvider.getSonyInterface().getCameraApi().callGenericSonyApiMethod(service, apiName, params, version));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private JSONArray parseParams(final String parameter)
    {
        JSONArray params = new JSONArray();
        final String[] parameterItems = parameter.split(",");
        if (parameter.length() != 0)
        {
            for (int index = 0; index < parameterItems.length; index++)
            {
                String oneItem = parameterItems[index];
                if (oneItem.contains(":"))
                {
                    // key & value と判断
                    try
                    {
                        String[] keyValue = oneItem.split(":");
                        try
                        {
                            String key = keyValue[0];
                            String value = keyValue[1];
                            if (value.contains("$T"))
                            {
                                params.put(new JSONObject().put(key, true));
                            }
                            else if (value.contains("$F"))
                            {
                                params.put(new JSONObject().put(key, false));
                            }
                            else if (value.contains("#"))
                            {
                                if (value.contains("."))
                                {
                                    double doubleValue = Double.parseDouble(value.substring(1));
                                    params.put(new JSONObject().put(key, doubleValue));
                                }
                                else
                                {
                                    int intValue = Integer.parseInt(value.substring(1));
                                    params.put(new JSONObject().put(key, intValue));
                                }
                            }
                            else
                            {
                                params.put(new JSONObject().put(keyValue[0], keyValue[1]));
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            params.put(oneItem);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        params.put(oneItem);
                    }
                } else {
                    try
                    {
                        if (oneItem.contains("$T"))
                        {
                            params.put(true);
                        }
                        else if (oneItem.contains("$F"))
                        {
                            params.put(false);
                        }
                        else if (oneItem.contains("#"))
                        {
                            if (oneItem.contains("."))
                            {
                                double doubleValue = Double.parseDouble(oneItem.substring(1));
                                params.put(doubleValue);
                            } else {
                                int intValue = Integer.parseInt(oneItem.substring(1));
                                params.put(intValue);
                            }
                        }
                        else
                        {
                            params.put(oneItem);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        params.put(oneItem);
                    }
                }
            }
        }
        return (params);
    }

    @Override
    public void cancelled()
    {
        Log.v(TAG, "cancelled()");
    }


    private void receivedReply(final JSONObject reply)
    {
        try
        {
            final Activity activity =  getActivity();
            if (activity != null)
            {
                activity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            ConfirmationDialog dialog = ConfirmationDialog.newInstance(getActivity());
                            String replyString;
                            try
                            {
                                replyString = reply.getString("result");
                            }
                            catch (Exception ee)
                            {
                                replyString = reply.getString("results");
                            }
                            dialog.show(android.R.drawable.ic_dialog_info, getString(R.string.dialog_title_reply), replyString);
                        }
                        catch (Exception e)
                        {
                            ConfirmationDialog dialog = ConfirmationDialog.newInstance(getActivity());
                            String replyString = "";
                            try
                            {
                                replyString = reply.toString(4);
                            }
                            catch (Exception ee)
                            {
                                ee.printStackTrace();
                            }
                            dialog.show(android.R.drawable.ic_dialog_alert, getString(R.string.dialog_title_reply), "RECEIVE ERROR \r\n" + replyString);
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}
