package net.osdn.gokigen.pkremote.camera.vendor.visionkids.wrapper.playback

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContent
import net.osdn.gokigen.pkremote.camera.interfaces.playback.ICameraContentListCallback
import net.osdn.gokigen.pkremote.preference.IPreferencePropertyAccessor

class VisionKidsCameraContentProvider(context: AppCompatActivity) : IFtpServiceCallback
{
    private val ftpClient = MyFtpClient(this)
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val cameraContentList = ArrayList<ICameraContent>()
    private lateinit var callback : ICameraContentListCallback

    fun getCameraContent(name: String) : ICameraContent?
    {
        try
        {
            for (cameraContent in cameraContentList)
            {
                if (cameraContent.contentName == name)
                {
                    return (cameraContent)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (null)
    }

    fun forceDisconnect()
    {
        try
        {
            ftpClient.enqueueCommand(FtpCommand("quit", "QUIT\r\n"))
            try
            {
                Thread.sleep(750)
            }
            catch (ee: Exception)
            {
                ee.printStackTrace()
            }
            ftpClient.disconnect()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }


    fun getContentList(callback: ICameraContentListCallback)
    {
        try
        {
            val address = preferences.getString(IPreferencePropertyAccessor.VISIONKIDS_HOST_IP, IPreferencePropertyAccessor.VISIONKIDS_HOST_IP_DEFAULT_VALUE)?: IPreferencePropertyAccessor.VISIONKIDS_HOST_IP_DEFAULT_VALUE
            this.callback = callback
            ftpClient.connect(address)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    // IFtpServiceCallback
    override fun onReceivedFtpResponse(command: String, code: Int, response: String)
    {
        try
        {
            if (code == 0)
            {
                // 成功の応答の場合... FTPのシーケンスを進める
                when (command)
                {
                    "connect" -> inputUser(response)
                    "user" -> inputPass(response)
                    "pass" -> changeCurrentWorkingDirectory(response)
                    "cwd" -> setAsciiTransferMode(response)
                    "ascii" -> setPassiveMode(response)
                    "passive" -> checkPassivePort(response)
                    "data_port" -> getFileList(response)
                    "list" -> checkListCommand(response)
                    "data" -> parseFileList(response)
                    "quit" -> checkQuitResponse(response)
                }
            }
            else
            {
                Log.v(TAG, " onReceivedFtpResponse($command/$code) [${response.length}] $response")
                when (command)
                {
                    "receiveFromDevice(data)" -> onReceivedDataError(response)
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onReceivedDataError(response: String)
    {
        try
        {
            // Occurs data receive timeout, so disconnect ftp
            ftpClient.enqueueCommand(FtpCommand("quit", "QUIT\r\n"))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun inputUser(response: String)
    {
        try
        {
            if (response.startsWith("220"))
            {
                val user = preferences.getString(IPreferencePropertyAccessor.VISIONKIDS_FTP_USER, IPreferencePropertyAccessor.VISIONKIDS_FTP_USER_DEFAULT_VALUE)?: IPreferencePropertyAccessor.VISIONKIDS_FTP_USER_DEFAULT_VALUE

                ftpClient.enqueueCommand(FtpCommand("user", "USER $user\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun inputPass(response: String)
    {
        try
        {
            if (response.startsWith("331"))
            {
                val pass = preferences.getString(IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS, IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS_DEFAULT_VALUE)?: IPreferencePropertyAccessor.VISIONKIDS_FTP_PASS_DEFAULT_VALUE
                ftpClient.enqueueCommand(FtpCommand("pass", "PASS $pass\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun changeCurrentWorkingDirectory(response: String)
    {
        try
        {
            if (response.startsWith("230"))
            {
                ftpClient.enqueueCommand(FtpCommand("cwd", "CWD /1/DCIM\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setAsciiTransferMode(response: String)
    {
        try
        {
            if (response.startsWith("250"))
            {
                ftpClient.enqueueCommand(FtpCommand("ascii", "TYPE A\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setPassiveMode(response: String)
    {
        try
        {
            if (response.startsWith("200"))
            {
                ftpClient.enqueueCommand(FtpCommand("passive", "PASV\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkPassivePort(response: String)
    {
        try
        {
            if (response.startsWith("227"))
            {
                ftpClient.decidePassivePort(response)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun getFileList(response: String)
    {
        try
        {
            ftpClient.openPassivePort(response)
            ftpClient.enqueueCommand(FtpCommand("list", "LIST\r\n"))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun checkListCommand(response: String)
    {
        try
        {
            Log.v(TAG, "RESPONSE: $response")
            if ((response.startsWith("226"))||((response.startsWith("150"))&&(response.contains("226"))))
            {
                ftpClient.enqueueCommand(FtpCommand("quit", "QUIT\r\n"))
            }
            else if (response.startsWith("150"))
            {
                Log.v(TAG, "RESP. 150")
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseFileList(response: String)
    {
        try
        {
            cameraContentList.clear()
            val fileList = response.split("\r\n")
            for (files in fileList)
            {
                val fileData = files.split(Regex("\\s+"))
                if (fileData.size > 8)
                {
                    val imageFile = fileData[8]
                    val imagePath = ""
                    val imageDate = "${fileData[5]} ${fileData[6]} ${fileData[7]}" // MM DD YYYY
                    cameraContentList.add(VisionKidsCameraContent(imageFile, imagePath, imageDate))
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkQuitResponse(response: String)
    {
        try
        {
            Log.v(TAG, "RESPONSE: $response")
            ftpClient.disconnect()

            // 取得した画像の一覧を応答する
            callback.onCompleted(cameraContentList)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = VisionKidsCameraContentProvider::class.java.simpleName
    }
}
