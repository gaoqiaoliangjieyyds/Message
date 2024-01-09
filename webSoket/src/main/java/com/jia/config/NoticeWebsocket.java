package com.jia.config;/**
 * @author ChenJia
 * @create 2024-01-09 9:04
 */

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *@ClassName NoticeWebsocket
 *@Description @ServerEndpoint 注解是一个类层次的注解，它的功能主要是将目前的类定义成一个websocket服务器端,
 *  注解的值将被用于监听用户连接的终端访问URL地址,客户端可以通过这个URL来连接到WebSocket服务器端
 *@Author jia
 *@Date 2024/1/9 9:04
 *@Version 1.0
 **/
@ServerEndpoint("/notice/{userId}")
@Component
@Slf4j
public class NoticeWebsocket {
    //记录连接的客户端
    public static Map<String, Session> clients = new ConcurrentHashMap<>();

    /**
     * userId关联sid（解决同一用户id，在多个web端连接的问题）
     */
    public static Map<String, Set<String>> conns = new ConcurrentHashMap<>();

    private String sid = null;

    private String userId;


    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        String tempSid = session.getId();
        this.sid = tempSid;
        this.userId = userId;
        clients.put(tempSid, session);

        Set<String> clientSet = conns.get(userId);
        if (clientSet==null){
            clientSet = new HashSet<>();
            conns.put(userId,clientSet);
        }
        clientSet.add(tempSid);
        //session.getBasicRemote().sendText("aa");发送消息
        log.error("DeviceWebsocket---onOpen===>id:{}--{}--连接数：{}--在线数：{}--当前设备连接数：{}", userId, tempSid, clients.size(), conns.size(), conns.get(userId).size());
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        String tempSid = session.getId();
        //log.info(this.sid + "连接断开！");
        closeSession(tempSid, userId);
    }

    public void closeSession(String sid, String userId){
        Session s  = clients.remove(sid);
        if (s!=null){
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Set<String> clientSet = conns.get(userId);
        if (clientSet!=null){
            clientSet.remove(sid);
        }
        int currentConnectCount = clientSet.size();
        if(clientSet!=null && clientSet.size()==0){
            conns.remove(userId);
            currentConnectCount = 0;
        }
        log.error("DeviceWebsocket---onClose===>id:--{}--{}--连接数：{}--在线数：{}--当前设备连接数：{}", userId, sid, clients.size(), conns.size(), currentConnectCount);
        //log.error("在线人数===="+clients.size());
    }

    public static void sendMessage(String noticeType){
        NoticeWebsocketResp noticeWebsocketResp = new NoticeWebsocketResp();
        noticeWebsocketResp.setNoticeType(noticeType);
        sendMessage(noticeWebsocketResp);
    }


    /**
     * 浏览器发送消息到服务端，该方法被调用
     * <p>
     * 张三  -->  李四
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
//        try {
//            //将消息推送给指定的用户
//            Message msg = JSONUtil.toBean(message, Message.class);
//
//            //获取 消息接收方的用户 id
//            String toUserId = msg.getToUserId();
//            String msgMessage = msg.getMessage();
//
//            // 消息入库
//            DmMessagesMapper messagesMapper = SpringContextHolder.getBean(DmMessagesMapper.class);
//            DmMessages dmMessages = new DmMessages();
//            dmMessages.setContent(msgMessage);
//            dmMessages.setMessagesStatus(0);
//            dmMessages.setMessagesType(0);
//            dmMessages.setSendId(userId);
//            dmMessages.setUserId(Long.valueOf(toUserId));
//            Date now = new Date();
//            dmMessages.setCreateTime(now);
//            dmMessages.setUpdateTime(now);
//            dmMessages.setIsDelete(0);
//            messagesMapper.insert(dmMessages);
//            // 获取消息接收方用户对象的session对象
////            StringRedisTemplate stringRedisTemplate = SpringContextHolder.getBean(StringRedisTemplate.class);
////            Object cacheSession = stringRedisTemplate.opsForValue().get(ONLINE_USERS + toUserId);
//            Session cacheSession = onlineUsers.get(String.valueOf(toUserId));
//            if (cacheSession == null) {
//                // log
//                return;
//            }
//            // 接受信息人的session
////            Session session2 = JSONUtil.parseObj(cacheSession).toBean(Session.class);
//
//            // 根据userId查询用户
////            DmUserService userService = SpringContextHolder.getBean(DmUserService.class);
//
////            DmUser user = userService.getById(toUserId);
//            // userId--我
//            String msgTemplate = MessageUtils.getMessage(false, userId, msgMessage);
//            // 消息入库
//            cacheSession.getBasicRemote().sendText(msgTemplate);
//
//
////            //获取 消息接收方的用户
////            String toName = msg.getToUserId();
////            String mess = msg.getMessage();
////            //获取消息接收方用户对象的session对象
////            Session session = onlineUsers.get(toName);
////            String user = (String) this.httpSession.getAttribute("user");
////            String msg1 = MessageUtils.getMessage(false, user, mess);
////            session.getBasicRemote().sendText(msg1);
//        } catch (Exception e) {
//            //记录日志
//        }
    }



    /**
     * 发送给所有用户
     * @param noticeWebsocketResp
     */
    public static void sendMessage(NoticeWebsocketResp noticeWebsocketResp){
        String message = com.alibaba.fastjson.JSONObject.toJSONString(noticeWebsocketResp);
        for (Session session1 : NoticeWebsocket.clients.values()) {
            try {
                session1.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据用户id发送给某一个用户
     * **/
    public static void sendMessageByUserId(String userId, NoticeWebsocketResp noticeWebsocketResp) {
        if (!StringUtils.isEmpty(userId)) {
            String message = com.alibaba.fastjson.JSONObject.toJSONString(noticeWebsocketResp);
            Set<String> clientSet = conns.get(userId);
            if (clientSet != null) {
                Iterator<String> iterator = clientSet.iterator();
                while (iterator.hasNext()) {
                    String sid = iterator.next();
                    Session session = clients.get(sid);
                    if (session != null) {
                        try {
                            session.getBasicRemote().sendText(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else {
                        iterator.remove();;
                    }
                }
            }
        }
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }

}
