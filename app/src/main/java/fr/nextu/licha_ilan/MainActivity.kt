package fr.nextu.licha_ilan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import fr.nextu.licha_ilan.databinding.ActivityMainBinding
import fr.nextu.licha_ilan.entity.AppDatabase
import fr.nextu.licha_ilan.entity.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val db: AppDatabase by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    fun getDatabase(): AppDatabase {
        return db
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        CoroutineScope(Dispatchers.IO).launch {
            handleMenuItemClick(item)
        }
        return true
    }

    private suspend fun handleMenuItemClick(item: MenuItem) {
        val currentLanguage = getDatabase().settingsDao().getLanguage() ?: "fr"
        val newLanguage = when (item.itemId) {
            R.id.action_french -> "fr"
            R.id.action_english -> "en"
            else -> return
        }

        if (currentLanguage != newLanguage) {
            updateLanguage(newLanguage)
        } else {
            showToast(if (newLanguage == "fr") "Le français est déjà sélectionné !" else "English is already selected !")
        }
    }

    private suspend fun updateLanguage(language: String) {
        getDatabase().settingsDao().insertOrUpdate(Settings(1, language))
        withContext(Dispatchers.Main) {
            showToast(if (language == "en") "Changing to English..." else "Changement vers le français...")
            resetData()
        }
    }

    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetData() {
        CoroutineScope(Dispatchers.IO).launch {
            getDatabase().skinDao().deleteAllSkins()
            getDatabase().skinDao().deleteAllCategories()
            getDatabase().skinDao().deleteAllRarities()
        }
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.popBackStack(R.id.categoriesListFragment, false)
        navController.navigate(R.id.categoriesListFragment)
    }

    // Réagit à l'intent envoyé au clic de la notification
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val bundle = Bundle().apply {
            putString("skin_id", intent.getStringExtra("skin_id"))
            putString("category_name", intent.getStringExtra("category_name"))
        }
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(R.id.skinViewFragment, bundle)
    }
}