package vip.yazilim.p2g.android.ui.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.dialog_create_room.view.*
import kotlinx.android.synthetic.main.dialog_create_room.view.dialog_cancel_button
import kotlinx.android.synthetic.main.dialog_create_room.view.dialog_room_password
import kotlinx.android.synthetic.main.dialog_room_password.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.activity.RoomActivity
import vip.yazilim.p2g.android.api.client.ApiClient
import vip.yazilim.p2g.android.api.generic.Callback
import vip.yazilim.p2g.android.api.generic.P2GRequest
import vip.yazilim.p2g.android.constant.GeneralConstants.LOG_TAG
import vip.yazilim.p2g.android.constant.GeneralConstants.UNDEFINED
import vip.yazilim.p2g.android.model.p2g.Room
import vip.yazilim.p2g.android.model.p2g.RoomModel
import vip.yazilim.p2g.android.model.p2g.RoomUser
import vip.yazilim.p2g.android.ui.FragmentBase
import vip.yazilim.p2g.android.util.helper.UIHelper

/**
 * @author mustafaarifsisman - 04.02.2020
 * @contact mustafaarifsisman@gmail.com
 */
class HomeFragment : FragmentBase(HomeViewModel(), R.layout.fragment_home),
    HomeAdapter.OnItemClickListener {
    private lateinit var adapter: HomeAdapter
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRooms()
    }

    override fun setupViewModel() {
        viewModel = super.setupViewModelBase() as HomeViewModel
        viewModel.roomModels.observe(this, renderRoomModels)
    }

    override fun setupUI() {
        val recyclerView = root.findViewById<View>(R.id.recyclerView) as RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = HomeAdapter(viewModel.roomModels.value ?: mutableListOf(), this)
        recyclerView.adapter = adapter

        val createRoomButton: Button = root.findViewById(R.id.button_create_room)
        createRoomButton.setOnClickListener { createRoomButtonEvent() }

        val swipeContainer = root.findViewById<View>(R.id.swipeContainer) as SwipeRefreshLayout
        swipeContainer.setOnRefreshListener { refreshRoomsEvent() }
    }

    // Observer
    private val renderRoomModels = Observer<MutableList<RoomModel>> {
        Log.v(LOG_TAG, "data updated $it")
        layoutError.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        adapter.roomModelsFull = it
        adapter.update(it)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem: MenuItem? = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem?.actionView as SearchView

        searchView.queryHint = "Search Room or Room Owner"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("queryText", query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                Log.d("queryText", newText)
                return true
            }
        })

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                searchView.requestFocus()
                searchView.isIconified = false
                searchView.isIconifiedByDefault = false
                searchView.visibility = View.VISIBLE
                setMenuItemsVisibility(menu, searchItem, false)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                searchView.clearFocus()
                searchView.setQuery("", false)
                adapter.filter.filter("")
                searchView.isIconified = true
                searchView.isIconifiedByDefault = true
                searchView.visibility = View.VISIBLE
                setMenuItemsVisibility(menu, searchItem, true)
                return true
            }
        })
    }

    override fun onItemClicked(roomModel: RoomModel) {
        val room: Room? = roomModel.room

        if (room?.password?.isNotEmpty()!!) {
            joinPrivateRoomEvent(roomModel)
        } else {
            joinRoomEvent(roomModel)
        }

    }

    private fun joinRoomEvent(roomModel: RoomModel) {
        val room = roomModel.room
        P2GRequest.build(
            room?.id?.let { ApiClient.build().joinRoom(it, UNDEFINED) },
            object : Callback<RoomUser> {
                override fun onError(msg: String) {
                    Log.d(LOG_TAG, msg)
                    UIHelper.showToastLong(context, msg)
//                        UIHelper.showSnackBarLong(root, msg)
                }

                override fun onSuccess(obj: RoomUser) {
                    Log.d(LOG_TAG, "Joined room with roomUser ID: " + obj.id)

                    val intent = Intent(activity, RoomActivity::class.java)
                    intent.putExtra("roomModel", roomModel)
                    intent.putExtra("roomUser", obj)
                    startActivity(intent)
                }
            })
    }

    private fun joinPrivateRoomEvent(roomModel: RoomModel) {
        val room = roomModel.room

        val mDialogView = View.inflate(context, R.layout.dialog_room_password, null)
        val mBuilder = AlertDialog.Builder(activity).setView(mDialogView)
        val joinButton = mDialogView.dialog_join_room_button
        val roomPasswordEditText = mDialogView.dialog_room_password
        val mAlertDialog: AlertDialog
        mAlertDialog = mBuilder.show()

        roomPasswordEditText.requestFocus()
        showKeyboard()

        // For disable create button if password is empty
        roomPasswordEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                joinButton.isEnabled = s.isNotEmpty()
            }
        })

        // Click join
        joinButton.setOnClickListener {
            val roomPassword = roomPasswordEditText.text.toString()

            P2GRequest.build(
                room?.id?.let { it1 -> ApiClient.build().joinRoom(it1, roomPassword) },
                object : Callback<RoomUser> {
                    override fun onError(msg: String) {
                        Log.d(LOG_TAG, msg)
                        UIHelper.showToastLong(context, msg)
//                            UIHelper.showSnackBarLong(root, msg)
                    }

                    override fun onSuccess(obj: RoomUser) {
                        Log.d(LOG_TAG, "Joined room with roomUser ID: " + obj.id)
                        mAlertDialog.dismiss()
                        closeKeyboard()

                        val intent = Intent(activity, RoomActivity::class.java)
                        startActivity(intent)
                    }
                })
        }

        // Click cancel
        mDialogView.dialog_cancel_button.setOnClickListener {
            mAlertDialog.cancel()
            roomPasswordEditText.clearFocus()
            closeKeyboard()
        }
    }

    private fun createRoomButtonEvent() {
        val mDialogView = View.inflate(context, R.layout.dialog_create_room, null)
        val mBuilder = AlertDialog.Builder(activity).setView(mDialogView)
        val mAlertDialog = mBuilder.show()

        val roomNameEditText = mDialogView.dialog_room_name
        val roomPasswordEditText = mDialogView.dialog_room_password
        val createButton = mDialogView.dialog_create_room_button

        // For request focus and open keyboard
        roomNameEditText.requestFocus()
        showKeyboard()

        // For disable create button if name is empty
        roomNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                createButton.isEnabled = s.isNotEmpty()
            }
        })

        // Click create
        createButton.setOnClickListener {
            val roomName = roomNameEditText.text.toString()
            val roomPassword = roomPasswordEditText.text.toString()

            P2GRequest.build(
                ApiClient.build().createRoom(roomName, roomPassword),
                object : Callback<Room> {
                    override fun onError(msg: String) {
                        Log.d(LOG_TAG, "Room can not created")
                        UIHelper.showToastLong(context, msg)
//                        UIHelper.showSnackBarLong(root, msg)
                    }

                    override fun onSuccess(obj: Room) {
                        Log.d(LOG_TAG, "Room created with ID: " + obj.id)
                        mAlertDialog.dismiss()

                        val intent = Intent(activity, RoomActivity::class.java)
                        startActivity(intent)
                    }
                })
        }

        // Click cancel
        mDialogView.dialog_cancel_button.setOnClickListener {
            mAlertDialog.cancel()
            roomNameEditText.clearFocus()
            roomPasswordEditText.clearFocus()
            closeKeyboard()
        }
    }

    private fun refreshRoomsEvent() {
        P2GRequest.build(
            ApiClient.build().getRoomModels(),
            object : Callback<MutableList<RoomModel>> {
                override fun onError(msg: String) {
                    Log.d(LOG_TAG, msg)
                    UIHelper.showSnackBarLong(root, "Rooms cannot refreshed")
                    swipeContainer.isRefreshing = false
                }

                override fun onSuccess(obj: MutableList<RoomModel>) {
                    adapter.update(obj)
                    adapter.roomModelsFull = obj
                    swipeContainer.isRefreshing = false
                }
            })
    }

}