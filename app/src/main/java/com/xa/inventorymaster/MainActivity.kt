package com.xa.inventorymaster

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.xa.inventorymaster.databinding.ActivityMainBinding
import com.xa.inventorymaster.databinding.DialogAddItemBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private val storageLocations = listOf(
        "Kitchen A", "Kitchen B", "Kitchen C", "Kitchen D", "Kitchen E", "Kitchen F",
        "Coffee Shelf", "Guest Loft", "Main Loft"
    )
    private lateinit var binding: ActivityMainBinding
    private var dialog: AlertDialog? = null
    private var isDialogOpen = false
    private var selectedPhotoPath: String? = null

    private val viewModel: ItemViewModel by viewModels { ItemViewModelFactory(application, this) }

    private val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = File(cacheDir, "item_photo_${System.currentTimeMillis()}.jpg")
                contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedPhotoPath = file.absolutePath
                dialog?.findViewById<android.widget.ImageView>(R.id.photoPreview)?.setImageURI(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called with savedInstanceState: $savedInstanceState")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up ViewPager2 and TabLayout
        binding.viewPager.adapter = StorageLocationAdapter(this, storageLocations)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = storageLocations[position]
            tab.setIcon(when (position) {
                0 -> R.drawable.ic_kitchen
                1 -> R.drawable.ic_kitchen
                2 -> R.drawable.ic_kitchen
                3 -> R.drawable.ic_kitchen
                4 -> R.drawable.ic_kitchen
                5 -> R.drawable.ic_kitchen
                6 -> R.drawable.ic_coffee
                7 -> R.drawable.ic_loft
                8 -> R.drawable.ic_loft
                else -> R.drawable.ic_kitchen
            })
        }.attach()

        // Add fade animation for ViewPager2
        binding.viewPager.setPageTransformer { page, position ->
            page.alpha = 1 - kotlin.math.abs(position)
        }

        // Set up FAB to show dialog for adding new items
        binding.fab.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(this, R.anim.fab_scale)
            binding.fab.startAnimation(animation)
            isDialogOpen = true
            showAddItemDialog()
        }

        // Restore dialog state if it was open before rotation
        if (savedInstanceState != null) {
            isDialogOpen = savedInstanceState.getBoolean("isDialogOpen", false)
            if (isDialogOpen) {
                showAddItemDialog()
            }
        }
    }

    private fun showAddItemDialog() {
        dialog?.dismiss()

        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Set up the storage location spinner
        val storageLocations = StorageLocation.values().map { it.name.replace("_", " ") }
        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            storageLocations
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.storageLocationSpinner.adapter = adapter

        // Restore saved form data
        dialogBinding.nameEditText.setText(viewModel.getFormName())
        dialogBinding.priceEditText.setText(viewModel.getFormPrice())
        dialogBinding.quantityBoughtEditText.setText(viewModel.getFormQuantityBought())
        dialogBinding.quantityRemainingEditText.setText(viewModel.getFormQuantityRemaining())
        val storagePosition = storageLocations.indexOf(viewModel.getFormStorageLocation()?.replace("_", " "))
        dialogBinding.storageLocationSpinner.setSelection(if (storagePosition != -1) storagePosition else 0)
        dialogBinding.safetyStockEditText.setText(viewModel.getFormSafetyStock())
        val unitPosition = resources.getStringArray(R.array.units).indexOf(viewModel.getFormUnit())
        dialogBinding.unitSpinner.setSelection(if (unitPosition != -1) unitPosition else 0)

        // Set up photo picker
        dialogBinding.pickPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickPhotoLauncher.launch(intent)
        }

        // Add TextWatchers and OnItemSelectedListener
        dialogBinding.nameEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateFormField(ItemViewModel.KEY_NAME, s.toString().trim())
            }
        })

        dialogBinding.priceEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateFormField(ItemViewModel.KEY_PRICE, s.toString())
            }
        })

        dialogBinding.quantityBoughtEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateFormField(ItemViewModel.KEY_QUANTITY_BOUGHT, s.toString())
            }
        })

        dialogBinding.quantityRemainingEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateFormField(ItemViewModel.KEY_QUANTITY_REMAINING, s.toString())
            }
        })

        dialogBinding.storageLocationSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedLocation = parent?.getItemAtPosition(position).toString().replace(" ", "_")
                viewModel.updateFormField(ItemViewModel.KEY_STORAGE_LOCATION, selectedLocation)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        dialogBinding.safetyStockEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.updateFormField(ItemViewModel.KEY_SAFETY_STOCK, s.toString())
            }
        })

        dialogBinding.unitSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedUnit = parent?.getItemAtPosition(position).toString()
                viewModel.updateFormField(ItemViewModel.KEY_UNIT, selectedUnit)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Adjust dialog window to handle soft input
        dialog?.window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog?.window?.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
        val displayMetrics = resources.displayMetrics
        val maxHeight = (displayMetrics.heightPixels * 0.7).toInt()
        dialog?.window?.setAttributes(
            dialog?.window?.attributes?.apply {
                height = maxHeight
            }
        )

        dialogBinding.nameEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialogBinding.nestedScrollView.post {
                    dialogBinding.nestedScrollView.smoothScrollTo(0, dialogBinding.nameEditText.top)
                }
            }
        }

        dialogBinding.addButton.setOnClickListener {
            Log.d("MainActivity", "Add button clicked")
            val name = dialogBinding.nameEditText.text.toString().trim()
            val priceStr = dialogBinding.priceEditText.text.toString()
            val quantityBoughtStr = dialogBinding.quantityBoughtEditText.text.toString()
            val quantityRemainingStr = dialogBinding.quantityRemainingEditText.text.toString()
            val storageLocationStr = dialogBinding.storageLocationSpinner.selectedItem.toString().replace(" ", "_")
            val safetyStockStr = dialogBinding.safetyStockEditText.text.toString()
            val unit = dialogBinding.unitSpinner.selectedItem.toString()

            if (name.isEmpty()) {
                dialogBinding.nameEditText.error = "Required"
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0
            val quantityBought = quantityBoughtStr.toIntOrNull() ?: 0
            val quantityRemaining = quantityRemainingStr.toIntOrNull() ?: 0
            val safetyStock = safetyStockStr.toIntOrNull() ?: 0
            val storageLocation = StorageLocation.valueOf(storageLocationStr)

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    Log.d("MainActivity", "Checking for existing item with name: $name")
                    val existingItem = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        viewModel.getAllItems().find { it.name.equals(name, ignoreCase = true) }
                    }
                    if (existingItem != null) {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Duplicate Item")
                            .setMessage("An item named '$name' already exists. Do you want to edit it?")
                            .setPositiveButton("Edit") { _, _ ->
                                dialogBinding.nameEditText.setText(existingItem.name)
                                dialogBinding.priceEditText.setText(existingItem.price.toString())
                                dialogBinding.quantityBoughtEditText.setText(existingItem.quantityBought.toString())
                                dialogBinding.quantityRemainingEditText.setText(existingItem.quantityRemaining.toString())
                                dialogBinding.storageLocationSpinner.setSelection(
                                    storageLocations.indexOf(existingItem.storageLocation.name.replace("_", " "))
                                )
                                dialogBinding.safetyStockEditText.setText(existingItem.safetyStock.toString())
                                dialogBinding.unitSpinner.setSelection(
                                    resources.getStringArray(R.array.units).indexOf(existingItem.weight)
                                )
                                selectedPhotoPath = existingItem.photoPath
                                if (selectedPhotoPath != null) {
                                    dialogBinding.photoPreview.setImageBitmap(android.graphics.BitmapFactory.decodeFile(selectedPhotoPath))
                                }
                            }
                            .setNegativeButton("Cancel") { _, _ ->
                                dialog?.dismiss()
                                isDialogOpen = false
                            }
                            .show()
                    } else {
                        val newItem = Item(
                            name = name,
                            price = price,
                            quantityBought = quantityBought,
                            quantityRemaining = quantityRemaining,
                            storageLocation = storageLocation,
                            photoPath = selectedPhotoPath,
                            weight = unit,
                            safetyStock = safetyStock
                        )
                        withContext(kotlinx.coroutines.Dispatchers.IO) {
                            viewModel.insert(newItem)
                        }
                        viewModel.clearFormData()
                        dialogBinding.nameEditText.setText("")
                        dialogBinding.priceEditText.setText("")
                        dialogBinding.quantityBoughtEditText.setText("")
                        dialogBinding.quantityRemainingEditText.setText("")
                        dialogBinding.storageLocationSpinner.setSelection(0)
                        dialogBinding.safetyStockEditText.setText("")
                        dialogBinding.unitSpinner.setSelection(0)
                        dialogBinding.photoPreview.setImageResource(R.drawable.ic_item_default)
                        selectedPhotoPath = null
                        dialog?.dismiss()
                        isDialogOpen = false
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error inserting new item: ${e.message}", e)
                }
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            viewModel.clearFormData()
            dialogBinding.nameEditText.setText("")
            dialogBinding.priceEditText.setText("")
            dialogBinding.quantityBoughtEditText.setText("")
            dialogBinding.quantityRemainingEditText.setText("")
            dialogBinding.storageLocationSpinner.setSelection(0)
            dialogBinding.safetyStockEditText.setText("")
            dialogBinding.unitSpinner.setSelection(0)
            dialogBinding.photoPreview.setImageResource(R.drawable.ic_item_default)
            selectedPhotoPath = null
            dialog?.dismiss()
            isDialogOpen = false
        }

        dialog?.setOnDismissListener {
            viewModel.saveFormData(
                name = dialogBinding.nameEditText.text.toString().trim(),
                price = dialogBinding.priceEditText.text.toString(),
                quantityBought = dialogBinding.quantityBoughtEditText.text.toString(),
                quantityRemaining = dialogBinding.quantityRemainingEditText.text.toString(),
                storageLocation = dialogBinding.storageLocationSpinner.selectedItem.toString().replace(" ", "_"),
                safetyStock = dialogBinding.safetyStockEditText.text.toString(),
                unit = dialogBinding.unitSpinner.selectedItem.toString()
            )
        }

        dialog?.show()
        dialog?.window?.setWindowAnimations(R.style.DialogAnimation)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isDialogOpen", isDialogOpen)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
        isDialogOpen = false
    }
}

class StorageLocationAdapter(
    fragmentActivity: FragmentActivity,
    private val storageLocations: List<String>
) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = storageLocations.size
    override fun createFragment(position: Int): Fragment {
        return StorageFragment.newInstance(storageLocations[position])
    }
}