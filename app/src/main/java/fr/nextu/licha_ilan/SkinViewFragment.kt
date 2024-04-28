package fr.nextu.licha_ilan

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import coil.load
import fr.nextu.licha_ilan.databinding.FragmentSkinViewBinding
import fr.nextu.licha_ilan.entity.AppDatabase
import fr.nextu.licha_ilan.entity.Skin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [SkinViewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SkinViewFragment : Fragment() {

    private lateinit var db: AppDatabase

    lateinit var skinId: String
    lateinit var categoryName: String
    private lateinit var language: String

    private var _binding: FragmentSkinViewBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            skinId = it.getString("skin_id").toString()
            categoryName = it.getString("category_name").toString()
        }
    }

    override fun onStart() {
        super.onStart()
        db = (activity as MainActivity).getDatabase()
        (activity as AppCompatActivity).supportActionBar?.title = categoryName

        CoroutineScope(Dispatchers.IO).launch {
            language = db.settingsDao().getLanguage() ?: "fr"

            val skin: Skin? = db.skinDao().getSkinById(skinId)

            skin?.let {
                withContext(Dispatchers.Main) {
                    binding.skinImage.load(skin.image)
                    binding.skinName.text = skin.name.substringAfter("| ")
                    binding.skinWeapon.text = skin.weapon.name
                    binding.skinRarity.text = skin.rarity.name
                    binding.skinRarity.setTextColor(Color.parseColor(skin.rarity.color))

                    val orientation = resources.configuration.orientation
                    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                        if (skin.description.contains("\\n")) {
                            binding.skinDescriptionFirst?.text =
                                skin.description.substringBefore("\\n\\n<i>")
                            binding.skinDescriptionSecond?.text =
                                skin.description.substringAfter("\\n\\n<i>").substringBefore("</i>")
                                    .replace("\\", "")
                        } else {
                            binding.skinDescriptionFirst?.text =
                                skin.description
                            binding.skinDescriptionSecond?.text = ""
                        }
                        binding.rotatePhone?.text =
                            getString(if (language == "fr") R.string.rotate_phone_french else R.string.rotate_phone_english)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkinViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}