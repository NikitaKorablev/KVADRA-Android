package com.kvadra_app.contacts_list.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kvadra_app.contacts_list.data.Contact
import com.kvadra_app.contacts_list.data.ContactItem

class ContactsAdapter(private val contactItems: List<ContactItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_list_item_1, parent, false
                )
            )
        } else {
            ContactViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    android.R.layout.simple_list_item_2, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = contactItems[position]
        if (getItemViewType(position) == TYPE_HEADER) {
            (holder as HeaderViewHolder).bind(item.header)
        } else {
            (holder as ContactViewHolder).bind(item.contact)
        }
    }

    override fun getItemCount(): Int = contactItems.size

    override fun getItemViewType(position: Int): Int {
        return if (contactItems[position].header != null) TYPE_HEADER else TYPE_CONTACT
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(header: Char?) {
            textView.text = header.toString()
        }
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewName: TextView = itemView.findViewById(android.R.id.text1)
        private val textViewPhone: TextView = itemView.findViewById(android.R.id.text2)
        fun bind(contact: Contact?) {
            textViewName.text = contact?.name
            textViewPhone.text = contact?.phoneNumber
        }
    }
}
