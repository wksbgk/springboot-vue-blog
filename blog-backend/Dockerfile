# 换成 Eclipse Temurin（官方推荐替代 openjdk 镜像，国内好拉）
FROM eclipse-temurin:11-jre

WORKDIR /app
COPY target/*.jar app.jar

CMD java $JAVA_OPTS -jar app.jar
