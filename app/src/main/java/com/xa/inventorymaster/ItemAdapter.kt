package com.xa.inventorymaster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xa.inventorymaster.databinding.ItemRowBinding

class ItemAdapter(
    private val onEditClick: (Item) -> Unit
) : ListAdapter<Item, ItemAdapter.ItemViewHolder>(ItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        if (item.photoPath != null) {
            Glide.with(holder.itemView.context)
                .load(item.photoPath)
                .placeholder(R.drawable.ic_item_default)
                .into(holder.binding.itemImage)
        } else {
            holder.binding.itemImage.setImageResource(R.drawable.ic_item_default)
        }
    }

    inner class ItemViewHolder(private val binding: ItemRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Item) {
            binding.item = item
            // Set background color based on low stock
            val backgroundColor = if (item.quantityRemaining <= item.safetyStock) {
                ContextCompat.getColor(binding.root.context, android.R.color.holo_red_light)
            } else {
                ContextCompat.getColor(binding.root.context, android.R.color.transparent)
            }
            binding.root.setBackgroundColor(backgroundColor)
            binding.editButton.setOnClickListener {
                onEditClick(item)
            }
            // Load item photo if available, otherwise use default icon
            if (item.photoPath != null && item.photoPath.isNotEmpty()) {
                // Load the photo (e.g., using Glide or a simple BitmapFactory)
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(item.photoPath)
                    binding.itemIcon.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.itemIcon.setImageResource(R.drawable.ic_item_default)
                }
            } else {
                binding.itemIcon.setImageResource(R.drawable.ic_item_default)
            }
            binding.executePendingBindings()
        }
    }
}

class ItemDiffCallback : DiffUtil.ItemCallback<Item>() {
    override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
        return oldItem == newItem
    }
}