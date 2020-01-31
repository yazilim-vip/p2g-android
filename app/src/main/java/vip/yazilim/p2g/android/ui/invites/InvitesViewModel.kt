package vip.yazilim.p2g.android.ui.invites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * @author mustafaarifsisman - 31.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
class InvitesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is invites Fragment"
    }
    val text: LiveData<String> = _text
}