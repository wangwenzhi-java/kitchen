# 环境版本
FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER wang@wangwenzhi.wang

# JVM调优参数等额外参数
ENV PARAMS ""

#配置时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 指定容器端口
EXPOSE 8888

# 设置工作目录
WORKDIR /app

# 添加本地 JAR 到容器内
COPY kitchen-web.jar /app/kitchen_web_app.jar

# 容器启动后执行的命令
ENTRYPOINT ["java", "-jar","/app/kitchen_web_app.jar" , "-Djava.security.egd=file:/dev/./urandom","$PARAMS"]
