# 🚀 Donorbox Platform Deployment Guide
## DevOps Team Instructions

---

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Backend Deployment](#backend-deployment)
4. [Frontend Deployment](#frontend-deployment)
5. [Environment Configuration](#environment-configuration)
6. [Database Setup](#database-setup)
7. [Troubleshooting](#troubleshooting)
8. [Monitoring & Maintenance](#monitoring--maintenance)

---

## 🏗️ Project Overview

**Platform**: Donorbox - Crowdfunding Platform  
**Backend**: Spring Boot 3.2.1 (Java 17)  
**Frontend**: React.js  
**Database**: PostgreSQL (Render)  
**Deployment Platform**: Render  
**Payment Gateway**: Razorpay  

---

## 🏛️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React Frontend│    │ Spring Boot API │    │ PostgreSQL DB   │
│   (Render)      │◄──►│   (Render)      │◄──►│   (Render)      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         │                       │                       │
    ┌─────────┐            ┌─────────┐            ┌─────────┐
    │Razorpay │            │Email    │            │File     │
    │Payment  │            │Service  │            │Storage  │
    └─────────┘            └─────────┘            └─────────┘
```

---

## 🔧 Backend Deployment

### Prerequisites
- Java 17 JDK
- Maven 3.6+
- Git repository access
- Render account

### Deployment Steps

#### 1. Repository Setup
```bash
# Clone the repository
git clone <repository-url>
cd cloud_fund

# Verify Java version
java -version  # Should be Java 17

# Verify Maven
mvn -version
```

#### 2. Build the Application
```bash
# Clean and package
mvn clean package -DskipTests

# Verify JAR creation
ls -la target/donorbox-backend-0.0.1-SNAPSHOT.jar
```

#### 3. Render Configuration

**File**: `render.yaml`
```yaml
services:
  - type: web
    name: donorbox-backend
    env: docker
    plan: starter
    region: oregon
    healthCheckPath: /health
    envVars:
      - key: SPRING_DATASOURCE_URL
        fromDatabase:
          name: donorbox-db
          property: connectionString
      - key: SPRING_DATASOURCE_USERNAME
        fromDatabase:
          name: donorbox-db
          property: user
      - key: SPRING_DATASOURCE_PASSWORD
        fromDatabase:
          name: donorbox-db
          property: password
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: SERVER_PORT
        value: 8080
      - key: JWT_SECRET
        generateValue: true
      - key: JWT_EXPIRATION
        value: 86400
      - key: MAIL_HOST
        value: mail.alphaseam.com
      - key: MAIL_PORT
        value: 587
      - key: MAIL_USERNAME
        sync: false
      - key: MAIL_PASSWORD
        sync: false
      - key: RAZORPAY_KEY_ID
        sync: false
      - key: RAZORPAY_KEY_SECRET
        sync: false
      - key: ADMIN_EMAIL
        sync: false
      - key: APP_BASE_URL
        value: https://donorbox-backend.onrender.com
    buildCommand: mvn clean package -DskipTests
    startCommand: java -jar target/donorbox-backend-0.0.1-SNAPSHOT.jar

databases:
  - name: donorbox-db
    databaseName: cloud_fund_db_k3qg
    user: cloud_fund_user
    plan: starter
    region: oregon
```

#### 4. Environment Variables (Manual Setup)

**Required Environment Variables in Render Dashboard:**

| Variable | Value | Required | Notes |
|----------|-------|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `production` | ✅ | |
| `JWT_SECRET` | `kk3aFRZhghFRypZS4df7Wb5slZ1EOywq` | ✅ | |
| `JWT_EXPIRATION` | `86400` | ✅ | |
| `MAIL_HOST` | `mail.alphaseam.com` | ✅ | |
| `MAIL_PORT` | `587` | ✅ | |
| `MAIL_USERNAME` | `[ACTUAL_EMAIL]` | ✅ | Set real email |
| `MAIL_PASSWORD` | `[ACTUAL_PASSWORD]` | ✅ | Set real password |
| `RAZORPAY_KEY_ID` | `[PRODUCTION_KEY]` | ✅ | Production keys |
| `RAZORPAY_KEY_SECRET` | `[PRODUCTION_SECRET]` | ✅ | Production keys |
| `ADMIN_EMAIL` | `[ADMIN_EMAIL]` | ✅ | Admin email |

**Auto-configured by Render:**
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

---

## 🎨 Frontend Deployment

### Prerequisites
- Node.js 16+
- npm or yarn
- Git repository access

### Deployment Steps

#### 1. Repository Setup
```bash
# Clone frontend repository
git clone <frontend-repository-url>
cd donorbox-frontend

# Install dependencies
npm install
# or
yarn install
```

#### 2. Environment Configuration

**File**: `.env.production`
```env
REACT_APP_API_BASE_URL=https://donorbox-backend.onrender.com
REACT_APP_RAZORPAY_KEY_ID=your_production_razorpay_key
REACT_APP_APP_NAME=Donorbox
REACT_APP_VERSION=1.0.0
```

#### 3. Build the Application
```bash
# Build for production
npm run build
# or
yarn build

# Verify build
ls -la build/
```

#### 4. Render Configuration

**File**: `render.yaml` (Frontend)
```yaml
services:
  - type: web
    name: donorbox-frontend
    env: static
    plan: starter
    region: oregon
    buildCommand: npm install && npm run build
    staticPublishPath: ./build
    envVars:
      - key: REACT_APP_API_BASE_URL
        value: https://donorbox-backend.onrender.com
      - key: REACT_APP_RAZORPAY_KEY_ID
        sync: false
```

---

## 🗄️ Database Setup

### PostgreSQL Database Configuration

**Connection Details:**
- **Host**: `dpg-d2img6juibrs73a0gljg-a.oregon-postgres.render.com`
- **Database**: `cloud_fund_db_k3qg`
- **Username**: `cloud_fund_user`
- **Password**: `aOub47Il4DLSDLB7k3gJAdy9G086Y1iA`
- **Port**: `5432`

### Critical Database Fix

**⚠️ IMPORTANT**: Before deployment, run this SQL command to fix the missing column:

```sql
-- Connect to PostgreSQL database and run:
ALTER TABLE donations ADD COLUMN followup_email_count INTEGER NOT NULL DEFAULT 0;
```

### Database Schema Verification

```sql
-- Verify the column was added
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'donations' 
AND column_name = 'followup_email_count';

-- Expected result:
-- column_name        | data_type | is_nullable | column_default
-- -------------------+-----------+-------------+----------------
-- followup_email_count | integer   | NO          | 0
```

---

## 🔧 Environment Configuration Files

### Backend Environment Files

1. **`src/main/resources/application.properties`** - Default configuration
2. **`src/main/resources/application-production.properties`** - Production settings
3. **`src/main/resources/application-local.properties`** - Local development

### Frontend Environment Files

1. **`.env`** - Default environment variables
2. **`.env.production`** - Production environment variables
3. **`.env.development`** - Development environment variables

### Key Configuration Differences

| Environment | Database | Logging | Swagger | H2 Console |
|-------------|----------|---------|---------|------------|
| **Local** | MySQL | INFO | Enabled | Enabled |
| **Production** | PostgreSQL | WARN | Disabled | Disabled |

---

## 🚨 Troubleshooting

### Common Issues

#### 1. Database Connection Issues
```bash
# Error: column "followup_email_count" does not exist
# Solution: Run the database fix SQL command above
```

#### 2. Build Failures
```bash
# Check Java version
java -version  # Should be 17

# Check Maven
mvn -version

# Clean and rebuild
mvn clean package -DskipTests
```

#### 3. Environment Variable Issues
- Verify all required variables are set in Render dashboard
- Check variable names match exactly (case-sensitive)
- Ensure no extra spaces or quotes

#### 4. Health Check Failures
```bash
# Test health endpoint
curl https://donorbox-backend.onrender.com/health
# Expected: "Donorbox Backend API is running!"
```

### Log Analysis

**Backend Logs Location**: Render Dashboard → Your Service → Logs

**Key Log Patterns:**
- `ERROR`: Critical issues requiring immediate attention
- `WARN`: Potential issues to monitor
- `INFO`: Normal operation logs

---

## 📊 Monitoring & Maintenance

### Health Checks

**Backend Health Endpoint**: `GET /health`
**Expected Response**: `"Donorbox Backend API is running!"`

### Performance Monitoring

1. **Response Times**: Monitor API response times
2. **Database Connections**: Check connection pool usage
3. **Memory Usage**: Monitor JVM heap usage
4. **Error Rates**: Track 4xx and 5xx error rates

### Regular Maintenance

#### Weekly Tasks
- [ ] Review application logs
- [ ] Check database performance
- [ ] Verify backup status
- [ ] Monitor error rates

#### Monthly Tasks
- [ ] Update dependencies
- [ ] Review security patches
- [ ] Performance optimization
- [ ] Database maintenance

---

## 🔐 Security Checklist

### Backend Security
- [ ] JWT_SECRET is properly set and secure
- [ ] Database credentials are encrypted
- [ ] HTTPS is enabled
- [ ] CORS is properly configured
- [ ] Input validation is active
- [ ] SQL injection protection is enabled

### Frontend Security
- [ ] Environment variables are properly set
- [ ] API keys are not exposed in client-side code
- [ ] HTTPS is enforced
- [ ] Content Security Policy is configured

---

## 📞 Support Contacts

**Backend Issues**: Check application logs in Render dashboard  
**Database Issues**: Contact database administrator  
**Frontend Issues**: Check browser console and network tab  
**Deployment Issues**: Review Render deployment logs  

---

## 📝 Deployment Checklist

### Pre-Deployment
- [ ] Database schema is updated
- [ ] Environment variables are configured
- [ ] Application builds successfully
- [ ] Tests pass (if applicable)
- [ ] Security review completed

### Deployment
- [ ] Backend deployed successfully
- [ ] Frontend deployed successfully
- [ ] Database connection verified
- [ ] Health checks passing
- [ ] Payment gateway integration tested

### Post-Deployment
- [ ] Application is accessible
- [ ] All features working correctly
- [ ] Error monitoring is active
- [ ] Performance monitoring is active
- [ ] Backup verification completed

---

**Document Version**: 1.0  
**Last Updated**: August 2025  
**Maintained By**: DevOps Team
