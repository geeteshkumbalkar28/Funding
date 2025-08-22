@echo off
echo Starting Donorbox Backend with Swagger UI...
echo.
echo Application will be available at:
echo   - Swagger UI: http://localhost:8080/swagger-ui.html
echo   - API Docs: http://localhost:8080/v3/api-docs
echo   - H2 Console: http://localhost:8080/h2-console
echo   - Health Check: http://localhost:8080/health
echo.
echo Admin credentials: admin / admin123
echo.

java -jar target/donorbox-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
