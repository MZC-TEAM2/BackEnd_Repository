#!/bin/bash

# Multi-architecture Docker Build & Push Script
# Builds for both AMD64 and ARM64, pushes to Docker Hub

set -e

IMAGE_NAME="ddingsh9/mzc-core-api"
TAG="${1:-latest}"
PLATFORMS="linux/amd64,linux/arm64"
BUILDER_NAME="multiarch-builder"

echo "=========================================="
echo "Building: ${IMAGE_NAME}:${TAG}"
echo "Platforms: ${PLATFORMS}"
echo "=========================================="

# Check if buildx builder exists, create if not
if ! docker buildx inspect ${BUILDER_NAME} > /dev/null 2>&1; then
    echo "Creating buildx builder: ${BUILDER_NAME}"
    docker buildx create --name ${BUILDER_NAME} --use --bootstrap
else
    echo "Using existing builder: ${BUILDER_NAME}"
    docker buildx use ${BUILDER_NAME}
fi

# Login check
if ! docker info 2>/dev/null | grep -q "Username"; then
    echo "Docker Hub login required"
    docker login
fi

# Build and push multi-architecture image
echo "Building and pushing multi-architecture image..."
docker buildx build \
    --platform ${PLATFORMS} \
    --tag ${IMAGE_NAME}:${TAG} \
    --tag ${IMAGE_NAME}:latest \
    --push \
    .

echo "=========================================="
echo "Successfully pushed:"
echo "  - ${IMAGE_NAME}:${TAG}"
echo "  - ${IMAGE_NAME}:latest"
echo "=========================================="
