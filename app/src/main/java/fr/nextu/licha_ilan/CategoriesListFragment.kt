package fr.nextu.licha_ilan

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.gson.Gson
import fr.nextu.licha_ilan.databinding.FragmentCategoriesListBinding
import fr.nextu.licha_ilan.entity.AppDatabase
import fr.nextu.licha_ilan.entity.Category
import fr.nextu.licha_ilan.entity.Skin
import fr.nextu.licha_ilan.entity.Skins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

class CategoriesListFragment : Fragment() {

    private lateinit var db: AppDatabase
    private lateinit var language: String

    private var _binding: FragmentCategoriesListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        db = (activity as MainActivity).getDatabase()

        CoroutineScope(Dispatchers.IO).launch {
            language = db.settingsDao().getLanguage() ?: "fr"
            withContext(Dispatchers.Main) {
                (activity as AppCompatActivity).supportActionBar?.title =
                    if (language == "fr") "Catégories" else "Categories"
                binding.progressBar.visibility = View.VISIBLE
                requestSkinsList(::skinsFromJson)
            }
        }
    }

    private fun requestSkinsList(callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            if (db.skinDao().isTableEmpty()) {
                val client = OkHttpClient()

                val request: Request = Request.Builder()
                    .url("https://bymykel.github.io/CSGO-API/api/$language/skins.json")
                    .get()
                    .build()

                val response: Response = client.newCall(request).execute()
                callback(response.body?.string() ?: "")
            } else {
                createCategories()
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.INVISIBLE
            }
        }
    }

    private fun skinsFromJson(json: String) {
        val jsonArray = JSONArray(json)
        val newJson = JSONObject().apply {
            put("skins", jsonArray)
        }
        val gson = Gson()
        val om = gson.fromJson(newJson.toString(), Skins::class.java)
        db.runInTransaction {
            val skinsDao = db.skinDao()
            om.skins.forEach { skin: Skin ->
                val categoryId: String = skin.category.id
                if (categoryId != null && skinsDao.getCategoryById(categoryId) == null) {
                    skinsDao.insertCategory(skin.category)
                }

                val rarityName: String = skin.rarity.name
                if (rarityName != null && skinsDao.getRarityByName(rarityName) == null) {
                    skinsDao.insertRarity(skin.rarity)
                }

                if (skin.category.id != null && skinsDao.getSkinById(skin.id) == null) {
                    skinsDao.insertSkin(skin)
                }
            }
        }

        createCategories()
    }

    private fun createCategories() {
        val categories: List<Category> = db.skinDao().getCategories()

        CoroutineScope(Dispatchers.Main).launch {
            val layoutCategories = binding.layoutCategories

            categories.forEach { category ->
                if (category.id != "") {
                    val button = Button(requireContext()).apply {
                        text = category.name
                        textSize = 16f
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                        background =
                            ContextCompat.getDrawable(context, R.drawable.rounded_background)
                        setTextColor(Color.parseColor("#4F4A6F"))
                        isAllCaps = false
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            topMargin = 20
                            marginStart = 40
                            marginEnd = 40
                        }
                        setPadding(40, 0, 0, 0)
                        setOnClickListener {
                            val bundle = Bundle().apply {
                                putString("category_id", category.id)
                                putString("category_name", category.name)
                            }
                            findNavController().navigate(
                                R.id.action_categoriesListFragment_to_skinsListFragment,
                                bundle
                            )
                        }
                    }
                    layoutCategories.addView(button)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}