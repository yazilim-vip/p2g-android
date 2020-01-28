package vip.yazilim.p2g.android.api

import retrofit2.Call
import retrofit2.http.*
import vip.yazilim.p2g.android.api.generic.Response
import vip.yazilim.p2g.android.model.p2g.*

/**
 * @author mustafaarifsisman - 28.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
interface Play2GetherWebApi{

    // User API
    @GET("/api/user/{id}")
    fun getUser(@Path("id") userId: String): Call<Response<User>>

    @PUT("/api/user/")
    fun updateUser(@Body user: User): Call<Response<User>>

    @GET("/api/user/{id}/model")
    fun getUserModel(@Path("id") userId: String): Call<Response<UserModel>>


    // Room API
    @POST("/api/room/")
    fun createRoom(@Body room: Room): Call<Response<Room>>

    @GET("/api/room/{id}")
    fun getRoom(@Path("id") roomId: Long): Call<Response<Room>>

    @GET("/api/room/")
    fun getAllRooms(): Call<List<Response<Room>>>

    @GET("/api/room/model/")
    fun getRoomModels(): Call<Response<List<RoomModel>>>

    @PUT("/apiroom/")
    fun updateRoom(@Body room: Room): Call<Response<Room>>

    @DELETE("/api/room/{id}")
    fun deleteRoom(@Path("id") roomId: Long): Call<Response<Boolean>>

    @GET("/api/room/model/{id}")
    fun getRoomModel(@Path("id") roomId: Long): Call<Response<RoomModel>>

    @POST("/api/room/{roomId}/invite/{userId}")
    fun inviteUser(@Path("roomId") roomId: Long, @Path("userId") userId: String): Call<Response<RoomInvite>>

    @POST("/api/room/invite/accept")
    fun acceptInvite(@Body roomInvite: RoomInvite): Call<Response<RoomUser>>

    @DELETE("/api/room/invite/{id}/reject")
    fun rejectInvite(@Path("id") roomId: Long): Call<Response<Boolean>>

    @POST("/api/room/{id}/join")
    fun joinRoom(@Path("id") roomId: Long, @Body password: String): Call<Response<RoomUser>>

    @DELETE("/api/room/leave")
    fun leaveRoom(): Call<Response<Boolean>>

    @GET("/api/room/{id}/users")
    fun getRoomUsers(@Path("id") roomId: Long): Call<Response<List<User>>>

    @PUT("/api/room/user/{id}/promote")
    fun promoteUser(@Path("id") roomId: String): Call<Response<RoomUser>>

    @PUT("/api/room/user/{id}/demote")
    fun demoteUser(@Path("id") roomId: String): Call<Response<RoomUser>>


    // Song API
    @GET("/api/song/{id}")
    fun getSong(@Path("id") songId: Long): Call<Response<Song>>

    @GET("/api/song/{roomId}/list")
    fun getRoomSongList(@Path("roomId") roomId: Long): Call<Response<List<Song>>>

    @POST("/api/song/{roomId}")
    fun addSongToRoom(@Path("roomId") roomId: Long, @Body searchModelList: List<SearchModel>): Call<Response<List<Song>>>

    @DELETE("/api/song/{songId}")
    fun removeSongFromRoom(@Path("songId") songId: Long): Call<Response<List<Song>>>

    @PUT("/api/song/{songId}/upvote")
    fun upvoteSong(@Path("songId") songId: Long): Call<Response<Int>>

    @PUT("/api/song/{songId}/downvote")
    fun downvoteSong(@Path("songId") songId: Long): Call<Response<Int>>


    // Friends API
    @POST("/api/friend/requests/")
    fun getRequests(): Call<Response<List<User>>>

    @GET("/api/friend/requests/{id}")
    fun getRequestById(@Path("id") friendRequestId: Long): Call<Response<List<User>>>

    @POST("/api/friend/requests/{userId}/send")
    fun send(@Path("userId") userId: String): Call<Response<Boolean>>

    @PUT("/api/friend/requests/{id}/accept")
    fun accept(@Path("id") friendRequestId: Long): Call<Response<Boolean>>

    @PUT("/api/friend/requests/{id}/reject")
    fun reject(@Path("id") friendRequestId: Long): Call<Response<Boolean>>

    @PUT("/api/friend/requests/{id}/ignore")
    fun ignore(@Path("id") friendRequestId: Long): Call<Response<Boolean>>


    // Authorization API
    @GET("/api/spotify/login")
    fun login(): Call<Response<User>>

    @POST("/api/spotify/token")
    fun updateAccessToken(@Body accessToken:String): Call<Response<String>>

    // Player API
    @POST("/api/spotify/player/play")
    fun play(@Body song: Song): Call<Response<List<Song>>>

    @POST("/api/spotify/player/{id}/play")
    fun startResume(@Path("id") roomId: Long): Call<Response<List<Song>>>

    @POST("/api/spotify/player/{id}/pause")
    fun pause(@Path("id") roomId: Long): Call<Response<List<Song>>>

    @POST("/api/spotify/player/{id}/next")
    fun next(@Path("id") roomId: Long): Call<Response<List<Song>>>

    @POST("/api/spotify/player/{id}/previous")
    fun previous(@Path("id") roomId: Long): Call<Response<List<Song>>>

    @POST("/api/spotify/player/{id}/seek/{ms}")
    fun seek(@Path("id") roomId: Long, @Path("ms") ms: Int): Call<Response<Int>>

    @POST("/api/spotify/player/{id}/repeat")
    fun repeat(@Path("id") roomId: Long): Call<Response<Boolean>>

}