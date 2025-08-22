@echo off
echo Generating PDF from deployment guide...
echo.

REM Check if pandoc is installed
pandoc --version >nul 2>&1
if %errorlevel% neq 0 (
    echo Pandoc is not installed. Please install pandoc to generate PDF.
    echo.
    echo Installation options:
    echo 1. Download from: https://pandoc.org/installing.html
    echo 2. Or use: winget install pandoc
    echo 3. Or use: chocolatey install pandoc
    echo.
    echo Alternative: Use online markdown to PDF converters
    echo - Copy the content from DEPLOYMENT_GUIDE_DEVOPS.md
    echo - Paste into: https://www.markdowntopdf.com/
    echo.
    pause
    exit /b 1
)

echo Pandoc found. Generating PDF...
echo.

REM Generate PDF with proper styling
pandoc DEPLOYMENT_GUIDE_DEVOPS.md ^
  -o "Donorbox_Deployment_Guide_DevOps.pdf" ^
  --pdf-engine=wkhtmltopdf ^
  --css=style.css ^
  --metadata title="Donorbox Platform Deployment Guide" ^
  --metadata author="DevOps Team" ^
  --metadata date="$(Get-Date -Format 'yyyy-MM-dd')" ^
  --toc ^
  --toc-depth=3 ^
  --number-sections

if %errorlevel% equ 0 (
    echo.
    echo PDF generated successfully: Donorbox_Deployment_Guide_DevOps.pdf
    echo.
    echo The PDF contains:
    echo - Complete deployment instructions
    echo - Environment configuration details
    echo - Database setup procedures
    echo - Troubleshooting guide
    echo - Security checklist
    echo - Monitoring guidelines
    echo.
) else (
    echo.
    echo PDF generation failed. Trying alternative method...
    echo.
    REM Try without CSS
    pandoc DEPLOYMENT_GUIDE_DEVOPS.md -o "Donorbox_Deployment_Guide_DevOps.pdf" --toc --toc-depth=3 --number-sections
    
    if %errorlevel% equ 0 (
        echo PDF generated successfully: Donorbox_Deployment_Guide_DevOps.pdf
    ) else (
        echo PDF generation failed. Please use online converter.
    )
)

echo.
echo Next steps:
echo 1. Review the generated PDF
echo 2. Share with DevOps team
echo 3. Store in project documentation
echo.
pause
