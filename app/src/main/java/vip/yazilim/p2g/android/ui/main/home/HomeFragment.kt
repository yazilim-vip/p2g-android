package vip.yazilim.p2g.android.ui.main.home

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.dialog_create_room.view.*
import kotlinx.android.synthetic.main.dialog_create_room.view.dialog_cancel_button
import kotlinx.android.synthetic.main.dialog_create_room.view.dialog_room_password
import kotlinx.android.synthetic.main.dialog_room_password.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.activity.RoomActivity
import vip.yazilim.p2g.android.api.generic.Callback
import vip.yazilim.p2g.android.api.generic.request
import vip.yazilim.p2g.android.constant.GeneralConstants.UNDEFINED
import vip.yazilim.p2g.android.entity.Room
import vip.yazilim.p2g.android.entity.RoomUser
import vip.yazilim.p2g.android.model.p2g.RoomModel
import vip.yazilim.p2g.android.ui.FragmentBase
import vip.yazilim.p2g.android.ui.main.MainViewModel
import vip.yazilim.p2g.android.util.helper.TAG
import vip.yazilim.p2g.android.util.helper.UIHelper
import vip.yazilim.p2g.android.util.helper.UIHelper.Companion.closeKeyboard
import vip.yazilim.p2g.android.util.refrofit.Singleton

/**
 * @author mustafaarifsisman - 04.02.2020
 * @contact mustafaarifsisman@gmail.com
 */
class HomeFragment : FragmentBase(R.layout.fragment_home),
    HomeAdapter.OnItemClickListener {
    private lateinit var adapter: HomeAdapter
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRooms()
    }

    override fun setupViewModel() {
        super.setupDefaultObservers(viewModel)
        viewModel.roomModels.observe(this, renderRoomModels)
    }

    override fun setupUI() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = HomeAdapter(viewModel.roomModels.value ?: mutableListOf(), this)
        recyclerView.adapter = adapter

        fab.setOnClickListener { createRoomButtonEvent() }
        swipeRefreshContainer.setOnRefreshListener { refreshRoomsEvent() }
    }

    // Observer
    private val renderRoomModels = Observer<MutableList<RoomModel>> {
        Log.v(TAG, "data updated $it")
        layoutError.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        adapter.roomModelsFull = it
        adapter.update(it)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem: MenuItem? = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem?.actionView as SearchView

        searchView.queryHint = resources.getString(R.string.hint_search_room)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })
    }

    override fun onItemClicked(roomModel: RoomModel) {
        val room: Room? = roomModel.room

        if (room?.password == null) {
            joinRoomEvent(roomModel)
        } else {
            joinPrivateRoomEvent(roomModel)
        }

    }

    private fun joinRoomEvent(roomModel: RoomModel) = request(
        roomModel.room?.id?.let { Singleton.apiClient().joinRoom(it, UNDEFINED) },
        object : Callback<RoomUser> {
            override fun onError(msg: String) {
                UIHelper.showSnackBarShortTop(root, msg)
            }

            override fun onSuccess(obj: RoomUser) {
                Log.d(TAG, "Joined room with roomUser ID: " + obj.id)

                val intent = Intent(activity, RoomActivity::class.java)
                intent.putExtra("roomModel", roomModel)
                intent.putExtra("roomUser", obj)
                startActivity(intent)
            }
        })


    private fun joinPrivateRoomEvent(roomModel: RoomModel) {
        val room = roomModel.room

        val mDialogView = View.inflate(context, R.layout.dialog_room_password, null)
        val mBuilder = AlertDialog.Builder(activity).setView(mDialogView)
        val joinButton = mDialogView.dialog_join_room_button
        val roomPasswordEditText = mDialogView.dialog_room_password
        val mAlertDialog: AlertDialog
        mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        roomPasswordEditText.requestFocus()

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
            request(
                room?.id?.let { id ->
                    Singleton.apiClient().joinRoom(id, roomPasswordEditText.text.toString())
                },
                object : Callback<RoomUser> {
                    override fun onError(msg: String) {
                        UIHelper.showSnackBarShortTop(mDialogView, msg)
                    }

                    override fun onSuccess(obj: RoomUser) {
                        Log.d(TAG, "Joined room with roomUser ID: " + obj.id)
                        mAlertDialog.dismiss()
                        context?.closeKeyboard()

                        val intent = Intent(activity, RoomActivity::class.java)
                        intent.putExtra("roomModel", roomModel)
                        intent.putExtra("roomUser", obj)
                        startActivity(intent)
                    }
                })
        }


        // Click cancel
        mDialogView.dialog_cancel_button.setOnClickListener {
            mAlertDialog.cancel()
            roomPasswordEditText.clearFocus()
            context?.closeKeyboard()
        }
    }

    private fun createRoomButtonEvent() {
        val mDialogView = View.inflate(context, R.layout.dialog_create_room, null)
        val mBuilder = AlertDialog.Builder(activity).setView(mDialogView)
        val mAlertDialog = mBuilder.show()
        mAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val roomNameEditText = mDialogView.dialog_room_name
        val roomPasswordEditText = mDialogView.dialog_room_password
        val createButton = mDialogView.dialog_create_room_button

        // For request focus and open keyboard
        roomNameEditText.requestFocus()

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
            request(
                Singleton.apiClient().createRoom(
                    roomNameEditText.text.toString(),
                    roomPasswordEditText.text.toString()
                ),
                object : Callback<Room> {
                    override fun onError(msg: String) {
                        Log.d(TAG, "Room can not created")
                        UIHelper.showSnackBarShortTop(mDialogView, msg)
                    }

                    override fun onSuccess(obj: Room) {
                        Log.d(TAG, "Room created with ID: " + obj.id)
                        context?.closeKeyboard()
                        mAlertDialog.dismiss()

                        val roomIntent = Intent(activity, RoomActivity::class.java)
                        roomIntent.putExtra("room", obj)
                        startActivity(roomIntent)
                    }
                })
        }


        // Click cancel
        mDialogView.dialog_cancel_button.setOnClickListener {
            mAlertDialog.cancel()
            roomNameEditText.clearFocus()
            roomPasswordEditText.clearFocus()
            context?.closeKeyboard()
        }
    }

    private fun refreshRoomsEvent() = request(
        Singleton.apiClient().getRoomModels(),
        object : Callback<MutableList<RoomModel>> {
            override fun onError(msg: String) {
                UIHelper.showSnackBarShortTop(root, resources.getString(R.string.err_room_refresh))
                swipeRefreshContainer.isRefreshing = false
            }

            override fun onSuccess(obj: MutableList<RoomModel>) {
                adapter.update(obj)
                adapter.roomModelsFull = obj
                swipeRefreshContainer.isRefreshing = false
            }
        })

}