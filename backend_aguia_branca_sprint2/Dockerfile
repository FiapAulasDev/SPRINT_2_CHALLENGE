# Estágio 1: Build (Compila o projeto usando o Maven)
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Gera o arquivo .jar ignorando os testes para ser mais rápido
RUN mvn clean package -DskipTests

# Estágio 2: Execução (Roda a aplicação em uma imagem super leve)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copia o .jar gerado no Estágio 1 para o Estágio 2
COPY --from=build /app/target/*.jar app.jar
# Expõe a porta padrão do Spring Boot
EXPOSE 8080
# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]