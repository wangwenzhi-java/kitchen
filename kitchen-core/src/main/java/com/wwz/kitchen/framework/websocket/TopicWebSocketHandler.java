package com.wwz.kitchen.framework.websocket;

import javax.websocket.server.ServerEndpoint;

/**
 * 处理帖子的websocket
 * Created by wenzhi.wang.
 * on 2024/12/3.
 */
@ServerEndpoint(value = "/ws/topic/{token}")
public class TopicWebSocketHandler {

}
