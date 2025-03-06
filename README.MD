<p align="center">
	<strong>基于springboot开发，前端使用vue。</strong>
</p>
<p align="center">
	<a target="_blank" href="https://github.com/wangwenzhi-java/kitchen/blob/master/LICENSE">
		<img src="https://img.shields.io/badge/license-GPL%20v3-yellow.svg" ></img>
	</a>
	<img src="https://img.shields.io/badge/springboot-2.7.13-Cyan.svg" ></img>
	<img src="https://img.shields.io/badge/Maven-3.8.8-green.svg" ></img>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8-blue.svg" ></img>
	</a>
	<img src="https://img.shields.io/badge/MySQL-5.7+-red.svg" ></img>
	<img src="https://img.shields.io/badge/Redis-3.2.6-orange.svg" ></img>
	<img src="https://img.shields.io/badge/RabbitMq-4.0.2-pink.svg" ></img>
</p>
<p align="center">
	<strong>地址：<a target="_blank" href='https://github.com/wangwenzhi-java/kitchen'>Github</a></strong>
</p>
<p align="center">
	<strong>前端项目地址：<a target="_blank" href='https://github.com/wangwenzhi-java/kitchen_web'>Github</a></strong>
</p>
<p align="center">
	<strong>演示demo：<a target="_blank" href='https://miao.minlan.fun'>喵</a></strong>
</p>
<p align="center">
    <strong>邮箱：</strong>wwz@wangwenzhi.wang
</p>

----
# 模块划分

|      模块      |              释义              | 备注 |
|:------------:|:----------------------------:| :------------: |
| kitchen-core | 核心业务类模块，提供基本的数据操作、工具处理、安全加密等 | 该模块只是作为核心依赖包存在 |
| kitchen-web  |     基础功能模块 提供api入口与业务分发      | 单独部署
| kitchen-chat |   聊天模块api、处理消息队列异步消息、增强功能等   | 该模块作为单独项目打包部署 |

# 技术栈
- docker
- docker-compose
- Springboot 2.7.13
- spring security 
- Logback
- Redis sentinel
- Lombok
- Websocket
- MySQL、Mybatis Plus
- vue
- 七牛云
- Nginx
- jenkins
- rabbitmq
- 。。。

# 2.0版本构想
基于ActivityPub协议构建一个中继服务，实现kitchen联邦，喵圈topic共享，跨实例用户交互等！

# 重要声明
1.提问题时请优先选择[GitHub Issues](https://github.com/wangwenzhi-java/kitchen/issues)（方便问题追踪和一对一解决），其次[我的博客-留言板](https://wangwenzhi.top/guestbook)，再次QQ群（QQ群消息较多，提问请注意节奏、时机），最次加我QQ好友直接提问（不推荐）
2.本人唯一官网：[https://wangwenzhi.top](https://wangwenzhi.top)  
3.本项目开源地址： [Github](https://github.com/wangwenzhi-java/kitchen)
4.本项目禁止商业用途！