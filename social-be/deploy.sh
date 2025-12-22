#!/bin/bash

# Environment configuration
export DOCKER_IMAGE_NAME=khanksky/social-be:latest
export DB_HOST=192.168.1.203
export DB_USER=root
export DB_PASSWORD=123456

echo "=== Deploying $DOCKER_IMAGE_NAME ==="
echo "Database Host: $DB_HOST"

# 1. Pull latest image
echo "1. Pulling latest image..."
docker pull $DOCKER_IMAGE_NAME

# 2. Start container
echo "2. Starting container..."
if command -v docker-compose &> /dev/null; then
    docker-compose -f docker-compose.prod.yml up -d
else
    docker compose -f docker-compose.prod.yml up -d
fi

echo "=== Deployment Complete! ==="
echo "App is running on port 9090"
