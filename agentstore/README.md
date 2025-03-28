# AgentStore: Secure User Management Backend

This project is a Spring Boot application with Keycloak integration for secure user management.

## Architecture

The application consists of three main components:

1. **Spring Boot Application**: A REST API for user management
2. **Keycloak**: Authentication server for identity and access management
3. **PostgreSQL**: Database for both the application and Keycloak

## Prerequisites

- Docker and Docker Compose
- Java 17 (for development)
- Maven (for building the application)

## Running the Services

### Option 1: Run All Services Together

You can run all services at once using the provided script:

```bash
chmod +x setup-and-run.sh
./setup-and-run.sh
```

### Option 2: Run Each Service Independently

#### 1. Create Network

First, create a Docker network that all services will share:

```bash
docker network create agentstore-network
```

#### 2. Start PostgreSQL

```bash
# Make script executable
chmod +x postgres-init/init-multiple-databases.sh

# Start PostgreSQL
docker compose -f docker-compose-postgres.yml up -d
```

#### 3. Start Keycloak

```bash
# Start Keycloak
docker compose -f docker-compose-keycloak.yml up -d
```

#### 4. Start the Application

```bash
# Build the application first
mvn clean package

# Start the application
docker compose -f docker-compose-app.yml up -d
```

## Accessing the Services

- **Spring Boot Application**: http://localhost:8080/api
- **Keycloak Admin Console**: http://localhost:8081/admin (username: admin, password: admin)
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **PostgreSQL**: localhost:5432 (username: admin, password: admin)

## Default Users

The system is initialized with an admin user:

- Username: admin
- Password: admin
- Roles: admin, user

## Managing Keycloak

If you need to make changes to the Keycloak configuration, you can:

1. Log in to the Keycloak admin console at http://localhost:8081/admin
2. Select the "agentstore" realm
3. Make your changes through the UI
4. Export the realm configuration if needed:

```bash
docker exec agentstore-keycloak /opt/keycloak/bin/kc.sh export --realm agentstore --file /tmp/agentstore-realm.json
docker cp agentstore-keycloak:/tmp/agentstore-realm.json ./keycloak-init/agentstore-realm.json
```

## License

This project is licensed under the Apache 2.0 License. 





eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkMzc5ZjdjYS02YmZlLTQzMGYtODQ5Yi1mMDhkOTcwMjFjYjAifQ.eyJleHAiOjE3NDY2MjQyMjcsImlhdCI6MTc0MzE2ODIyNywianRpIjoiOGJlMWFjYWQtMTQ5Zi00MDBiLWE4ZTEtYWVkYTgwZDUxNzdkIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3JlYWxtcy9hZ2VudHN0b3JlIiwiYXVkIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgxL3JlYWxtcy9hZ2VudHN0b3JlIiwidHlwIjoiSW5pdGlhbEFjY2Vzc1Rva2VuIn0.fdHP3oV-9bxqARhzCsGcYW1VFWuxtUeLOrGXVgFmkq4