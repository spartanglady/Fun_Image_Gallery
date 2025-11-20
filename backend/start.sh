#!/bin/bash

# Start script for Photo Gallery Backend

echo "Starting Photo Gallery Backend..."
echo "=================================="
echo ""

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed. Please install Java 17 or higher."
    exit 1
fi

java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$java_version" -lt 17 ]; then
    echo "Error: Java 17 or higher is required. Current version: $java_version"
    exit 1
fi

echo "Java version: OK"
echo ""

# Build the project
echo "Building project..."
./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo ""
echo "Starting server on http://localhost:8080"
echo "Press Ctrl+C to stop"
echo ""

# Run the application
./gradlew bootRun
