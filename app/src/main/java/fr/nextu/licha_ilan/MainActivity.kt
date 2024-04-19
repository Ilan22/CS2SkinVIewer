package fr.nextu.licha_ilan

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import fr.nextu.licha_ilan.databinding.ActivityMainBinding
import fr.nextu.licha_ilan.entity.Skin
import fr.nextu.licha_ilan.entity.Skins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    val db: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()
        }
    }

    override fun onStart() {
        super.onStart()
        requestSkinsList(::skinsFromJson)
    }

    fun requestSkinsList(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {

            val client = OkHttpClient()

            val request: Request = Request.Builder()
                .url("https://bymykel.github.io/CSGO-API/api/fr/skins.json")
                .get()
                .build()

            val response: Response = client.newCall(request).execute()
            callback(response.body?.string() ?: "")
        }
    }

    fun skinsFromJson(json: String) {
        val jsonArray = JSONArray(json)
        val newJson = JSONObject().apply {
            put("skins", jsonArray)
        }
        val gson = Gson()
        val om = gson.fromJson(newJson.toString(), Skins::class.java)
        db.skinDao().insertAll(*om.skins.toTypedArray())
        Log.d("TEEEEESTT", db.skinDao().getAll().toString())
    }
}