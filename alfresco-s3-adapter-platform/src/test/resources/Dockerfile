FROM ${docker.acs.image}:${alfresco.platform.version}

COPY *.amp .

USER root

RUN java -jar /usr/local/tomcat/alfresco-mmt/alfresco-mmt-*.jar install alfresco-s3-adapter-*.amp /usr/local/tomcat/webapps/alfresco -verbose -nobackup -force

USER alfresco
