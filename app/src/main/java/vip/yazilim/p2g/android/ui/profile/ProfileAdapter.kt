package vip.yazilim.p2g.android.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.haipq.android.flagkit.FlagImageView
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.constant.enums.OnlineStatus
import vip.yazilim.p2g.android.model.p2g.FriendModel
import vip.yazilim.p2g.android.model.p2g.UserModel
import vip.yazilim.p2g.android.util.glide.GlideApp
import vip.yazilim.p2g.android.util.helper.TimeHelper.Companion.dateTimeFormatterFull

/**
 * @author mustafaarifsisman - 31.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
class ProfileAdapter(
    private var userModel: UserModel,
    private var friends: MutableList<FriendModel>
) :
    RecyclerView.Adapter<ProfileAdapter.MViewHolder>() {

    private lateinit var view: View

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.profile_card_view)
        private val memberSince: TextView = itemView.findViewById(R.id.member_since_text_view)
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_photo_image_view)
        private val flagImage: FlagImageView = itemView.findViewById(R.id.country_flag_image_view)
        private val email: TextView = itemView.findViewById(R.id.email_text_view)
        private val onlineStatus: ImageView =
            itemView.findViewById(R.id.online_status_online_image_view)
        private val userName: TextView = itemView.findViewById(R.id.user_name_text_view)
        private val friendCountsTextView: TextView =
            itemView.findViewById(R.id.friend_counts_text_view)
        private val songAndRoomStatus: TextView =
            itemView.findViewById(R.id.song_room_status_text_view)
        private val anthem: TextView = itemView.findViewById(R.id.anthem_text_view)
        private val spotifyId: TextView = itemView.findViewById(R.id.spotify_id_text_view)

        fun bindView(userModel: UserModel) {
            val user = userModel.user

            if (user != null) {
                val profileNamePlaceholder = user.name
                val memberSincePlaceholder =
                    "${view.resources.getString(R.string.placeholder_member_since)} ${user.creationDate?.format(
                        dateTimeFormatterFull
                    )}"
                val profileEmailPlaceholder =
                    "${view.resources.getString(R.string.placeholder_email)} ${user.email}"
                val profileSongAndRoomStatusPlaceholder =
                    "${view.resources.getString(R.string.placeholder_song_and_room_status_helper)} ${userModel.room?.name}"
                val profileAnthemPlaceholder =
                    "${view.resources.getString(R.string.placeholder_anthem)} ${user.anthem}"
                val profileSpotifyAccountIdPlaceholder =
                    "${view.resources.getString(R.string.placeholder_spotify_account_id)} ${user.id}"

                if (user.imageUrl != null) {
                    GlideApp.with(view)
                        .load(user.imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImage)
                } else {
                    profileImage.setImageResource(R.drawable.ic_profile_image)
                }

                try {
                    flagImage.countryCode = user.countryCode
                } catch (exception: Exception) {
                    flagImage.visibility = View.GONE
                }

                userName.text = profileNamePlaceholder
                memberSince.text = memberSincePlaceholder
                email.text = profileEmailPlaceholder

                if (user.anthem == null) {
                    anthem.visibility = View.GONE
                } else {
                    anthem.visibility = View.VISIBLE
                    anthem.text = profileAnthemPlaceholder
                }

                spotifyId.text = profileSpotifyAccountIdPlaceholder

                if (userModel.room != null) {
                    songAndRoomStatus.text = profileSongAndRoomStatusPlaceholder
                } else {
                    val songAndRoomStatusString =
                        " - " + view.resources.getString(R.string.placeholder_room_user_not_found)
                    songAndRoomStatus.text = songAndRoomStatusString
                }

                when (user.onlineStatus) {
                    OnlineStatus.ONLINE.onlineStatus -> {
                        onlineStatus.setImageResource(android.R.drawable.presence_online)
                        onlineStatus.visibility = View.VISIBLE
                    }
                    OnlineStatus.OFFLINE.onlineStatus -> {
                        onlineStatus.setImageResource(android.R.drawable.presence_offline)
                        onlineStatus.visibility = View.VISIBLE
                    }
                    OnlineStatus.AWAY.onlineStatus -> {
                        onlineStatus.setImageResource(android.R.drawable.presence_away)
                        onlineStatus.visibility = View.VISIBLE
                    }
                }

                cardView.visibility = View.VISIBLE
            }

            if (friends.isNotEmpty()) {
                val profileFriendCountsPlaceholder =
                    "${friends.size} ${view.resources.getString(R.string.placeholder_friend_counts)}"
                friendCountsTextView.text = profileFriendCountsPlaceholder
            } else {
                val profileFriendCountsPlaceholder =
                    "0 " + view.resources.getString(R.string.placeholder_friend_counts)
                friendCountsTextView.text = profileFriendCountsPlaceholder
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        view = LayoutInflater.from(parent.context).inflate(R.layout.row_profile, parent, false)
        return MViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 1
    }

    fun update(data: UserModel) {
        userModel = data
        notifyDataSetChanged()
    }

    fun update(data: MutableList<FriendModel>) {
        friends = data
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        holder.bindView(userModel)
    }

}