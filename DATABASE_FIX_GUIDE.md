# Database Fix Guide - Missing followup_email_count Column

## 🚨 Issue Description

Your frontend is getting this error when trying to make donations:
```
Payment failed: Failed to create donation and payment order: could not execute statement [ERROR: column "followup_email_count" of relation "donations" does not exist]
```

## 🔍 Root Cause

The `donations` table in your PostgreSQL database is missing the `followup_email_count` column that is defined in your `Donation` entity.

## ✅ Solutions

### **Option 1: Automatic Fix (Recommended)**

Run the provided script:
```bash
.\fix-database.bat
```

### **Option 2: Manual Database Fix**

#### **Using Render Dashboard:**
1. Go to your Render PostgreSQL database
2. Click on "Connect" → "External Database"
3. Use a PostgreSQL client (pgAdmin, DBeaver, etc.)
4. Connect to your database
5. Run this SQL command:

```sql
ALTER TABLE donations ADD COLUMN followup_email_count INTEGER NOT NULL DEFAULT 0;
```

#### **Using Command Line:**
```bash
psql "postgresql://cloud_fund_user:mL6Xuc48KpPPubIEexv0FlIir2ORJ8M0@dpg-d20u18re5dus7388a0v0-a.oregon-postgres.render.com/cloud_fund_db" -c "ALTER TABLE donations ADD COLUMN followup_email_count INTEGER NOT NULL DEFAULT 0;"
```

### **Option 3: Application Restart (Temporary Fix)**

The application has been modified to make the column nullable. Restart your application and it should work, but the column will still be missing from the database.

## 🔧 Verification

After running the fix, verify the column was added:

```sql
-- Check if column exists
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'donations' 
AND column_name = 'followup_email_count';

-- Show table structure
\d donations;
```

## 📋 Expected Result

You should see:
```
column_name        | data_type | is_nullable | column_default
-------------------+-----------+-------------+----------------
followup_email_count | integer   | NO          | 0
```

## 🚀 After Fix

1. **Restart your application** on Render
2. **Test the donation flow** from your frontend
3. **Verify emails are sent** properly
4. **Check that follow-up emails** work correctly

## 🔒 Security Note

The `followup_email_count` column tracks how many follow-up emails have been sent for each donation to prevent spam. It defaults to 0 and increments with each follow-up email sent.

## 📞 Support

If you continue to have issues:
1. Check the application logs on Render
2. Verify the database connection
3. Ensure all environment variables are set correctly
4. Test with a simple donation first
