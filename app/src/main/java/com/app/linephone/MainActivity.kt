package com.app.linephone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.linephone.databinding.ActivityInCommingBinding
import com.app.linephone.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    val TAG ="MainActivityTAG"
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val intent = Intent(this@MainActivity, MyService::class.java);
        intent.putExtra("username","10000")
        intent.putExtra("password", "10000")
        startService(intent)

        binding.btnCall.setOnClickListener {
            val intent = Intent(this@MainActivity, InCommingActivity::class.java);
            intent.putExtra("partnerNo","888")
            startActivity(intent)
        }

        if (packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }
    }
}