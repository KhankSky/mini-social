#!/bin/bash

# Environment configuration
export DOCKER_IMAGE_FE=khanksky/social-fe:latest

echo "=== Deploying Frontend $DOCKER_IMAGE_FE ==="

# 1. Pull latest image
echo "1. Pulling latest image..."
docker pull $DOCKER_IMAGE_FE

# 2. Start container
echo "2. Starting container..."
if command -v docker-compose &> /dev/null; then
    docker-compose -f docker-compose.fe.yml up -d
else
    docker compose -f docker-compose.fe.yml up -d
fi

echo "=== Deployment Complete! ==="
echo "Frontend is running on port 3000"
