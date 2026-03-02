#!/bin/bash
set -e

cd "$(dirname "$0")"

echo "================================"
echo "Building and Running Tests"
echo "================================"
echo ""

# Try with java25 first
if command -v java25 &> /dev/null; then
    echo "Using java25 alias..."
    java25
    java -version
fi

echo ""
echo "Running Maven tests..."
./mvnw clean test -DskipITs -q

echo ""
echo "================================"
echo "Test run completed!"
echo "================================"

