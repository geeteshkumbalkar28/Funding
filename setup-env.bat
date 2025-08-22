@echo off
echo 🔧 Setting up environment variables...
echo.

REM Check if .env already exists
if exist ".env" (
    echo ⚠️  .env file already exists!
    echo Do you want to overwrite it? (y/n)
    set /p choice=
    if /i "%choice%" neq "y" (
        echo Setup cancelled.
        pause
        exit /b 0
    )
)

REM Copy template to .env
copy "env.template" ".env" >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ .env file created successfully!
    echo.
    echo 📝 Next steps:
    echo 1. Edit .env file with your actual values
    echo 2. Update JWT_SECRET with a secure random string
    echo 3. Update MAIL_PASSWORD with your actual email password
    echo 4. Update RAZORPAY keys with your production keys
    echo 5. Never commit .env file to version control
    echo.
    echo 🔒 Security reminder: Change default values in production!
) else (
    echo ❌ Failed to create .env file
    echo Make sure env.template exists in the current directory
)

pause
