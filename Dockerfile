# Multi-stage build para otimizar tamanho da imagem
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar apenas pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte e buildar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Instalar curl para health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Copiar JAR da aplicação
COPY --from=builder /app/target/tyler-api-*.jar app.jar

# Criar diretório para configurações
RUN mkdir -p /app/config

# Criar usuário não-root para segurança
RUN adduser --disabled-password --gecos '' appuser && \
    chown -R appuser:appuser /app
USER appuser

# Configurações JVM otimizadas para Cloud Run
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Dspring.profiles.active=production"

# Expor porta (Cloud Run usa $PORT)
EXPOSE 8080

# Health check com timeout maior para Cloud Run
HEALTHCHECK --interval=60s --timeout=10s --start-period=120s --retries=5 \
    CMD curl -f http://localhost:${PORT:-8080}/actuator/health || exit 1

# Comando de inicialização com logs detalhados
CMD ["sh", "-c", "echo '🚀 Iniciando Tyler API...' && java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]