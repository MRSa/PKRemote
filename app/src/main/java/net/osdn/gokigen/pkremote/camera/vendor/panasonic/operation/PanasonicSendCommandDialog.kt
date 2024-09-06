package net.osdn.gokigen.pkremote.camera.vendor.panasonic.operation

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import net.osdn.gokigen.pkremote.R
import net.osdn.gokigen.pkremote.camera.utils.SimpleHttpClient
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.IPanasonicInterfaceProvider
import net.osdn.gokigen.pkremote.camera.vendor.panasonic.wrapper.IPanasonicCamera

class PanasonicSendCommandDialog : DialogFragment(), View.OnClickListener
{
    private lateinit var interfaceProvider: IPanasonicInterfaceProvider
    private var camera: IPanasonicCamera? = null
    private var service: EditText? = null
    private var parameter: EditText? = null
    private var command: EditText? = null
    private var responseArea: TextView? = null

    private fun prepare(interfaceProvider: IPanasonicInterfaceProvider)
    {
        this.interfaceProvider = interfaceProvider
        this.camera = interfaceProvider.getPanasonicCamera()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
    {
        // コマンド送信ダイアログの生成
        val activity = requireActivity()
        val alertDialog = AlertDialog.Builder(activity)
        try
        {
            val inflater = activity.layoutInflater
            val alertView = inflater.inflate(R.layout.panasonic_request_layout, null, false)
             alertDialog.setView(alertView)

            alertDialog.setIcon(R.drawable.ic_linked_camera_black_24dp)
            alertDialog.setTitle(activity.getString(R.string.dialog_panasonic_command_title_command))
            service = alertView.findViewById(R.id.edit_service)
            parameter = alertView.findViewById(R.id.edit_parameter)
            command = alertView.findViewById(R.id.edit_command)
            responseArea = alertView.findViewById(R.id.panasonic_command_response_value)
            val sendButton = alertView.findViewById<Button>(R.id.send_message_button)
            val toRunningButton = alertView.findViewById<Button>(R.id.change_to_liveview)
            val toPlaybackButton = alertView.findViewById<Button>(R.id.change_to_playback)

            toRunningButton.setOnClickListener(this)
            toPlaybackButton.setOnClickListener(this)
            sendButton.setOnClickListener(this)
            alertDialog.setCancelable(true)
            try
            {
                service?.setText(activity.getText(R.string.panasonic_service_string))
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }

            // ボタンを押したら閉じるようにする
            alertDialog.setPositiveButton(activity.getString(R.string.dialog_positive_execute)) { dialog, _ -> dialog.dismiss() }
            alertDialog.setNegativeButton(activity.getString(R.string.dialog_negative_cancel)) { dialog, _ -> dialog.cancel() }
        }
        catch (ee: Exception)
        {
            ee.printStackTrace()
        }
        return (alertDialog.create())
    }

    private fun changeRunMode(isStartLiveView: Boolean)
    {
        // ライブビューの停止と開始
        Log.v(TAG, "changeRunMode() : $isStartLiveView")
        val liveViewControl = interfaceProvider.getPanasonicLiveViewControl()
        try
        {
            if (isStartLiveView)
            {
                liveViewControl?.startLiveView(false)
            }
            else
            {
                liveViewControl?.stopLiveView()
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    override fun onClick(view: View)
    {
        if (view.id == R.id.change_to_liveview)
        {
            changeRunMode(true)
            return
        }
        if (view.id == R.id.change_to_playback)
        {
            changeRunMode(false)
            return
        }

        try
        {
            var serviceStr: String
            var commandStr: String
            val activity: Activity? = activity
            if (activity != null)
            {
                serviceStr = service?.text.toString()
                commandStr = command?.text.toString()
                val isPost = (serviceStr.contains("post"))
                val parameterStr = parameter?.text.toString()
                if ((!isPost) && (parameterStr.isNotEmpty()))
                {
                    commandStr = "$commandStr&$parameterStr"
                }

                serviceStr = if (serviceStr.contains("pic"))
                {
                    camera?.getPictureUrl() + commandStr
                } else if (serviceStr.contains("obj"))
                {
                    camera?.getObjUrl() + commandStr
                } else
                {
                    camera?.getCmdUrl() + serviceStr + "?" + commandStr
                }
                val url = serviceStr
                val sessionId = camera?.getCommunicationSessionId()
                val thread = Thread {
                    try
                    {
                        val reply = if (!sessionId.isNullOrEmpty())
                        {
                            val headerMap: MutableMap<String, String> = HashMap()
                            headerMap["X-SESSION_ID"] = sessionId

                            if (isPost) {
                                SimpleHttpClient.httpPostWithHeader(url, parameterStr, headerMap, null, TIMEOUT_MS)
                            }
                            else
                            {
                                SimpleHttpClient.httpGetWithHeader(url, headerMap, null, TIMEOUT_MS)
                            }
                        }
                        else
                        {
                            if (isPost)
                            {
                                SimpleHttpClient.httpPost(url, parameterStr, TIMEOUT_MS)
                            } else {
                                SimpleHttpClient.httpGet(url, TIMEOUT_MS)
                            }
                        }
                        Log.v(TAG, "URL : $url RESPONSE : $reply")

                        activity.runOnUiThread(Runnable { responseArea?.text = reply })
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                    }
                }
                thread.start()
            }
            else
            {
                Log.v(TAG, "getActivity() Fail...")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private const val TIMEOUT_MS = 2000
        private val TAG: String = PanasonicSendCommandDialog::class.java.simpleName

        fun newInstance(interfaceProvider: IPanasonicInterfaceProvider): PanasonicSendCommandDialog {
            val instance = PanasonicSendCommandDialog()
            instance.prepare(interfaceProvider)

            // パラメータはBundleにまとめておく
            val arguments = Bundle()
            //arguments.putString("method", method);
            //arguments.putString("message", message);
            instance.arguments = arguments

            return (instance)
        }
    }
}