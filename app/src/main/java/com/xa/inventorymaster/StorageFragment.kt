package com.xa.inventorymaster

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.xa.inventorymaster.databinding.DialogAddItemBinding
import com.xa.inventorymaster.databinding.FragmentStorageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageFragment : Fragment() {
    companion object {
        private const val ARG_LOCATION = "location"
        fun newInstance(location: String): StorageFragment {
            return StorageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOCATION, location)
                }
            }
        }
    }

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!
    private var isSortByNameAscending = true
    private var isSortByPriceAscending = true
    private var isSortByQuantityAscending = true
    private var isSortByStorageAscending = true
    private var isSortByUnitAscending = true
    private var isSortBySafetyStockAscending = true
    private val viewModel: ItemViewModel by activityViewModels { ItemViewModelFactory(requireActivity().application, requireActivity()) }
    private var selectedPhotoPath: String? = null

    private val pickPhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = java.io.File(requireContext().cacheDir, "item_photo_${System.currentTimeMillis()}.jpg")
                requireContext().contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                selectedPhotoPath = file.absolutePath
                binding.root.findViewById<android.widget.ImageView>(R.id.photoPreview)?.setImageURI(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val location = arguments?.getString(ARG_LOCATION) ?: "Unknown"
        val adapter = ItemAdapter { item ->
            showEditItemDialog(item)
        }
        binding.inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.inventoryRecyclerView.adapter = adapter

        // Add slide animation for RecyclerView items
        binding.inventoryRecyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
            addDuration = 300
            removeDuration = 300
        }

        // Insert sample data only if the database is empty
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            val items = withContext(kotlinx.coroutines.Dispatchers.IO) {
                viewModel.getAllItems()
            }
            if (items.isEmpty()) {
                withContext(kotlinx.coroutines.Dispatchers.IO) {
                    viewModel.insert(Item(
                        name = "Laptop",
                        price = 999.99,
                        quantityBought = 10,
                        quantityRemaining = 5,
                        storageLocation = StorageLocation.KITCHEN_A,
                        photoPath = null,
                        weight = "kg",
                        safetyStock = 2
                    ))
                    viewModel.insert(Item(
                        name = "Mouse",
                        price = 29.99,
                        quantityBought = 20,
                        quantityRemaining = 15,
                        storageLocation = StorageLocation.KITCHEN_B,
                        photoPath = null,
                        weight = "unit",
                        safetyStock = 5
                    ))
                }
            }
        }

        // Set up filter spinner
        val filterAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        )
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.storageFilterSpinner.adapter = filterAdapter

        // Set up search functionality
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.allItems.value?.let { items ->
                    var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                    // Apply search filter
                    filteredItems = if (newText.isNullOrEmpty()) {
                        filteredItems
                    } else {
                        filteredItems.filter { it.name.contains(newText, ignoreCase = true) }
                    }
                    // Apply spinner filter
                    filteredItems = applySpinnerFilter(filteredItems)
                    adapter.submitList(filteredItems)
                }
                return true
            }
        })

        // Set up filter spinner listener
        binding.storageFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.allItems.value?.let { items ->
                    var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                    // Apply search filter
                    val searchQuery = binding.searchView.query.toString()
                    if (searchQuery.isNotEmpty()) {
                        filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }
                    // Apply spinner filter
                    filteredItems = applySpinnerFilter(filteredItems)
                    adapter.submitList(filteredItems)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set up sorting by name
        binding.sortNameTextView.setOnClickListener {
            isSortByNameAscending = !isSortByNameAscending
            binding.sortNameTextView.tag = if (isSortByNameAscending) "ascending" else "descending"
            binding.sortPriceTextView.tag = null
            binding.sortQuantityTextView.tag = null
            binding.sortStorageTextView.tag = null
            binding.sortUnitTextView.tag = null
            binding.sortSafetyStockTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                // Apply search filter
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                // Apply spinner filter
                filteredItems = applySpinnerFilter(filteredItems)
                // Apply sorting
                val sortedItems = if (isSortByNameAscending) {
                    filteredItems.sortedBy { it.name.lowercase() }
                } else {
                    filteredItems.sortedByDescending { it.name.lowercase() }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Set up sorting by price
        binding.sortPriceTextView.setOnClickListener {
            isSortByPriceAscending = !isSortByPriceAscending
            binding.sortPriceTextView.tag = if (isSortByPriceAscending) "ascending" else "descending"
            binding.sortNameTextView.tag = null
            binding.sortQuantityTextView.tag = null
            binding.sortStorageTextView.tag = null
            binding.sortUnitTextView.tag = null
            binding.sortSafetyStockTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                filteredItems = applySpinnerFilter(filteredItems)
                val sortedItems = if (isSortByPriceAscending) {
                    filteredItems.sortedBy { it.price }
                } else {
                    filteredItems.sortedByDescending { it.price }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Set up sorting by quantity
        binding.sortQuantityTextView.setOnClickListener {
            isSortByQuantityAscending = !isSortByQuantityAscending
            binding.sortQuantityTextView.tag = if (isSortByQuantityAscending) "ascending" else "descending"
            binding.sortNameTextView.tag = null
            binding.sortPriceTextView.tag = null
            binding.sortStorageTextView.tag = null
            binding.sortUnitTextView.tag = null
            binding.sortSafetyStockTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                filteredItems = applySpinnerFilter(filteredItems)
                val sortedItems = if (isSortByQuantityAscending) {
                    filteredItems.sortedBy { it.quantityRemaining }
                } else {
                    filteredItems.sortedByDescending { it.quantityRemaining }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Set up sorting by storage
        binding.sortStorageTextView.setOnClickListener {
            isSortByStorageAscending = !isSortByStorageAscending
            binding.sortStorageTextView.tag = if (isSortByStorageAscending) "ascending" else "descending"
            binding.sortNameTextView.tag = null
            binding.sortPriceTextView.tag = null
            binding.sortQuantityTextView.tag = null
            binding.sortUnitTextView.tag = null
            binding.sortSafetyStockTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                filteredItems = applySpinnerFilter(filteredItems)
                val sortedItems = if (isSortByStorageAscending) {
                    filteredItems.sortedBy { it.storageLocation.name.lowercase() }
                } else {
                    filteredItems.sortedByDescending { it.storageLocation.name.lowercase() }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Set up sorting by unit
        binding.sortUnitTextView.setOnClickListener {
            isSortByUnitAscending = !isSortByUnitAscending
            binding.sortUnitTextView.tag = if (isSortByUnitAscending) "ascending" else "descending"
            binding.sortNameTextView.tag = null
            binding.sortPriceTextView.tag = null
            binding.sortQuantityTextView.tag = null
            binding.sortStorageTextView.tag = null
            binding.sortSafetyStockTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                filteredItems = applySpinnerFilter(filteredItems)
                val sortedItems = if (isSortByUnitAscending) {
                    filteredItems.sortedBy { it.weight.lowercase() }
                } else {
                    filteredItems.sortedByDescending { it.weight.lowercase() }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Set up sorting by safety stock
        binding.sortSafetyStockTextView.setOnClickListener {
            isSortBySafetyStockAscending = !isSortBySafetyStockAscending
            binding.sortSafetyStockTextView.tag = if (isSortBySafetyStockAscending) "ascending" else "descending"
            binding.sortNameTextView.tag = null
            binding.sortPriceTextView.tag = null
            binding.sortQuantityTextView.tag = null
            binding.sortStorageTextView.tag = null
            binding.sortUnitTextView.tag = null
            viewModel.allItems.value?.let { items ->
                var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
                val searchQuery = binding.searchView.query.toString()
                if (searchQuery.isNotEmpty()) {
                    filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
                }
                filteredItems = applySpinnerFilter(filteredItems)
                val sortedItems = if (isSortBySafetyStockAscending) {
                    filteredItems.sortedBy { it.safetyStock }
                } else {
                    filteredItems.sortedByDescending { it.safetyStock }
                }
                adapter.submitList(sortedItems)
            }
        }

        // Observe the list of items and update the RecyclerView
        viewModel.allItems.observe(viewLifecycleOwner) { items: List<Item> ->
            var filteredItems = items.filter { it.storageLocation.name.replace("_", " ") == location }
            // Apply search filter
            val searchQuery = binding.searchView.query.toString()
            if (searchQuery.isNotEmpty()) {
                filteredItems = filteredItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
            }
            // Apply spinner filter
            filteredItems = applySpinnerFilter(filteredItems)
            adapter.submitList(filteredItems)
            // Show toast for low stock items
            val lowStockItems = filteredItems.filter { it.quantityRemaining <= it.safetyStock }
            if (lowStockItems.isNotEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "Low stock items detected: ${lowStockItems.joinToString { it.name }}",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun applySpinnerFilter(items: List<Item>): List<Item> {
        return when (binding.storageFilterSpinner.selectedItem.toString()) {
            "All" -> items
            "Low Stock" -> items.filter { it.quantityRemaining <= it.safetyStock }
            "Unit: kg" -> items.filter { it.weight == "kg" }
            "Unit: unit" -> items.filter { it.weight == "unit" }
            else -> items
        }
    }

    private fun showEditItemDialog(item: Item) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        // Pre-fill the dialog with the item's data
        dialogBinding.nameEditText.setText(item.name)
        dialogBinding.priceEditText.setText(item.price.toString())
        dialogBinding.quantityBoughtEditText.setText(item.quantityBought.toString())
        dialogBinding.quantityRemainingEditText.setText(item.quantityRemaining.toString())
        val storageLocations = StorageLocation.values().map { it.name.replace("_", " ") }
        val storageAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            storageLocations
        )
        storageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.storageLocationSpinner.adapter = storageAdapter
        dialogBinding.storageLocationSpinner.setSelection(storageLocations.indexOf(item.storageLocation.name.replace("_", " ")))
        dialogBinding.safetyStockEditText.setText(item.safetyStock.toString())
        val unitPosition = resources.getStringArray(R.array.units).indexOf(item.weight)
        dialogBinding.unitSpinner.setSelection(if (unitPosition != -1) unitPosition else 0)
        selectedPhotoPath = item.photoPath
        if (selectedPhotoPath != null) {
            dialogBinding.photoPreview.setImageBitmap(android.graphics.BitmapFactory.decodeFile(selectedPhotoPath))
        }

        dialogBinding.addButton.text = "Update"

        dialogBinding.pickPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickPhotoLauncher.launch(intent)
        }

        dialogBinding.addButton.setOnClickListener {
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

            val updatedItem = item.copy(
                name = name,
                price = price,
                quantityBought = quantityBought,
                quantityRemaining = quantityRemaining,
                storageLocation = storageLocation,
                photoPath = selectedPhotoPath,
                weight = unit,
                safetyStock = safetyStock
            )

            kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    viewModel.update(updatedItem)
                }
                dialog.dismiss()
                selectedPhotoPath = null
            }
        }

        dialogBinding.cancelButton.setOnClickListener {
            dialog.dismiss()
            selectedPhotoPath = null
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}