package com.example.notihub.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            enableEdgeToEdge() // 이 부분에서 예외가 발생할 수 있으므로, 잘못된 구성이 있는지 확인 필요

            // 권한 요청 코드
            val requestLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    Log.d("kkang", "callback.. granted..")
                    handleFirstRun()
                } else {
                    Log.d("kkang", "callback.. denied..")
                    Toast.makeText(this, "알림 권한이 거부되었습니다. 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            // 권한 상태 체크
            val status = ContextCompat.checkSelfPermission(this, "android.permission.POST_NOTIFICATIONS")
            if (status == PackageManager.PERMISSION_GRANTED) {
                Log.d("kkang", "permission granted")
                handleFirstRun()
            } else {
                // 권한 요청
                requestLauncher.launch("android.permission.POST_NOTIFICATIONS")
            }
        } catch (e: Exception) {
            // 예외 발생 시 로그를 출력하고 종료되지 않도록 처리
            Log.e("MainActivity", "Error in onCreate: ${e.message}")
            e.printStackTrace()
            Toast.makeText(this, "앱을 시작하는 중에 오류가 발생했습니다.", Toast.LENGTH_LONG).show()
        }
    }

    // 첫 실행 처리 및 Fragment 전환을 위한 함수
    private fun handleFirstRun() {
        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            Toast.makeText(this, "앱이 처음 실행되었습니다!", Toast.LENGTH_SHORT).show()
            val editor = sharedPreferences.edit()
            editor.putBoolean("isFirstRun", false)
            editor.apply()

            // IntroFragment로 이동
            startActivity(Intent(this, IntroActivity::class.java))
            Toast.makeText(this, "앱이 처음 실행되었습니다?", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "앱을 다시 실행했습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ItemListActivity::class.java))
        }
        finish()
    }
}
