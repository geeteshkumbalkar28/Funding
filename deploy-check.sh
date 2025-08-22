#!/bin/bash

echo "🔍 Donorbox Backend Deployment Check"
echo "====================================="

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    exit 1
else
    echo "✅ Maven is installed"
fi

# Check if Java 17 is available
if ! java -version 2>&1 | grep -q "version \"17"; then
    echo "❌ Java 17 is not installed or not the default version"
    echo "Current Java version:"
    java -version
    exit 1
else
    echo "✅ Java 17 is installed"
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker is not installed (optional for local testing)"
else
    echo "✅ Docker is installed"
fi

# Check if pom.xml exists
if [ ! -f "pom.xml" ]; then
    echo "❌ pom.xml not found"
    exit 1
else
    echo "✅ pom.xml found"
fi

# Check if Dockerfile exists
if [ ! -f "Dockerfile" ]; then
    echo "❌ Dockerfile not found"
    exit 1
else
    echo "✅ Dockerfile found"
fi

# Check if render.yaml exists
if [ ! -f "render.yaml" ]; then
    echo "❌ render.yaml not found"
    exit 1
else
    echo "✅ render.yaml found"
fi

# Try to build the project
echo ""
echo "🔨 Testing Maven build..."
if mvn clean compile -q; then
    echo "✅ Maven build successful"
else
    echo "❌ Maven build failed"
    exit 1
fi

# Check if target directory was created
if [ -d "target" ]; then
    echo "✅ Target directory created"
else
    echo "❌ Target directory not found after build"
    exit 1
fi

echo ""
echo "🎉 All checks passed! Your project is ready for deployment."
echo ""
echo "📋 Next steps:"
echo "1. Push your code to Git repository"
echo "2. Connect repository to Render"
echo "3. Set up environment variables in Render dashboard"
echo "4. Create PostgreSQL database in Render"
echo "5. Deploy!"
echo ""
echo "📖 See DEPLOYMENT.md for detailed instructions"
