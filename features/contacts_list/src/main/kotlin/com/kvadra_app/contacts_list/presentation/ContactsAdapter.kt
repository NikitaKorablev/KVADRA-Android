package com.kvadra_app.contacts_list.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kvadra_app.contacts_list.data.Contact
import com.kvadra_app.contacts_list.data.ContactItem
import com.kvadra_app.contacts_list.domain.OnContactClickListener
import com.kvadra_app.contacts_list.databinding.ContactItemBinding
import com.kvadra_app.contacts_list.databinding.HeaderItemBinding

class ContactsAdapter(
    private val contactItems: List<ContactItem>,
    private val onContactClickListener: OnContactClickListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val binding = HeaderItemBinding
                .inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false)

            HeaderViewHolder(binding)
        } else {
            val binding = ContactItemBinding
                .inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

            ContactViewHolder(binding, onContactClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = contactItems[position]
        if (getItemViewType(position) == TYPE_HEADER)
            (holder as HeaderViewHolder).bind(item.header)
        else (holder as ContactViewHolder).bind(item.contact)
    }

    override fun getItemCount(): Int = contactItems.size

    override fun getItemViewType(position: Int): Int {
        return if (contactItems[position].header != null) TYPE_HEADER else TYPE_CONTACT
    }

    class HeaderViewHolder(
        private val binding: HeaderItemBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(header: Char?) {
            binding.headerTitle.text = header.toString()
        }
    }

    class ContactViewHolder(
        private val binding: ContactItemBinding,
        private val onContactClickListener: OnContactClickListener
    ): RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val contact = binding.name.tag as? Contact
                contact?.let {
                    onContactClickListener.onContactClick(it)
                }
            }
        }

        fun bind(contact: Contact?) {
            binding.name.text = contact?.name
            binding.phone.text = contact?.phoneNumber
            binding.name.tag = contact
        }
    }
}
