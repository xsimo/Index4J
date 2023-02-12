#!/bin/bash
#mvn package
chcon --reference=/usr/share/tomcat/clufBase/ROOT.war target/index4j-1.0.war
chown tomcat target/index4j-1.0.war
chgrp tomcat target/index4j-1.0.war
chmod g+w target/index4j-1.0.war
cp target/index4j-1.0.war /usr/share/tomcat/index4jBase/ROOT.war

