FROM ubuntu:14.04
RUN apt-get update
RUN apt-get install -y default-jdk
RUN apt-get install -y maven2
COPY ./Java/ /src/
WORKDIR /src/
RUN ant
ENTRYPOINT ["java", "-classpath", "/src/build/airplay.jar:/src/lib/jmdns.jar", "com.jameslow.AirPlay"]
