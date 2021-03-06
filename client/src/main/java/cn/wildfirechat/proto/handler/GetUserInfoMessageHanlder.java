package cn.wildfirechat.proto.handler;

import com.comsince.github.core.ByteBufferList;
import com.comsince.github.core.future.SimpleFuture;
import com.comsince.github.logger.Log;
import com.comsince.github.logger.LoggerFactory;
import com.comsince.github.push.Header;
import com.comsince.github.push.Signal;
import com.comsince.github.push.SubSignal;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.model.ProtoUserInfo;
import cn.wildfirechat.proto.ProtoService;
import cn.wildfirechat.proto.WFCMessage;

public class GetUserInfoMessageHanlder extends AbstractMessageHandler {
    Log log = LoggerFactory.getLogger(GetUserInfoMessageHanlder.class);

    public GetUserInfoMessageHanlder(ProtoService protoService) {
        super(protoService);
    }

    @Override
    public boolean match(Header header) {
        return header.getSignal() == Signal.PUB_ACK && header.getSubSignal() == SubSignal.UPUI;
    }

    @Override
    public void processMessage(Header header, ByteBufferList byteBufferList) {
        int errorCode = byteBufferList.get();
        SimpleFuture<ProtoUserInfo[]> simpleFuture = protoService.futureMap.remove(header.getMessageId());
        log.i("messageId "+header.getMessageId()+" error code "+errorCode);
        try {
            WFCMessage.PullUserResult pullUserResult = WFCMessage.PullUserResult.parseFrom(byteBufferList.getAllByteArray());
            ProtoUserInfo[] protoUserInfos = new ProtoUserInfo[pullUserResult.getResultCount()];
            List<ProtoUserInfo> protoUserInfoList = new ArrayList<>();
            for(WFCMessage.UserResult userResult :pullUserResult.getResultList()){
                WFCMessage.User user = userResult.getUser();
                ProtoUserInfo protoUserInfo = protoService.convertUser(user);
                log.i("userId "+user.getUid()+" userName "+user.getDisplayName());
                protoService.getImMemoryStore().addUserInfo(protoUserInfo);
                protoUserInfoList.add(protoUserInfo);
            }
            simpleFuture.setComplete(protoUserInfoList.toArray(protoUserInfos));
        } catch (InvalidProtocolBufferException e) {

        }

    }
}
