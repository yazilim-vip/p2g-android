package vip.yazilim.p2g.android.util.refrofit

import android.util.Log
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import vip.yazilim.p2g.android.api.client.ApiClient
import vip.yazilim.p2g.android.api.client.SpotifyApiClient
import vip.yazilim.p2g.android.api.generic.Callback
import vip.yazilim.p2g.android.api.generic.SpotifyRequest
import vip.yazilim.p2g.android.constant.GeneralConstants.LOG_TAG
import vip.yazilim.p2g.android.constant.SpotifyConstants
import vip.yazilim.p2g.android.constant.TokenConstants
import vip.yazilim.p2g.android.model.spotify.TokenModel
import vip.yazilim.p2g.android.util.data.SharedPrefSingleton

/**
 * @author mustafaarifsisman - 22.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
class TokenAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken =
            SharedPrefSingleton.read(TokenConstants.REFRESH_TOKEN, TokenConstants.UNDEFINED)
        val updatedToken = refreshExpiredToken(refreshToken.toString())

        SharedPrefSingleton.write(TokenConstants.ACCESS_TOKEN, updatedToken)

        updateAccessTokenOnPlay2Gether(updatedToken)

        return response.request.newBuilder()
            .header("Authorization", "Bearer $updatedToken")
            .build()
    }


    companion object {
        fun refreshExpiredToken(refreshToken: String): String {
            SpotifyRequest.build(
                SpotifyApiClient.build().refreshExpiredToken(
                    SpotifyConstants.CLIENT_ID,
                    SpotifyConstants.CLIENT_SECRET,
                    SpotifyConstants.GRANT_TYPE_REFRESH_TOKEN_REQUEST,
                    refreshToken
                ), object : Callback<TokenModel> {
                    override fun onError(msg: String) {
                        Log.d(LOG_TAG, msg)
                    }

                    override fun onSuccess(obj: TokenModel) {
                        SharedPrefSingleton.write(TokenConstants.ACCESS_TOKEN, obj.access_token)
                        SharedPrefSingleton.write(TokenConstants.REFRESH_TOKEN, obj.refresh_token)
                        Log.d(LOG_TAG, "Token refreshed")
                    }
                })

            return SharedPrefSingleton.read(TokenConstants.ACCESS_TOKEN, TokenConstants.UNDEFINED)
                .toString()
        }
    }

    private fun updateAccessTokenOnPlay2Gether(accessToken: String) {
        vip.yazilim.p2g.android.api.generic.Request.build(
            ApiClient.build().updateAccessToken(accessToken),
            object : Callback<String> {
                override fun onError(msg: String) {
                    Log.d(LOG_TAG, msg)
                }

                override fun onSuccess(obj: String) {
                }
            })
    }

}
