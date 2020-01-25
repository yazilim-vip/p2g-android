package vip.yazilim.p2g.android.data.websocket

import org.joda.time.LocalDateTime
import java.io.Serializable

/**
 * @author mustafaarifsisman - 24.01.2020
 * @contact mustafaarifsisman@gmail.com
 */
data class ChatMessage(
    var userId: String,
    var userName: String,
    var roomId: String,
    var message: String,
    var timestamp: LocalDateTime
) : Serializable


