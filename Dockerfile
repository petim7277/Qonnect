# Stage 1: Build the app
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 1914

ENTRYPOINT ["sh", "-c", "java -jar app.jar \
  --spring.datasource.url=$DATASOURCE_-URL \
  --spring.datasource.username=$DATASOURCE_USERNAME \
  --spring.datasource.password=$DATASOURCE_PASSWORD \
  --spring.data.redis.host=$REDIS_HOST \
  --spring.data.redis.port=$REDIS_PORT \
  --spring.data.redis.password=$REDIS_PASSWORD \
  --spring.security.oauth2.resourceserver.jwt.jwk-set-uri=$JWK_SET_URI \
  --keycloak.client-id=$CLIENT_ID \
  --keycloak.client-secret=$CLIENT_SECRET \
  --keycloak.realm=$KEYCLOAK_REALM \
  --keycloak.server-url=$KEYCLOAK_SERVER_URL \
  --app.keycloak.tokenUrl=$KEYCLOAK_TOKENURL \
  --app.keycloak.logouturl=$LOGOUT_URL"]
