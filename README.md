# Enhanced Call Dialer Admin App

A comprehensive Android call dialer application with admin features, Firebase integration, and Cashfree payment gateway for premium plans.

## Features

### 🔥 Core Features
- **Modern Dialer Interface**: Clean, intuitive dial pad with contact integration
- **Default Dialer Support**: Request to be set as the system's default dialer app
- **Call History Tracking**: Complete call history with detailed analytics
- **Contact Management**: View and manage contacts with search functionality
- **Admin Dashboard**: Comprehensive admin panel for call monitoring
- **Incoming Call Handling**: Full support for incoming and outgoing calls

### 💳 Payment Integration
- **Cashfree Payment Gateway**: Secure payment processing for premium plans
- **Multiple Subscription Plans**: Weekly, Monthly, 3-Months, and Yearly plans
- **Premium Features**: Enhanced functionality for premium users
- **Payment History**: Track all payment transactions

### 🔐 Authentication & Security
- **Firebase Authentication**: Secure user login and registration
- **Admin Role Management**: Role-based access control
- **Phone Number Verification**: Required for payment processing
- **Secure Data Storage**: Firebase Realtime Database integration

### 📊 Call Analytics
- **Real-time Call Tracking**: Monitor calls as they happen
- **Call History Analytics**: Detailed call statistics and reports
- **Child Number Tracking**: Track calls made via specific child numbers
- **Premium Call Identification**: Distinguish premium vs regular calls

### 🎨 User Interface
- **Material Design**: Modern, responsive UI following Material Design guidelines
- **Dark/Light Theme Support**: Customizable appearance
- **Smooth Animations**: Enhanced user experience with animations
- **Accessibility Features**: Support for accessibility tools

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+ (API Level 24)
- Google Firebase account
- Cashfree merchant account

### 1. Firebase Setup

1. Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add your Android app to the project
3. Download `google-services.json` and place it in the `app/` directory
4. Enable Authentication with Email/Password
5. Enable Realtime Database with the following structure:

```json
{
  "admins": {
    "admin_uid": {
      "uid": "admin_uid",
      "email": "admin@example.com",
      "phoneNumber": "+1234567890",
      "role": "admin",
      "isActivated": true,
      "isPremium": false,
      "planType": "",
      "planActivatedAt": 0,
      "planExpiryAt": 0,
      "createdAt": 1234567890,
      "childNumber": ""
    }
  },
  "call_history": {
    "call_id": {
      "id": "call_id",
      "adminId": "admin_uid",
      "childNumber": "+1234567890",
      "contactNumber": "+0987654321",
      "contactName": "John Doe",
      "callType": "OUTGOING",
      "callStartTime": 1234567890,
      "callEndTime": 1234567890,
      "callDuration": 120,
      "isPremiumCall": false,
      "planType": "",
      "createdAt": 1234567890
    }
  }
}
```

### 2. Cashfree Setup

1. Sign up for a Cashfree merchant account
2. Get your App ID and Secret Key from the Cashfree dashboard
3. Update the credentials in `OrderApiClient.java`:

```java
private static final String APP_ID = "your_app_id_here";
private static final String SECRET_KEY = "your_secret_key_here";
```

### 3. Build and Run

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project
5. Run on a device or emulator

## Usage

### Admin Registration/Login
1. Launch the app
2. Register as a new admin or login with existing credentials
3. Provide email, password, and phone number (required for payments)

### Adding Child Number
1. Navigate to the admin panel
2. Add child phone numbers for tracking
3. All calls made via these numbers will be tracked

### Making Calls
1. Use the dial pad to enter phone numbers
2. Make calls directly from the app
3. All calls are automatically tracked and saved to Firebase

### Setting as Default Dialer
1. Navigate to Settings from the dialer home screen
2. Tap on "Set as Default Dialer" option
3. Follow the system prompts to set the app as your default dialer
4. Once set, all phone calls will be handled by this app

### Viewing Call History
1. Navigate to Call History from the menu
2. Filter calls by type (Incoming, Outgoing, Missed, Premium)
3. Search for specific contacts or numbers
4. View detailed call information

### Premium Plans
1. Navigate to Packages from the menu
2. Select a subscription plan
3. Complete payment via Cashfree
4. Enjoy premium features

## File Structure

```
app/src/main/java/com/mehboob/dialeradmin/
├── activities/
│   ├── MainActivity.java
│   ├── AuthActivity.java
│   ├── CallHistoryActivity.java
│   ├── PacakageActivity.java
│   └── ...
├── fragments/
│   ├── DialPadFragment.java
│   ├── ContactsFragment.java
│   ├── CallListFragment.java
│   └── ...
├── models/
│   ├── AdminModel.java
│   ├── CallHistory.java
│   └── ...
├── adapters/
│   ├── CallHistoryAdapter.java
│   └── ...
├── payment/
│   ├── CashfreePaymentHelper.java
│   ├── OrderApiClient.java
│   └── ...
├── CallManager.java
├── CallReceiver.java
└── MyApplication.java
```

## Permissions Required

- `READ_CALL_LOG`: To access call history
- `READ_PHONE_STATE`: To detect call states
- `CALL_PHONE`: To make phone calls
- `READ_CONTACTS`: To access contacts
- `INTERNET`: For Firebase and payment integration
- `RECEIVE_BOOT_COMPLETED`: For call tracking on device restart
- `ANSWER_PHONE_CALLS`: To answer incoming calls
- `MANAGE_OWN_CALLS`: To manage call connections
- `BIND_TELECOM_CONNECTION_SERVICE`: For default dialer functionality
- `BIND_INCALL_SERVICE`: For in-call service management
- `MODIFY_PHONE_STATE`: To modify phone state for calls

## Security Features

- Firebase Authentication for secure login
- Role-based access control
- Secure payment processing
- Data encryption in transit
- Permission-based feature access

## Troubleshooting

### Common Issues

1. **Firebase Connection Issues**
   - Verify `google-services.json` is in the correct location
   - Check Firebase project settings
   - Ensure internet connectivity

2. **Payment Issues**
   - Verify Cashfree credentials
   - Check network connectivity
   - Ensure phone number is provided

3. **Call Tracking Issues**
   - Grant necessary permissions
   - Check device compatibility
   - Verify Firebase database rules

### Support

For technical support or feature requests, please contact the development team.

## License

This project is proprietary software. All rights reserved.

## Version History

- **v1.0**: Initial release with basic dialer functionality
- **v1.1**: Added Firebase integration and call tracking
- **v1.2**: Integrated Cashfree payment gateway
- **v1.3**: Enhanced UI and added premium features
- **v2.0**: Complete admin dashboard with analytics
- **v2.1**: Added default dialer support with full call handling capabilities
