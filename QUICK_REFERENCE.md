# 🚀 Quick Reference - Donorbox Deployment

## 📋 Critical Information

### Database Connection
```
Host: dpg-d2img6juibrs73a0gljg-a.oregon-postgres.render.com
Database: cloud_fund_db_k3qg
Username: cloud_fund_user
Password: aOub47Il4DLSDLB7k3gJAdy9G086Y1iA
```

### Critical Database Fix (MUST DO FIRST)
```sql
ALTER TABLE donations ADD COLUMN followup_email_count INTEGER NOT NULL DEFAULT 0;
```

### Environment Variables (Set in Render Dashboard)
```
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=kk3aFRZhghFRypZS4df7Wb5slZ1EOywq
MAIL_USERNAME=[ACTUAL_EMAIL]
MAIL_PASSWORD=[ACTUAL_PASSWORD]
RAZORPAY_KEY_ID=[PRODUCTION_KEY]
RAZORPAY_KEY_SECRET=[PRODUCTION_SECRET]
ADMIN_EMAIL=[ADMIN_EMAIL]
```

## 🔧 Build Commands

### Backend
```bash
mvn clean package -DskipTests
```

### Frontend
```bash
npm install && npm run build
```

## 📁 Key Files

### Backend
- `render.yaml` - Render configuration
- `src/main/resources/application-production.properties` - Production config
- `target/donorbox-backend-0.0.1-SNAPSHOT.jar` - Built application

### Frontend
- `.env.production` - Production environment variables
- `build/` - Built application

## 🚨 Common Issues

1. **Database Column Missing**: Run the SQL fix above
2. **Build Failures**: Check Java 17 and Maven
3. **Environment Variables**: Verify in Render dashboard
4. **Health Check**: Test `/health` endpoint

## 📞 Health Check
```bash
curl https://donorbox-backend.onrender.com/health
# Expected: "Donorbox Backend API is running!"
```

## 📖 Full Documentation
See `DEPLOYMENT_GUIDE_DEVOPS.md` for complete instructions.
