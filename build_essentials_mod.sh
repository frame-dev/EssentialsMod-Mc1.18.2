#!/bin/bash

# Define variables
REPO_URL="https://github.com/frame-dev/EssentialsMod-Mc1.18.2-Forge.git"
REPO_NAME="EssentialsMod-Mc1.18.2-Forge"

# Clone the repository
if git clone "$REPO_URL"; then
    echo "Repository cloned successfully."
else
    echo "Failed to clone repository."
    exit 1
fi

# Navigate into the repository directory
cd "$REPO_NAME" || { echo "Failed to enter repository directory."; exit 1; }

# Build the project using Gradle
if ./gradlew jar; then
    echo "Build completed successfully."
else
    echo "Build failed."
    exit 1
fi
