# Estágio 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia só o pom.xml primeiro para aproveitar o cache das dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código-fonte e gera o .jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Estágio 2: Runtime (imagem final leve e com correção do Java4Shell)
FROM eclipse-temurin:21.0.13.1_2-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]