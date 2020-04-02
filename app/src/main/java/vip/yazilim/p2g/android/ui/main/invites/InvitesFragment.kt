package vip.yazilim.p2g.android.ui.main.invites

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_home.*
import vip.yazilim.p2g.android.R
import vip.yazilim.p2g.android.activity.MainActivity
import vip.yazilim.p2g.android.activity.RoomActivity
import vip.yazilim.p2g.android.activity.UserActivity
import vip.yazilim.p2g.android.api.generic.Callback
import vip.yazilim.p2g.android.api.generic.request
import vip.yazilim.p2g.android.constant.WebSocketActions.ACTION_ROOM_INVITE
import vip.yazilim.p2g.android.entity.RoomUser
import vip.yazilim.p2g.android.model.p2g.RoomInviteModel
import vip.yazilim.p2g.android.model.p2g.UserModel
import vip.yazilim.p2g.android.ui.FragmentBase
import vip.yazilim.p2g.android.ui.main.MainViewModel
import vip.yazilim.p2g.android.util.helper.TAG
import vip.yazilim.p2g.android.util.refrofit.Singleton

/**
 * @author mustafaarifsisman - 31.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
class InvitesFragment : FragmentBase(R.layout.fragment_invites),
    InvitesAdapter.OnItemClickListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: InvitesAdapter

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val roomInviteModel = intent?.getParcelableExtra<RoomInviteModel>("roomInviteModel")
            roomInviteModel?.let {
                adapter.add(it)
                adapter.roomInviteModelsFull.add(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        viewModel = ViewModelProvider(activity as MainActivity).get(MainViewModel::class.java)

        val intentFilter = IntentFilter(ACTION_ROOM_INVITE)
        activity?.registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadRoomInviteModel()
    }

    override fun setupViewModel() {
        super.setupDefaultObservers(viewModel)
        viewModel.roomInviteModel.observe(this, renderRoomInviteModel)
    }


    override fun setupUI() {
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = InvitesAdapter(viewModel.roomInviteModel.value ?: mutableListOf(), this)
        recyclerView.adapter = adapter

        // SwipeRefreshLayout
        swipeRefreshContainer.setOnRefreshListener { refreshRoomInvitesEvent() }
    }

    // Observers
    private val renderRoomInviteModel = Observer<MutableList<RoomInviteModel>> {
        if (it.isNullOrEmpty()) {
            viewModel.onEmptyList.postValue(true)
            adapter.clear()
        } else {
            viewModel.onEmptyList.postValue(false)
            adapter.roomInviteModelsFull.addAll(it)
            adapter.update(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem: MenuItem? = menu.findItem(R.id.action_search)
        val searchView: SearchView = searchItem?.actionView as SearchView

        searchView.queryHint = resources.getString(R.string.hint_search_invites)

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

    override fun onAccept(roomInviteModel: RoomInviteModel) = request(
        roomInviteModel.roomInvite?.let { Singleton.apiClient().acceptInvite(it) },
        object : Callback<RoomUser> {
            override fun onError(msg: String) {
                viewModel.onMessageError.postValue(msg)
            }

            override fun onSuccess(obj: RoomUser) {
                Log.d(TAG, "Joined room with roomUser ID: " + obj.id)

                val intent = Intent(activity, RoomActivity::class.java)
                intent.putExtra("roomModel", roomInviteModel.roomModel)
                intent.putExtra("roomUser", obj)
                startActivity(intent)
            }
        })


    override fun onReject(roomInviteModel: RoomInviteModel) = request(
        roomInviteModel.roomInvite?.id?.let { Singleton.apiClient().rejectInvite(it) },
        object : Callback<Boolean> {
            override fun onError(msg: String) {
                Log.d(TAG, msg)
                viewModel.onMessageError.postValue(msg)
            }

            override fun onSuccess(obj: Boolean) {
                adapter.remove(roomInviteModel)
            }
        })


    override fun onRowClicked(roomInviteModel: RoomInviteModel) = request(
        roomInviteModel.roomInvite?.inviterId?.let { Singleton.apiClient().getUserModel(it) },
        object : Callback<UserModel> {
            override fun onError(msg: String) {
            }

            override fun onSuccess(obj: UserModel) {
                val intent = Intent(activity, UserActivity::class.java)
                intent.putExtra("userModel", obj)
                startActivity(intent)
            }
        })


    private fun refreshRoomInvitesEvent() = request(
        Singleton.apiClient().getRoomInviteModels(),
        object : Callback<MutableList<RoomInviteModel>> {
            override fun onError(msg: String) {
                Log.d(TAG, msg)
                viewModel.onMessageError.postValue(
                    resources.getString(R.string.err_room_invites_refresh)
                )
                swipeRefreshContainer.isRefreshing = false
            }

            override fun onSuccess(obj: MutableList<RoomInviteModel>) {
                if (obj.isNullOrEmpty()) {
                    viewModel.onEmptyList.postValue(true)
                } else {
                    viewModel.onEmptyList.postValue(false)
                    adapter.update(obj)
                    adapter.roomInviteModelsFull.addAll(obj)
                }
                swipeRefreshContainer.isRefreshing = false
            }
        })
}