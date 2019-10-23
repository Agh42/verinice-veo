FROM openjdk:11-jdk AS BUILDER

ARG GRADLE_OPTS

ENV APP_HOME=/root/dev/myapp
RUN mkdir -p $APP_HOME
WORKDIR $APP_HOME

# get gradle
COPY gradle gradle
COPY gradlew gradlew.bat $APP_HOME/
RUN ./gradlew --version

# copy all gradle files so gradle knows which dependencies to fetch
COPY build.gradle settings.gradle ./
COPY veo-core/build.gradle veo-core/build.gradle
COPY veo-data-xml/build.gradle veo-data-xml/build.gradle
COPY veo-json-validation/build.gradle veo-json-validation/build.gradle
COPY veo-rest/build.gradle veo-rest/build.gradle
COPY veo-vna-import/build.gradle veo-vna-import/build.gradle

COPY buildSrc/src buildSrc/src
COPY buildSrc/build.gradle buildSrc/build.gradle

RUN ./gradlew --no-daemon --dry-run build
COPY . .
RUN ./gradlew --no-daemon build


FROM openjdk:11-jre-slim
LABEL org.opencontainers.image.vendor="SerNet GmbH"
LABEL org.opencontainers.image.authors=verinice@sernet.de
LABEL org.opencontainers.image.ref.name=veo
LABEL org.opencontainers.image.version=0.1.0

WORKDIR /root
COPY --from=BUILDER /root/dev/myapp/veo-rest/build/libs/veo-rest-0.1.0-SNAPSHOT.jar .
EXPOSE 8070
CMD ["java", "-jar", "veo-rest-0.1.0-SNAPSHOT.jar"]