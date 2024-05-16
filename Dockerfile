FROM maven:3.9.6-amazoncorretto-21
ENV HOME=/usr/app
RUN mkdir -p $HOME
WORKDIR $HOME
ADD . $HOME
RUN mvn -f $HOME/commonapi/pom.xml install