FROM alpine:3.5
MAINTAINER phxql

# Install Java 8
RUN apk --update add openjdk8-jre && rm -rf /var/cache/apk/*

EXPOSE 7777

WORKDIR /
CMD /server-*/bin/server

ADD config.yaml /
ADD restwars.tar /
