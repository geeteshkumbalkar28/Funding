#!/bin/bash

echo "🔧 Setting up environment variables..."
echo

# Check if .env already exists
if [ -f ".env" ]; then
    echo "⚠️  .env file already exists!"
    read -p "Do you want to overwrite it? (y/n): " choice
    if [[ ! "$choice" =~ ^[Yy]$ ]]; then
        echo "Setup cancelled."
        exit 0
    fi
fi

# Copy template to .env
if cp "env.template" ".env" 2>/dev/null; then
    echo "✅ .env file created successfully!"
    echo
    echo "📝 Next steps:"
    echo "1. Edit .env file with your actual values"
    echo "2. Update JWT_SECRET with a secure random string"
    echo "3. Update MAIL_PASSWORD with your actual email password"
    echo "4. Update RAZORPAY keys with your production keys"
    echo "5. Never commit .env file to version control"
    echo
    echo "🔒 Security reminder: Change default values in production!"
else
    echo "❌ Failed to create .env file"
    echo "Make sure env.template exists in the current directory"
    exit 1
fi
