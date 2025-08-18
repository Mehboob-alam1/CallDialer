# Cashfree Payment Integration Setup Guide

This guide will help you set up the Cashfree payment gateway integration in your dialer app.

## 🔧 **Prerequisites**

1. **Cashfree Merchant Account**: Sign up at [Cashfree Dashboard](https://merchant.cashfree.com/merchant/sign-up)
2. **Android Studio**: Latest version
3. **Test Phone**: For testing UPI payments

## 📋 **Step 1: Get Cashfree Credentials**

### 1.1 Create Merchant Account
1. Go to [Cashfree Merchant Dashboard](https://merchant.cashfree.com/merchant/sign-up)
2. Complete the registration process
3. Verify your account

### 1.2 Get API Credentials
1. Login to your Cashfree Dashboard
2. Go to **Settings** → **API Keys**
3. Copy your **App ID** and **Secret Key**
4. Note: Use **Test** credentials for development, **Production** for live

## 🔑 **Step 2: Update Configuration**

### 2.1 Update Config.java
Replace the placeholder credentials in `app/src/main/java/com/mehboob/dialeradmin/Config.java`:

```java
// Replace with your actual Cashfree credentials
public static final String CASHFREE_APP_ID = "YOUR_APP_ID_HERE";
public static final String CASHFREE_SECRET_KEY = "YOUR_SECRET_KEY_HERE";

// Change to true for production
public static final boolean IS_PRODUCTION = false;
```

### 2.2 Environment Configuration
- **Sandbox (Testing)**: `IS_PRODUCTION = false`
- **Production (Live)**: `IS_PRODUCTION = true`

## 🧪 **Step 3: Testing Setup**

### 3.1 Test Cards (Sandbox)
Use these test cards for testing:

| Card Type | Card Number | Expiry | CVV |
|-----------|-------------|--------|-----|
| Visa | 4111 1111 1111 1111 | 12/25 | 123 |
| Mastercard | 5555 5555 5555 4444 | 12/25 | 123 |

### 3.2 Test UPI IDs
Use these test UPI IDs:
- `success@upi`
- `failure@upi`
- `pending@upi`

### 3.3 Test Bank Accounts
Use these test bank details:
- **Account Number**: 1234567890
- **IFSC**: SBIN0001234
- **Account Holder**: Test User

## 📱 **Step 4: Build and Test**

### 4.1 Build the App
```bash
./gradlew assembleDebug
```

### 4.2 Test Payment Flow
1. Launch the app
2. Register/Login as admin
3. Add phone number in profile
4. Go to Packages
5. Select a plan
6. Complete test payment

## 🔍 **Step 5: Debugging**

### 5.1 Enable Logging
The app has logging enabled. Check Logcat for:
- `CashfreePaymentService` - Payment flow logs
- `OrderApiClient` - API request/response logs

### 5.2 Common Issues

#### Issue: "Invalid credentials"
**Solution**: Verify your App ID and Secret Key in Config.java

#### Issue: "Order creation failed"
**Solution**: 
1. Check network connectivity
2. Verify API credentials
3. Check order ID format (must be unique)

#### Issue: "Payment failed"
**Solution**:
1. Use correct test credentials
2. Check UPI app installation
3. Verify phone number format

## 🚀 **Step 6: Go Live**

### 6.1 Production Setup
1. Update `Config.java`:
   ```java
   public static final boolean IS_PRODUCTION = true;
   ```

2. Replace test credentials with production credentials

3. Update `build.gradle.kts`:
   ```kotlin
   buildTypes {
       release {
           isMinifyEnabled = true
           proguardFiles(...)
       }
   }
   ```

### 6.2 Security Checklist
- [ ] Use production credentials
- [ ] Enable ProGuard/R8
- [ ] Test with real payment methods
- [ ] Verify webhook endpoints
- [ ] Test order status verification

## 📊 **Step 7: Monitor Payments**

### 7.1 Cashfree Dashboard
Monitor payments in your Cashfree Dashboard:
- **Orders**: View all orders
- **Settlements**: Track settlements
- **Reports**: Download payment reports

### 7.2 App Analytics
The app tracks:
- Payment success/failure rates
- Plan subscription analytics
- User payment behavior

## 🔧 **Advanced Configuration**

### 7.1 Custom UPI Apps
To customize UPI app order, modify in `CashfreePaymentService.java`:

```java
CFUPIIntentCheckout cfupiIntentCheckout = new CFUPIIntentCheckout.CFUPIIntentBuilder()
    .setOrder(Arrays.asList(
        CFUPIIntentCheckout.CFUPIApps.BHIM,
        CFUPIIntentCheckout.CFUPIApps.PHONEPE,
        CFUPIIntentCheckout.CFUPIApps.GPAY,
        CFUPIIntentCheckout.CFUPIApps.PAYTM
    ))
    .build();
```

### 7.2 Custom Theme
To customize payment UI theme:

```java
CFIntentTheme cfTheme = new CFIntentTheme.CFIntentThemeBuilder()
    .setPrimaryTextColor("#000000")
    .setBackgroundColor("#FFFFFF")
    .build();
```

## 📞 **Support**

### Cashfree Support
- **Documentation**: [Cashfree Docs](https://www.cashfree.com/docs/)
- **API Reference**: [API Docs](https://www.cashfree.com/docs/api/)
- **Support**: support@cashfree.com

### App Support
For app-specific issues, check:
1. Logcat for error messages
2. Network connectivity
3. Firebase configuration
4. Android permissions

## ✅ **Verification Checklist**

- [ ] Cashfree credentials configured
- [ ] Test payments working
- [ ] Order creation successful
- [ ] Payment verification working
- [ ] Plan activation working
- [ ] Error handling implemented
- [ ] Logging enabled
- [ ] Production ready

## 🎯 **Next Steps**

1. **Test thoroughly** with different payment methods
2. **Monitor** payment success rates
3. **Optimize** based on user feedback
4. **Scale** as your user base grows

---

**Note**: Always test thoroughly in sandbox mode before going live with real payments.
