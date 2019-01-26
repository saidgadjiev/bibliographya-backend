FROM openjdk:8-jre
WORKDIR /app
COPY ./target/app.jar .
ENTRYPOINT ["/usr/bin/java"]
CMD ["-jar", "app.jar"]