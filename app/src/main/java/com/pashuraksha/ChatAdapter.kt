package com.pashuraksha

<<<<<<< HEAD
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
=======
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
<<<<<<< HEAD
 * Chat message — supports text and image messages.
 * role = "user", "bot", or "user_image"
 */
data class ChatMessage(
    val role: String,
    val text: String,
    val bitmap: Bitmap? = null  // Attached image (for user_image role)
)

/**
 * RecyclerView adapter for the chat. Renders:
 *   - User text (right side, orange bubble)
 *   - Bot text (left side, white bubble with 🌿 avatar)
 *   - User image (right side, rounded image card)
=======
 * Single chat message. role = "user" or "bot".
 */
data class ChatMessage(val role: String, val text: String)

/**
 * RecyclerView adapter for the chat. Renders user (right) vs bot (left) bubbles
 * using two distinct item view types.
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
 */
class ChatAdapter(private val messages: MutableList<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_USER = 1
        private const val VIEW_BOT = 2
<<<<<<< HEAD
        private const val VIEW_IMAGE = 3
    }

    override fun getItemViewType(position: Int): Int = when (messages[position].role) {
        "user" -> VIEW_USER
        "user_image" -> VIEW_IMAGE
        else -> VIEW_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_USER -> UserVH(inflater.inflate(R.layout.item_message_user, parent, false))
            VIEW_IMAGE -> ImageVH(inflater.inflate(R.layout.item_message_image, parent, false))
            else -> BotVH(inflater.inflate(R.layout.item_message_bot, parent, false))
=======
    }

    override fun getItemViewType(position: Int): Int =
        if (messages[position].role == "user") VIEW_USER else VIEW_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_USER) {
            UserVH(inflater.inflate(R.layout.item_message_user, parent, false))
        } else {
            BotVH(inflater.inflate(R.layout.item_message_bot, parent, false))
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        when (holder) {
            is UserVH -> holder.text.text = msg.text
            is BotVH -> holder.text.text = msg.text
<<<<<<< HEAD
            is ImageVH -> {
                msg.bitmap?.let { holder.image.setImageBitmap(it) }
                if (msg.text.isNotBlank()) {
                    holder.caption.text = msg.text
                    holder.caption.visibility = View.VISIBLE
                } else {
                    holder.caption.visibility = View.GONE
                }
            }
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }

    fun updateLastBot(text: String) {
        if (messages.isEmpty()) return
        val lastIdx = messages.size - 1
        if (messages[lastIdx].role == "bot") {
            messages[lastIdx] = ChatMessage("bot", text)
            notifyItemChanged(lastIdx)
        }
    }

    class UserVH(v: View) : RecyclerView.ViewHolder(v) {
        val text: TextView = v.findViewById(R.id.messageText)
    }

    class BotVH(v: View) : RecyclerView.ViewHolder(v) {
        val text: TextView = v.findViewById(R.id.messageText)
    }
<<<<<<< HEAD

    class ImageVH(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView = v.findViewById(R.id.messageImage)
        val caption: TextView = v.findViewById(R.id.messageText)
    }
=======
>>>>>>> 6f0c543afecea5a353f8c95925748291d2e2578e
}
