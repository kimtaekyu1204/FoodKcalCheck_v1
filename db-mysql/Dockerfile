# MySQL Dockerfile
FROM mysql:8.0

# Set environment variables
ENV MYSQL_ROOT_PASSWORD=rootpassword \
    MYSQL_DATABASE=checkfood \
    MYSQL_USER=checkfood_user \
    MYSQL_PASSWORD=checkfood_pass

# Copy initialization scripts
COPY ./init/*.sql /docker-entrypoint-initdb.d/

# Expose MySQL port
EXPOSE 3306

# MySQL uses the default entrypoint from the base image
# Data will be persisted in /var/lib/mysql (mounted as volume)
