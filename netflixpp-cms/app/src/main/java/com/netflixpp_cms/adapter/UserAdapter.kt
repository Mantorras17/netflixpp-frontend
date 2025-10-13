package com.netflixpp_cms.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netflixpp_cms.R
import com.netflixpp_cms.model.User

class UserAdapter(
    private val users: List<User>,
    private val onDeleteClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        private val tvRole: TextView = itemView.findViewById(R.id.tvRole)
        private val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)

        fun bind(user: User) {
            tvUsername.text = user.username
            tvEmail.text = user.email
            tvRole.text = user.role

            // Mostrar papel do usuário com cor diferente para admin
            when (user.role.uppercase()) {
                "ADMIN" -> {
                    tvRole.setTextColor(itemView.context.getColor(R.color.red))
                    tvRole.text = "👑 ${user.role}"
                }
                "USER" -> {
                    tvRole.setTextColor(itemView.context.getColor(R.color.gray_light))
                    tvRole.text = "👤 ${user.role}"
                }
                else -> {
                    tvRole.setTextColor(itemView.context.getColor(R.color.gray_light))
                }
            }

            btnDelete.setOnClickListener {
                onDeleteClick(user)
            }

            // Não permitir deletar o próprio usuário (será verificado no backend também)
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