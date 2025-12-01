FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Создаём непользовательского пользователя для безопасности
RUN useradd -ms /bin/bash appuser
USER appuser

# Копируем заранее собранный jar из GitHub Actions артефакта
COPY build/libs/*.jar app.jar

# Порт Spring Boot берём из переменной окружения (можно переопределять при запуске)
ENV SERVER_PORT=8099
EXPOSE ${SERVER_PORT}

# Активируем нужный Spring профиль
ENV SPRING_PROFILES_ACTIVE=prod

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]