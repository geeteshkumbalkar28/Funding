# Deployment Guide for Donorbox Backend

## 🚀 Deploying to Render

### Prerequisites
1. Render account
2. Git repository with your code
3. Environment variables configured

### Steps to Deploy

1. **Connect your repository to Render**
   - Go to [Render Dashboard](https://dashboard.render.com)
   - Click "New +" → "Web Service"
   - Connect your Git repository

2. **Configure the service**
   - **Name**: `donorbox-backend`
   - **Environment**: `Docker`
   - **Region**: Choose closest to your users
   - **Branch**: `main` (or your default branch)
   - **Build Command**: `mvn clean package -DskipTests`
   - **Start Command**: `java -jar target/donorbox-backend-0.0.1-SNAPSHOT.jar`

3. **Set Environment Variables**
   - `SPRING_PROFILES_ACTIVE`: `production`
   - `JWT_SECRET`: Generate a secure random string
   - `MAIL_USERNAME`: Your email username
   - `MAIL_PASSWORD`: Your email password
   - `RAZORPAY_KEY_ID`: Your Razorpay key ID
   - `RAZORPAY_KEY_SECRET`: Your Razorpay secret
   - `ADMIN_EMAIL`: Admin email address

4. **Create PostgreSQL Database**
   - Go to "New +" → "PostgreSQL"
   - Name: `donorbox-db`
   - Link it to your web service

5. **Deploy**
   - Click "Create Web Service"
   - Render will automatically build and deploy your application

### Environment Variables Reference

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | Yes | `production` |
| `SPRING_DATASOURCE_URL` | Database connection URL | Auto | From Render DB |
| `SPRING_DATASOURCE_USERNAME` | Database username | Auto | From Render DB |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Auto | From Render DB |
| `JWT_SECRET` | JWT signing secret | Yes | Generated |
| `JWT_EXPIRATION` | JWT expiration time | No | `86400` |
| `MAIL_HOST` | SMTP server host | No | `mail.alphaseam.com` |
| `MAIL_PORT` | SMTP server port | No | `587` |
| `MAIL_USERNAME` | Email username | Yes | - |
| `MAIL_PASSWORD` | Email password | Yes | - |
| `RAZORPAY_KEY_ID` | Razorpay key ID | Yes | - |
| `RAZORPAY_KEY_SECRET` | Razorpay secret | Yes | - |
| `ADMIN_EMAIL` | Admin email address | Yes | - |
| `APP_BASE_URL` | Application base URL | No | Auto-generated |

## 🔧 Troubleshooting

### Common Issues

1. **Build Failures**
   - Check Maven dependencies in `pom.xml`
   - Ensure Java 17 is specified
   - Verify all required dependencies are included

2. **Database Connection Issues**
   - Verify database is created and linked
   - Check environment variables are set correctly
   - Ensure PostgreSQL driver is in classpath

3. **Application Startup Failures**
   - Check logs in Render dashboard
   - Verify all required environment variables are set
   - Ensure port 8080 is exposed

4. **Health Check Failures**
   - Verify `/health` endpoint is accessible
   - Check application is starting correctly
   - Review startup logs

### Health Check Endpoint

Your application provides a health check endpoint at:
```
GET /health
```

Expected response:
```
Donorbox Backend API is running!
```

### Logs and Monitoring

- View logs in Render dashboard under your service
- Monitor application metrics
- Set up alerts for downtime

## 🐳 Local Docker Development

To test locally with Docker:

```bash
# Build and run with docker-compose
docker-compose up --build

# Or build and run manually
docker build -t donorbox-backend .
docker run -p 8080:8080 donorbox-backend
```

## 📝 Notes

- The application uses PostgreSQL in production (Render)
- MySQL is used for local development
- File uploads are stored in the `uploads` directory
- Swagger UI is disabled in production for security
- JWT tokens are used for authentication
