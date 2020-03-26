package vip.yazilim.p2g.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.androidadvance.topsnackbar.TSnackbar
import kotlinx.android.synthetic.main.fragment_home.*
import vip.yazilim.p2g.android.util.helper.UIHelper

/**
 * @author mustafaarifsisman - 04.02.2020
 * @contact mustafaarifsisman@gmail.com
 */
abstract class FragmentBase(var layout: Int) :
    Fragment() {
    lateinit var root: View
    lateinit var container: ViewGroup
    private var emptySnackbar: TSnackbar? = null
    private var errorSnackbar: TSnackbar? = null

    // Inflate view with container and setupViewModel and setupUI
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        root = inflater.inflate(layout, container, false)

        if (container != null) {
            this.container = container
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupUI()
    }

    abstract fun setupUI()
    abstract fun setupViewModel()

    fun setupDefaultObservers(viewModelBase: ViewModelBase) {
        viewModelBase.isViewLoading.observe(this, isViewLoadingObserver)
        viewModelBase.onMessageError.observe(this, onMessageErrorObserver)
        viewModelBase.onMessageInfo.observe(this, onMessageInfoObserver)
    }

    // Default Observers
    val isViewLoadingObserver = Observer<Boolean> {
        val visibility = if (it) View.VISIBLE else View.GONE
        progressBar?.visibility = visibility
        errorSnackbar?.dismiss()
    }

    val onMessageErrorObserver = Observer<String> { it ->
        errorSnackbar = UIHelper.showSnackBarError(container, it)
    }

    val onMessageInfoObserver = Observer<String> { it ->
        UIHelper.showSnackBarShortTop(container, it)
    }
}