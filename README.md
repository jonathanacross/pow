building a standalone app:

mvn clean compile assembly:single

Running the standalone app:

cd target
java -javaagent:/usr/share/java/jayatanaag.jar -jar pow-1.0-SNAPSHOT-jar-with-dependencies.jar
