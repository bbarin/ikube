#-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n
#export JAVA_OPTS=-XX:PermSize=256m -XX:MaxPermSize=512m -Xms1024m -Xmx4096m
#export MAVEN_OPTS="-XX:PermSize=128m -XX:MaxPermSize=256m -Xms512m -Xmx1024m -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=4000,server=y,suspend=n"
mvn jetty:run -DskipTests=true -DskipITs=true