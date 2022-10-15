package com.app.linephone

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import org.linphone.core.*


class MyService : Service() {
    var TAG = "MyServiceTAG"
    var i = 1
    private val binder: IBinder = MyBinder()
    var core: Core?=null

    override fun onCreate() {
        super.onCreate()
        val factory = Factory.instance()
        factory.setDebugMode(true, "Hello Linphone")
        core = factory.createCore(null, null, this)
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        intLinePhone(intent.getStringExtra("username")!!,intent.getStringExtra("password")!!);
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show()


        // hitung();
        return START_NOT_STICKY
    }

    private fun intLinePhone(username :String, password:String) {
        val domain =  getString(com.app.linephone.R.string.domain)

        val authInfo = Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)

        val params = core!!.createAccountParams()
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        params.identityAddress = identity

        val address = Factory.instance().createAddress("sip:$domain")
        address?.transport = TransportType.Tcp
        params.serverAddress = address
        params.registerEnabled = true
        val account = core!!.createAccount(params)

        core!!.addAuthInfo(authInfo)
        core!!.addAccount(account)

        core!!.defaultAccount = account
        core!!.addListener(object : CoreListenerStub() {
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

            override fun onCallStateChanged(p0: Core, p1: Call, state: Call.State?, p3: String) {
                Log.d(TAG, "onCallStateChanged: "+state)
                when (state) {
                    Call.State.IncomingReceived -> {
                        val dialogIntent = Intent(applicationContext, InCommingActivity::class.java)
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(dialogIntent)
                        Log.d(TAG, "onCallStateChanged: IncomingReceived")
                    }
                    Call.State.Connected -> {
                        Log.d(TAG, "onCallStateChanged: Connected")
                    }
                    Call.State.Released -> {
                        Log.d(TAG, "onCallStateChanged: Released")
                    }
                }
            }
        })
        core!!.start()

    }

    private fun hitung() {
        Thread {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    Log.d(TAG, "run: " + i++)
                    Thread.sleep(1500)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                //REST OF CODE HERE//
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this, "Service destroyed by user.", Toast.LENGTH_LONG).show()
    }

    inner  class MyBinder : Binder() {
        val service: MyService get() = this@MyService
    }

    public  fun getCoreLinePhone(): Core? {
        return core
    }
}