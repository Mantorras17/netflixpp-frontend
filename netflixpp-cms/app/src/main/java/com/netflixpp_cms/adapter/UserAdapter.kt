package com.netflixpp_cms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_cms.R
import com.netflixpp_cms.model.User

class UserAdapter(
    private val users: List<User>,
    private val onDeleteClick: (User) -> Unit,
    private val onEditClick: (User) -> Unit,
    private val onResetPasswordClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnResetPassword: Button = itemView.findViewById(R.id.btnResetPassword)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvEmail.text = user.email
            tvRole.text = user.role

            // Color code role
            when (user.role.uppercase()) {
                "ADMIN" -> {
                    tvRole.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                    tvRole.text = "👑 ${user.role}"
                }
                else -> {
                    tvRole.setTextColor(itemView.context.getColor(android.R.color.darker_gray))
                    tvRole.text = "👤 ${user.role}"
                }
            }

            btnEdit.setOnClickListener { onEditClick(user) }
            btnResetPassword.setOnClickListener { onResetPasswordClick(user) }
            btnDelete.setOnClickListener { onDeleteClick(user) }

            // Disable delete for admin users
            if (user.role.equals("admin", ignoreCase = true)) {
                btnDelete.alpha = 0.5f
                btnDelete.isEnabled = false
            } else {
                btnDelete.alpha = 1.0f
                btnDelete.isEnabled = true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size
}