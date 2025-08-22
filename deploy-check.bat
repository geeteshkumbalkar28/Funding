@echo off
echo 🔍 Donorbox Backend Deployment Check
echo =====================================

REM Check if Maven is installed
mvn -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Maven is not installed or not in PATH
    pause
    exit /b 1
) else (
    echo ✅ Maven is installed
)

REM Check if Java 17 is available
java -version 2>&1 | findstr "version \"17" >nul
if %errorlevel% neq 0 (
    echo ❌ Java 17 is not installed or not the default version
    echo Current Java version:
    java -version
    pause
    exit /b 1
) else (
    echo ✅ Java 17 is installed
)

REM Check if Docker is available
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  Docker is not installed (optional for local testing)
) else (
    echo ✅ Docker is installed
)

REM Check if pom.xml exists
if not exist "pom.xml" (
    echo ❌ pom.xml not found
    pause
    exit /b 1
) else (
    echo ✅ pom.xml found
)

REM Check if Dockerfile exists
if not exist "Dockerfile" (
    echo ❌ Dockerfile not found
    pause
    exit /b 1
) else (
    echo ✅ Dockerfile found
)

REM Check if render.yaml exists
if not exist "render.yaml" (
    echo ❌ render.yaml not found
    pause
    exit /b 1
) else (
    echo ✅ render.yaml found
)

REM Try to build the project
echo.
echo 🔨 Testing Maven build...
mvn clean compile -q
if %errorlevel% neq 0 (
    echo ❌ Maven build failed
    pause
    exit /b 1
) else (
    echo ✅ Maven build successful
)

REM Check if target directory was created
if exist "target" (
    echo ✅ Target directory created
) else (
    echo ❌ Target directory not found after build
    pause
    exit /b 1
)

echo.
echo 🎉 All checks passed! Your project is ready for deployment.
echo.
echo 📋 Next steps:
echo 1. Push your code to Git repository
echo 2. Connect repository to Render
echo 3. Set up environment variables in Render dashboard
echo 4. Create PostgreSQL database in Render
echo 5. Deploy!
echo.
echo 📖 See DEPLOYMENT.md for detailed instructions
pause
