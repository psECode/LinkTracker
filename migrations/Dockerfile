# Одноразовый контейнер миграций БД: Liquibase + драйвер PostgreSQL.
# Приложение (scrapper) при запуске в docker миграции не выполняет —
# их применяет этот контейнер, отрабатывая один раз и завершаясь.
FROM liquibase/liquibase:5.0.1

# В официальном образе нет драйвера PostgreSQL — добавляем его на этапе сборки.
RUN lpm add postgresql --global

USER root

# Changelog копируем из ресурсов scrapper, чтобы схема БД оставалась в одном месте.
COPY scrapper/src/main/resources/migrations /liquibase/changelog

# Файлы должны быть читаемы пользователем liquibase внутри образа
# (в рабочей копии у них может быть любой локальный umask).
RUN chown -R liquibase:liquibase /liquibase/changelog && chmod -R a+rX /liquibase/changelog

USER liquibase

# Путь к changelog задаётся относительно search-path (/liquibase/changelog);
# URL/логин/пароль приходят переменными окружения LIQUIBASE_COMMAND_URL /
# LIQUIBASE_COMMAND_USERNAME / LIQUIBASE_COMMAND_PASSWORD из docker-compose.yml.
ENV LIQUIBASE_COMMAND_CHANGELOG_FILE=changelog-master.xml

CMD ["update"]
