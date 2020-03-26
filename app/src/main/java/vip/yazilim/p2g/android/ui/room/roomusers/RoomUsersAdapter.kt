package vip.yazilim.p2g.android.ui.room.roomusers

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter
import com.daimajia.swipe.implments.SwipeItemRecyclerMangerImpl
import kotlinx.android.synthetic.main.item_room_user_model.view.*
import kotlinx.android.synthetic.main.layout_row_user_events.view.*
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.constant.ColorCodes.CYAN
import vip.yazilim.p2g.android.constant.ColorCodes.GREEN
import vip.yazilim.p2g.android.constant.ColorCodes.RED
import vip.yazilim.p2g.android.constant.ColorCodes.WHITE
import vip.yazilim.p2g.android.constant.enums.Role
import vip.yazilim.p2g.android.model.p2g.RoomUserModel
import vip.yazilim.p2g.android.util.data.SharedPrefSingleton
import vip.yazilim.p2g.android.util.glide.GlideApp


/**
 * @author mustafaarifsisman - 07.03.2020
 * @contact mustafaarifsisman@gmail.com
 */
class RoomUsersAdapter(
    private var roomUserModelList: MutableList<RoomUserModel>,
    private val itemClickListener: OnItemClickListener,
    private val swipeListener: SwipeLayout.SwipeListener
) : RecyclerSwipeAdapter<RoomUsersAdapter.MViewHolder>() {

    private lateinit var view: View
    private var itemManager = SwipeItemRecyclerMangerImpl(this)
    private var userIdMe: String? = "-"

    inner class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val swipeLayout: SwipeLayout = itemView.findViewById(R.id.row_user_model)
        fun bindView(roomUserModel: RoomUserModel) {
            itemView.row_user_model.close(false)

            val user = roomUserModel.user
            val roomUser = roomUserModel.roomUser

            if (user != null && roomUser != null) {
                itemView.user_name.text = user.name
                itemView.user_role.text = roomUser.role

                when {
                    roomUser.role.equals(Role.ROOM_OWNER.role) -> {
                        itemView.user_role.setTextColor(Color.parseColor(RED))
                    }
                    roomUser.role.equals(Role.ROOM_ADMIN.role) -> {
                        itemView.user_role.setTextColor(Color.parseColor(CYAN))
                    }
                    roomUser.role.equals(Role.ROOM_DJ.role) -> {
                        itemView.user_role.setTextColor(Color.parseColor(GREEN))
                    }
                    roomUser.role.equals(Role.ROOM_USER.role) -> {
                        itemView.user_role.setTextColor(Color.parseColor(WHITE))
                    }
                }

                if (user.imageUrl != null) {
                    GlideApp.with(view)
                        .load(user.imageUrl)
                        .apply(RequestOptions.circleCropTransform())
                        .into(itemView.user_image)
                } else {
                    itemView.user_image.setImageResource(R.drawable.ic_profile_image)
                }
            }

            itemView.row_user_model.showMode = SwipeLayout.ShowMode.LayDown
            itemView.row_user_model.isClickToClose = true
            itemView.row_user_model.addDrag(SwipeLayout.DragEdge.Right, itemView.user_event_holder)
        }

        fun bindEvent(roomUserModel: RoomUserModel, clickListener: OnItemClickListener) {
            itemView.swipeChangeRoleButton.setOnClickListener {
                clickListener.onChangeRoleClicked(
                    itemView.row_user_model,
                    roomUserModel
                )
            }
            itemView.swipeAddButton.setOnClickListener {
                clickListener.onAddClicked(
                    itemView.row_user_model,
                    roomUserModel
                )
            }
            swipeLayout.addSwipeListener(swipeListener)
        }

        fun bindItemManager(position: Int) {
            itemManager.bindView(itemView, position)
        }
    }

    interface OnItemClickListener {
        fun onChangeRoleClicked(view: SwipeLayout, roomUserModel: RoomUserModel)
        fun onAddClicked(view: SwipeLayout, roomUserModel: RoomUserModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_user_model, parent, false)
        return MViewHolder(view)
    }

    override fun getSwipeLayoutResourceId(position: Int): Int {
        return R.id.row_user_model
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        val roomUserModel = roomUserModelList[position]
        holder.bindView(roomUserModel)

        if (roomUserModel.user?.id == userIdMe) {
            holder.swipeLayout.isSwipeEnabled = false
        } else {
            holder.bindEvent(roomUserModelList[position], itemClickListener)
            holder.bindItemManager(position)
        }
    }

    override fun getItemCount(): Int {
        return roomUserModelList.size
    }

    fun update(data: MutableList<RoomUserModel>) {
        userIdMe = SharedPrefSingleton.read("userId", "-")
        roomUserModelList = data
        notifyDataSetChanged()
    }

}