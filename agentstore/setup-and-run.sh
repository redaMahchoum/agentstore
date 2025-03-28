#!/bin/bash

# Create required directories
mkdir -p postgres-init keycloak-init

# Create the network if it doesn't exist
docker network create agentstore-network 2>/dev/null || echo "Network agentstore-network already exists"

# Make scripts executable
chmod +x postgres-init/init-multiple-databases.sh

# Start PostgreSQL first
echo "Starting PostgreSQL..."
docker compose -f docker-compose-postgres.yml up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 15

# Start Keycloak
echo "Starting Keycloak..."
docker compose -f docker-compose-keycloak.yml up -d

# Wait for Keycloak to be initialized
echo "Waiting for Keycloak to be initialized..."
sleep 40

# Start the application
echo "Starting the application..."
docker compose -f docker-compose-app.yml up -d

echo "All services started!"
echo "Application: http://localhost:8080/api"
echo "Keycloak: http://localhost:8081"
echo "PostgreSQL: localhost:5432" 