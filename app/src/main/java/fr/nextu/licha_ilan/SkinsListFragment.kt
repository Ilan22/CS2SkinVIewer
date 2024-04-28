package fr.nextu.licha_ilan

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.sqlite.db.SimpleSQLiteQuery
import fr.nextu.licha_ilan.databinding.FragmentSkinsListBinding
import fr.nextu.licha_ilan.entity.AppDatabase
import fr.nextu.licha_ilan.entity.Rarity
import fr.nextu.licha_ilan.entity.RaritySpinnerListAdapter
import fr.nextu.licha_ilan.entity.SkinAdapter
import fr.nextu.licha_ilan.entity.Skins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [SkinsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SkinsListFragment : Fragment() {

    private lateinit var db: AppDatabase

    private lateinit var categoryId: String
    private lateinit var categoryName: String
    private lateinit var skinsRecycler: RecyclerView
    private lateinit var language: String

    private lateinit var arrow: ImageButton
    private lateinit var hiddenView: LinearLayout
    private lateinit var cardView: CardView

    private lateinit var spinner: Spinner

    private var currentJob: Job? = null
    private var orderBy = ""
    private var rarityOrder = "-"
    private var nameOrder = "-"
    private var rarityNameOrder = ""

    private var notificationSent = false

    private var _binding: FragmentSkinsListBinding? = null

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        arguments?.let {
            categoryId = it.getString("category_id").toString()
            categoryName = it.getString("category_name").toString()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSkinsListBinding.inflate(inflater, container, false)

        cardView = binding.baseCardview
        arrow = binding.arrowButton
        hiddenView = binding.hiddenView

        arrow.setOnClickListener {
            if (hiddenView.visibility == View.VISIBLE) {
                hiddenView.visibility = View.GONE
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                arrow.setImageResource(R.drawable.baseline_expand_more_24)
            } else {
                TransitionManager.beginDelayedTransition(cardView, AutoTransition())
                hiddenView.visibility = View.VISIBLE
                arrow.setImageResource(R.drawable.baseline_expand_less_24)
            }
        }

        val nameSortButton = binding.nameSortButton
        val raritySortButton = binding.raritySortButton

        nameSortButton.setOnClickListener {
            nameOrder = when (nameOrder) {
                "ASC" -> "DESC"
                "DESC" -> "-"
                else -> "ASC"
            }
            updateNameButtonImage()
            updateOrderBy()
        }

        raritySortButton.setOnClickListener {
            rarityOrder = when (rarityOrder) {
                "ASC" -> "DESC"
                "DESC" -> "-"
                else -> "ASC"
            }
            updateRarityButtonImage()
            updateOrderBy()
        }

        return binding.root
    }

    private fun updateNameButtonImage() {
        val imageResource = when (nameOrder) {
            "ASC" -> R.drawable.baseline_arrow_downward_24
            "DESC" -> R.drawable.baseline_arrow_upward_24
            else -> R.drawable.baseline_remove_24
        }
        binding.nameSortButton.setImageResource(imageResource)
    }

    private fun updateRarityButtonImage() {
        val imageResource = when (rarityOrder) {
            "ASC" -> R.drawable.baseline_arrow_downward_24
            "DESC" -> R.drawable.baseline_arrow_upward_24
            else -> R.drawable.baseline_remove_24
        }
        binding.raritySortButton.setImageResource(imageResource)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateOrderBy() {
        orderBy = when {
            nameOrder == "-" && rarityOrder == "-" -> ""
            nameOrder != "-" && rarityOrder == "-" -> "ORDER BY name $nameOrder"
            nameOrder == "-" && rarityOrder != "-" -> "ORDER BY rarity_name $rarityOrder"
            else -> "ORDER BY rarity_name $rarityOrder, name $nameOrder"
        }
        updateViewFromDB()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()

        (activity as AppCompatActivity).supportActionBar?.title = categoryName

        db = (activity as MainActivity).getDatabase()
        CoroutineScope(Dispatchers.IO).launch {
            language = db.settingsDao().getLanguage() ?: "fr"

            binding.textFilters.text =
                getString(if (language == "fr") R.string.filters_french else R.string.filters_english)
            binding.textName.text =
                getString(if (language == "fr") R.string.name_french else R.string.name_english)
            binding.textRarity1.text =
                getString(if (language == "fr") R.string.rarity_french else R.string.rarity_english)
            binding.textRarity2.text =
                getString(if (language == "fr") R.string.rarity_french else R.string.rarity_english)

            // // // // // Mise à jour du spinner avec ces valeurs
            spinner = binding.raritySpinner

            val listItems = db.skinDao().getAllRarities()
            val noneRarity = Rarity(if (language == "fr") "Aucune" else "None", "#238636")
            val updatedList = mutableListOf(noneRarity) + listItems

            val adapter = RaritySpinnerListAdapter(requireContext(), updatedList)

            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = parent?.getItemAtPosition(position) as? Rarity
                    rarityNameOrder =
                        if (selectedItem?.name == "None" || selectedItem?.name == "Aucune") "" else "AND rarity_name = '${selectedItem?.name}'"
                    updateViewFromDB()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            // // // // //
        }

        skinsRecycler = binding.skinsRecycler.apply {
            adapter = getRecyclerViewAdapter(Skins(emptyList()))
            layoutManager = LinearLayoutManager(requireContext())
        }

        updateViewFromDB()
    }

    private fun getRecyclerViewAdapter(skins: Skins): SkinAdapter {
        return SkinAdapter(skins, object : SkinAdapter.OnSkinClickListener {
            override fun onSkinClick(skinId: String) {
                val bundle = Bundle().apply {
                    putString("skin_id", skinId)
                    putString("category_name", categoryName)
                }
                findNavController().navigate(
                    R.id.action_skinsListFragment_to_skinViewFragment,
                    bundle
                )
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun updateViewFromDB() {
        currentJob?.cancel()
        currentJob = CoroutineScope(Dispatchers.IO).launch {
            // Query custom en fonction des filtres
            val queryStr = SimpleSQLiteQuery(
                "SELECT * FROM skins WHERE category_id = '$categoryId' $rarityNameOrder $orderBy"
            )

            val flow = db.skinDao().getFlowData(queryStr)
            flow.collect {
                CoroutineScope(Dispatchers.Main).launch {
                    skinsRecycler.adapter = getRecyclerViewAdapter(Skins(it))
                    if (!notificationSent) {
                        notificationSent = true
                        notifyNewData()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun notifyNewData() {
        lifecycleScope.launch {
            // // // // // Création de l'intent permettant d'afficher la page viewskin
            val skinId = withContext(Dispatchers.IO) {
                db.skinDao().getRandomSkinIdByCategory(categoryId)
            }

            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("skin_id", skinId)
                putExtra("category_name", categoryName)
            }

            val pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            // // // // //

            val title =
                getString(if (language == "fr") R.string.notification_title_french else R.string.notification_title_english)
            val content =
                String.format(
                    getString(if (language == "fr") R.string.notification_content_french else R.string.notification_content_english),
                    categoryName
                )

            val builder = NotificationCompat.Builder(
                requireContext(),
                CHANNEL_ID
            )
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            with(NotificationManagerCompat.from(requireContext())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
                        return@with
                    }
                }
                notify(1, builder.build())
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "fr_nextu_licha_ilan_skin_update"
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Skin update"
            val descriptionText = "A update notification when skins come"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                (activity as AppCompatActivity).getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}