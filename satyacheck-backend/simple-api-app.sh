#!/bin/sh

# Update packages and install required tools
apk update
apk add curl

# Install Maven
apk add maven

# Create a simple Maven project structure
mkdir -p src/main/java/com/example
cp SimpleApiApplication.java src/main/java/com/example/

# Create a pom.xml file
cat > pom.xml << 'EOL'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.0</version>
    </parent>
    
    <groupId>com.example</groupId>
    <artifactId>simple-api</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <java.version>17</java.version>
    </properties>
    
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOL

# Create application.properties
mkdir -p src/main/resources
cat > src/main/resources/application.properties << 'EOL'
server.port=8080
spring.data.mongodb.uri=${SPRING_DATA_MONGODB_URI:mongodb://localhost:27017/satyacheck}
EOL

# Build the application
echo "Building simple API application..."
mvn package -DskipTests

# Run the application
echo "Starting simple API application..."
java -jar target/simple-api-1.0.0.jar