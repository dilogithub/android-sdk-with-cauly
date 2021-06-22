package kr.co.dilo.sample.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.dilo.sample.app.databinding.ActivitySettingsBinding

class SettingsActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewBinding: ActivitySettingsBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
    }

    override fun onBackPressed() {
        val intent: Intent = Intent(this, ContentActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}
