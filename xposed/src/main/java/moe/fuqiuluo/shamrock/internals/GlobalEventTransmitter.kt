package moe.fuqiuluo.shamrock.internals

import com.tencent.qqnt.kernel.nativeinterface.MsgElement
import com.tencent.qqnt.kernel.nativeinterface.MsgRecord
import io.kritor.Scene
import io.kritor.contact
import io.kritor.event.MessageEvent
import io.kritor.event.messageEvent
import io.kritor.sender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.io.core.BytePacketBuilder
import qq.service.QQInterfaces

internal object GlobalEventTransmitter: QQInterfaces() {
    private val messageEventFlow by lazy {
        MutableSharedFlow<Pair<MsgRecord, MessageEvent>>()
    }
    //private val noticeEventFlow by lazy {
    //    MutableSharedFlow<NoticeEvent>()
    //}
    //private val requestEventFlow by lazy {
    //    MutableSharedFlow<RequestEvent>()
    //}

    //private suspend fun pushNotice(noticeEvent: NoticeEvent) = noticeEventFlow.emit(noticeEvent)

    //private suspend fun pushRequest(requestEvent: RequestEvent) = requestEventFlow.emit(requestEvent)

    private suspend fun transMessageEvent(record: MsgRecord, message: MessageEvent) = messageEventFlow.emit(record to message)

    object MessageTransmitter {
        suspend fun transGroupMessage(
            record: MsgRecord,
            elements: ArrayList<MsgElement>,
        ): Boolean {
            transMessageEvent(record, messageEvent {
                this.time = record.msgTime.toInt()
                this.scene = Scene.GROUP
                this.messageId = record.msgId
                this.messageSeq = record.msgSeq
                this.contact = contact {
                    this.scene = scene
                    this.peer = record.peerUin.toString()
                    this.subPeer = record.peerUid
                }
                this.sender = sender {
                    this.uin = record.senderUin
                    this.uid = record.senderUid
                    this.nick = record.sendNickName
                }
            })
            return true
        }

        suspend fun transPrivateMessage(
            record: MsgRecord,
            elements: ArrayList<MsgElement>,
        ): Boolean {
            val botUin = app.longAccountUin
            var nickName = record.sendNickName

            return true
        }

        suspend fun transTempMessage(
            record: MsgRecord,
            elements: ArrayList<MsgElement>,
            groupCode: Long,
            fromNick: String,
        ): Boolean {
            val botUin = app.longAccountUin
            var nickName = record.sendNickName

            return true
        }

        suspend fun transGuildMessage(
            record: MsgRecord,
            elements: ArrayList<MsgElement>,
        ): Boolean {
            val botUin = app.longAccountUin
            var nickName = record.sendNickName

            return true
        }
    }

    /*
    /**
     * 文件通知 通知器
     */
    object FileNoticeTransmitter {
        /**
         * 推送私聊文件事件
         */
        suspend fun transPrivateFileEvent(
            msgTime: Long,
            userId: Long,
            fileId: String,
            fileSubId: String,
            fileName: String,
            fileSize: Long,
            expireTime: Long,
            url: String
        ): Boolean {
            pushNotice(NoticeEvent(
                time = msgTime,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.PrivateUpload,
                operatorId = userId,
                userId = userId,
                senderId = userId,
                privateFile = PrivateFileMsg(
                    id = fileId,
                    name = fileName,
                    size = fileSize,
                    url = url,
                    subId = fileSubId,
                    expire = expireTime
                )
            ))
            return true
        }

        /**
         * 推送私聊文件事件
         */
        suspend fun transGroupFileEvent(
            msgTime: Long,
            userId: Long,
            groupId: Long,
            uuid: String,
            fileName: String,
            fileSize: Long,
            bizId: Int,
            url: String
        ): Boolean {
            pushNotice(NoticeEvent(
                time = msgTime,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.GroupUpload,
                operatorId = userId,
                userId = userId,
                groupId = groupId,
                file = GroupFileMsg(
                    id = uuid,
                    name = fileName,
                    size = fileSize,
                    busid = bizId.toLong(),
                    url = url
                )
            ))
            return true
        }
    }

    /**
     * 群聊通知 通知器
     */
    object GroupNoticeTransmitter {
        suspend fun transGroupSign(time: Long, target: Long, action: String?, rankImg: String?, groupCode: Long): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.Notify,
                subType = NoticeSubType.Sign,
                userId = target,
                groupId = groupCode,
                target = target,
                signDetail = SignDetail(
                    rankImg = rankImg,
                    action = action
                )
            ))
            return true
        }

        suspend fun transGroupPoke(time: Long, operation: Long, target: Long, action: String?, suffix: String?, actionImg: String?, groupCode: Long): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.Notify,
                subType = NoticeSubType.Poke,
                operatorId = operation,
                userId = operation,
                groupId = groupCode,
                target = target,
                pokeDetail = PokeDetail(
                    action = action,
                    suffix = suffix,
                    actionImg = actionImg
                )
            ))
            return true
        }

        suspend fun transGroupMemberNumChanged(
            time: Long,
            target: Long,
            targetUid: String,
            groupCode: Long,
            operator: Long,
            operatorUid: String,
            noticeType: NoticeType,
            noticeSubType: NoticeSubType
        ): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = noticeType,
                subType = noticeSubType,
                operatorId = operator,
                userId = target,
                senderId = operator,
                target = target,
                groupId = groupCode,
                targetUid = targetUid,
                operatorUid = operatorUid,
                userUid = targetUid
            ))
            return true
        }

        suspend fun transGroupAdminChanged(
            msgTime: Long,
            target: Long,
            targetUid: String,
            groupCode: Long,
            setAdmin: Boolean
        ): Boolean {
            pushNotice(NoticeEvent(
                time = msgTime,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.GroupAdminChange,
                subType = if (setAdmin) NoticeSubType.Set else NoticeSubType.UnSet,
                operatorId = 0,
                userId = target,
                userUid = targetUid,
                target = target,
                targetUid = targetUid,
                groupId = groupCode
            ))
            return true
        }

        suspend fun transGroupBan(
            msgTime: Long,
            subType: NoticeSubType,
            operator: Long,
            operatorUid: String,
            target: Long,
            targetUid: String,
            groupCode: Long,
            duration: Int
        ): Boolean {
            pushNotice(NoticeEvent(
                time = msgTime,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.GroupBan,
                subType = subType,
                operatorId = operator,
                userId = target,
                senderId = operator,
                target = target,
                groupId = groupCode,
                duration = duration,
                operatorUid = operatorUid,
                targetUid = targetUid
            ))
            return true
        }

        suspend fun transGroupMsgRecall(
            time: Long,
            operator: Long,
            target: Long,
            groupCode: Long,
            msgHash: Int,
            tipText: String
        ): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.GroupRecall,
                operatorId = operator,
                userId = target,
                msgId = msgHash,
                tip = tipText,
                groupId = groupCode
            ))
            return true
        }

        suspend fun transCardChange(
            time: Long,
            targetId: Long,
            oldCard: String,
            newCard: String,
            groupId: Long
        ): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.GroupCard,
                userId = targetId,
                cardNew = newCard,
                cardOld = oldCard,
                groupId = groupId
            ))
            return true
        }

        suspend fun transTitleChange(
            time: Long,
            targetId: Long,
            title: String,
            groupId: Long
        ): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.Notify,
                userId = targetId,
                groupId = groupId,
                title = title,
                subType = NoticeSubType.Title
            ))
            return true
        }

        suspend fun transEssenceChange(
            time: Long,
            senderUin: Long,
            operatorUin: Long,
            msgId: Int,
            groupId: Long,
            subType: NoticeSubType
        ): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.Essence,
                senderId = senderUin,
                groupId = groupId,
                operatorId = operatorUin,
                msgId = msgId,
                subType = subType
            ))
            return true
        }
    }

    /**
     * 私聊通知 通知器
     */
    object PrivateNoticeTransmitter {
        suspend fun transPrivatePoke(msgTime: Long, operation: Long, target: Long, action: String?, suffix: String?, actionImg: String?): Boolean {
            pushNotice(NoticeEvent(
                time = msgTime,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.Notify,
                subType = NoticeSubType.Poke,
                operatorId = operation,
                userId = operation,
                senderId = operation,
                target = target,
                pokeDetail = PokeDetail(
                    actionImg = actionImg,
                    action = action,
                    suffix = suffix
                )
            ))
            return true
        }

        suspend fun transPrivateRecall(time: Long, operation: Long, msgHashId: Int, tipText: String): Boolean {
            pushNotice(NoticeEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Notice,
                type = NoticeType.FriendRecall,
                operatorId = operation,
                userId = operation,
                msgId = msgHashId,
                tip = tipText
            ))
            return true
        }

    }

    /**
     * 请求 通知器
     */
    object RequestTransmitter {
        suspend fun transFriendApp(time: Long, operation: Long, tipText: String, flag: String): Boolean {
            pushRequest(RequestEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Request,
                type = RequestType.Friend,
                userId = operation,
                comment = tipText,
                flag = flag
            ))
            return true
        }

        suspend fun transGroupApply(
            time: Long,
            applier: Long,
            applierUid: String,
            reason: String,
            groupCode: Long,
            flag: String,
            subType: RequestSubType
        ): Boolean {
            pushRequest(RequestEvent(
                time = time,
                selfId = app.longAccountUin,
                postType = PostType.Request,
                type = RequestType.Group,
                userId = applier,
                userUid = applierUid,
                comment = reason,
                groupId = groupCode,
                subType = subType,
                flag = flag
            ))
            return true
        }
    }*/

    suspend inline fun onMessageEvent(collector: FlowCollector<Pair<MsgRecord, MessageEvent>>) {
        messageEventFlow.collect {
            GlobalScope.launch {
                collector.emit(it)
            }
        }
    }

    /*
    suspend inline fun onNoticeEvent(collector: FlowCollector<NoticeEvent>) {
        noticeEventFlow.collect {
            GlobalScope.launch {
                collector.emit(it)
            }
        }
    }

    suspend inline fun onRequestEvent(collector: FlowCollector<RequestEvent>) {
        requestEventFlow.collect {
            GlobalScope.launch {
                collector.emit(it)
            }
        }
    }*/
}