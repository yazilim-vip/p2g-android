package vip.yazilim.p2g.android.api.generic

import android.util.Log
import retrofit2.Call

/**
 * @author mustafaarifsisman - 28.01.2020
 * @contact mustafaarifsisman@gmail.com
 */

const val REQUEST_TAG = "Play2GetherRequest"

inline fun <reified T> request(call: Call<Response<T>>?, callback: Callback<T>?) {
    call?.enqueue { result ->
        when (result) {
            is Result.Success -> {
                if (result.response.isSuccessful) {
                    callback?.onSuccess(result.response.body()?.data as T)
                } else {
                    val msg = result.response.errorBody()!!.string()
                    Log.d("$REQUEST_TAG not successful ", msg)
                    callback?.onError(msg)
                }
            }
            is Result.Failure -> {
                val msg = result.error.message as String
                Log.d("$REQUEST_TAG failed ", msg)
                callback?.onError(msg)
            }
        }
    }
}