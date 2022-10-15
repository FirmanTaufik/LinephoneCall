package com.app.linephone

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.linephone.databinding.ActivityInCommingBinding
import org.linphone.core.*
import org.linphone.core.R


class InCommingActivity : AppCompatActivity() ,ServiceConnection  {
    val TAG ="InCommingActivityTAG"
    var myService: MyService?=null
    lateinit var core :Core
    lateinit var binding: ActivityInCommingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityInCommingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this, MyService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)

    }

    private val  coreLister = object :CoreListenerStub(){
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState?, message: String) {
            if (state == RegistrationState.Failed) {
                Log.d(TAG, "onAccountRegistrationStateChanged: RegistrationState.Failed")

            } else if (state == RegistrationState.Ok) {
                Log.d(TAG, "onAccountRegistrationStateChanged: RegistrationState.Ok")

            }

        }
        override fun onAudioDeviceChanged(core: Core, audioDevice: AudioDevice) {
            Log.d(TAG, "onAudioDeviceChanged: ")
            // This callback will be triggered when a successful audio device has been changed
        }

        override fun onAudioDevicesListUpdated(core: Core) {
            Log.d(TAG, "onAudioDevicesListUpdated: ")
            // This callback will be triggered when the available devices list has changed,
            // for example after a bluetooth headset has been connected/disconnected.
        }

        override fun onCallStateChanged(p0: Core, call: Call, state: Call.State?, p3: String) {
            Log.d(TAG, "onCallStateChanged: "+state)
            when (state) {
                Call.State.IncomingReceived -> {
                    Log.d(TAG, "onCallStateChanged: IncomingReceived")
                }
                Call.State.Connected -> {
                    Log.d(TAG, "onCallStateChanged: Connected")
                }
                Call.State.Released -> {
                    finish()
                    Log.d(TAG, "onCallStateChanged: Released")
                }
                Call.State.StreamsRunning ->  {

                }
            }

        }

    }


    override fun onServiceConnected(p0: ComponentName?, iBinder: IBinder?) {
        Log.d(TAG, "onServiceConnected: ")
        val binder  = iBinder as MyService.MyBinder
        myService = binder.service
        core = myService!!.getCoreLinePhone()!!
        core.addListener(coreLister)
        initButtonOnClick()
        if (intent.hasExtra("partnerNo")){
            outgoingCall()
        }
    }

    private fun outgoingCall() {
        val remoteAddress = Factory.instance().createAddress("sip:"+intent.getStringExtra("partnerNo")+"@"
                +resources.getString(com.app.linephone.R.string.domain))
        remoteAddress ?: return

        val params = core.createCallParams(null)
        params ?: return

        params.mediaEncryption = MediaEncryption.None
        core.inviteAddressWithParams(remoteAddress, params)
    }


    private fun initButtonOnClick() {
        core.config.setBool("video", "auto_resize_preview_to_keep_ratio", true)
        core.nativeVideoWindowId =  binding.remoteVideoSurface
        core.nativePreviewWindowId = binding.localPreviewVideoSurface
        core.enableVideoCapture(true)
        core.enableVideoDisplay(true)
        core.videoActivationPolicy.automaticallyAccept = true

        binding.btnAnswer.setOnClickListener {
            core.currentCall?.accept()
        }
        binding.btnHangUp.setOnClickListener {
            core.currentCall?.terminate()
        }
        binding.btnMute.setOnClickListener {
            core.enableMic(!core.micEnabled())
        }
        binding.btnVidCall.setOnClickListener {
            toggleVideo()
        }
    }

    private fun toggleVideo() {
        if (core.callsNb == 0) return
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        // We will need the CAMERA permission for video call
        if (packageManager.checkPermission(Manifest.permission.CAMERA, packageName) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 0)
            return
        }

        // To update the call, we need to create a new call params, from the call object this time
        val params = core.createCallParams(call)
        // Here we toggle the video state (disable it if enabled, enable it if disabled)
        // Note that we are using currentParams and not params or remoteParams
        // params is the object you configured when the call was started
        // remote params is the same but for the remote
        // current params is the real params of the call, resulting of the mix of local & remote params
  //      params?.enableVideo(!call.currentParams.videoEnabled())
        // Finally we request the call update
        params?.enableVideo(true)
        call.update(params)

        // Note that when toggling off the video, TextureViews will keep showing the latest frame displayed
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        Log.d(TAG, "onServiceDisconnected: ")

    }
}