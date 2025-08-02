FROM tomcat:9-jdk17

RUN rm -rf /usr/local/tomcat/webapps/*
#
#COPY target/my-app.war /usr/local/tomcat/webapps/my-app.war
#COPY init-db/init.sql /docker-entrypoint-initdb.d/init.sql
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8086

#ENTRYPOINT ["/entrypoint.sh"]
