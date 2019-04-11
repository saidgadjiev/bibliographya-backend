FROM openjdk:8-jre

RUN apt-get -y update && apt-get -y install imagemagick
COPY ./fonts/* /usr/share/fonts/ttf-roboto/

WORKDIR /app
COPY ./target/app.jar .

ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005", "app.jar"]