## building a standalone app:

mvn clean compile assembly:single

## Running the standalone app:

### ubuntu

cd target
java -javaagent:/usr/share/java/jayatanaag.jar -jar pow-1.0-SNAPSHOT-jar-with-dependencies.jar

### other platforms

cd target
java -jar pow-1.0-SNAPSHOT-jar-with-dependencies.jar