# Soleia: Illuminating Your Event Experiences

## Description
Soleia is an Android-based event management platform designed to streamline event discovery, booking, and management for attendees, organizers, and administrators. Developed as a final project for the Handheld Device Programming II unit at the Java Institute for Advanced Technology, it integrates real-time data handling with Firebase, secure payments via PayHere, and a custom PHP admin panel for enhanced control. The application leverages mobile commerce and location-based services to enhance user experiences across all roles.

## Features

### Attendees
- Browse events by category, location, or keyword.
- View detailed event information (date, venue, organizer, etc.).
- Purchase tickets securely using PayHere.
- Manage personal profiles and view booking history.
- Receive notifications about event updates and bookings.

### Organizers
- Create and edit events with images (via Cloudinary) and venue details (via Google Maps).
- Monitor analytics such as ticket sales and attendance.
- Scan QR codes for attendee check-in using ZXing.
- Update organizer profiles.

### Administrators
- Approve or reject events submitted by organizers via the PHP admin panel.
- Track payment transactions.
- Manage event categories (add, edit, remove).
- Monitor user activities and access user details.

## Technologies Used
- **Frontend**: Android (Java), XML for UI layouts
- **Backend**: Firebase (Firestore, Authentication, Storage), PHP for the admin panel
- **Third-party Services**:
  - PayHere for payment processing
  - Cloudinary for image uploads
  - Google Maps for location services
- **Architecture**: MVVM (Model-View-ViewModel)
- **Libraries**:
  - LiveData for reactive data handling
  - Glide for image loading
  - ZXing for QR code generation and scanning
  - OkHttp for HTTP requests (email notifications)

## Installation

### Prerequisites
- Android Studio with Java Development Kit (JDK)
- A local server environment (e.g., XAMPP, WAMP) for the PHP admin panel
- Composer for PHP dependency management
- Internet connection for third-party service configurations

### Android App
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application.git
   ```
2. **Open in Android Studio**:
   - Launch Android Studio and open the project from the cloned directory.
3. **Set Up Firebase**:
   - Visit the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project and add an Android app with the package name `com.example.soleia` (update if different).
   - Download the `google-services.json` file and place it in the `app/` directory.
4. **Configure Third-party Services**:
   - **PayHere**: Obtain merchant ID and secret from [PayHere](https://www.payhere.lk/). Add to `local.properties`:
     ```
     PAYHERE_MERCHANT_ID=your_merchant_id
     PAYHERE_MERCHANT_SECRET=your_merchant_secret
     ```
   - **Cloudinary**: Create an account at [Cloudinary](https://cloudinary.com/). Add credentials to `local.properties`:
     ```
     CLOUDINARY_CLOUD_NAME=your_cloud_name
     CLOUDINARY_API_KEY=your_api_key
     CLOUDINARY_API_SECRET=your_api_secret
     ```
   - **Google Maps**: Get an API key from [Google Cloud Console](https://console.cloud.google.com/). Add to `local.properties`:
     ```
     MAPS_API_KEY=your_maps_api_key
     ```
5. **Build and Run**:
   - Sync the project with Gradle, then build and run the app on an emulator or physical device (minimum SDK: API 21, Android 5.0).

### PHP Admin Panel
1. **Set Up Local Server**:
   - Install and configure a local server like XAMPP or WAMP.
2. **Copy Files**:
   - Copy the contents of the `admin-panel/` directory to your server’s root (e.g., `htdocs/soleia-admin`).
3. **Install Dependencies**:
   - Navigate to the `admin-panel/` directory in a terminal and run:
     ```bash
     composer install
     ```
4. **Configure Firebase Admin SDK**:
   - Download your Firebase service account key JSON from the Firebase Console (Project Settings > Service Accounts).
   - Place it in `admin-panel/config/` as `service-account.json`.
   - Update the PHP code to reference this file (e.g., in `sendEmailProcess.php` or other scripts).
5. **Access the Admin Panel**:
   - Start your local server and visit `http://localhost/soleia-admin` in a browser.
   - Sign in using Firebase Authentication (Google or email/password).

**Note**: Ensure `.gitignore` excludes `local.properties` and `service-account.json` to keep sensitive data secure. A `local.properties.example` file is provided as a template.

## Usage
- **Android App**:
  - Install and launch the app.
  - Follow the onboarding process to sign up or log in (via email/password or Google).
  - Explore events, book tickets, or manage events as an organizer based on your role.
- **Admin Panel**:
  - Access via the local server URL (`http://localhost/soleia-admin`).
  - Log in with admin credentials to manage events, users, payments, and categories.

## Screenshots
Below screenshots:

- ![Sign In](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(1).jpg?raw=true)
- ![Home Screen](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(2).jpg?raw=true)
- ![Event Details](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(3).jpg?raw=true)
- ![Admin Dashboard](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(4).jpg?raw=true)
- ![Admin Dashboard](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(5).jpg?raw=true)
- ![Admin Dashboard](https://github.com/PasanSWijekoon/Soleia-Event-Booking-Application/blob/main/1%20(6).jpg?raw=true)

## Project Structure
- `app/`: Android app source code
- `admin-panel/`: PHP admin panel source code
- `screenshots/`: Screenshots of the app and admin panel

## Testing
- **Unit Tests**: Run using Android Studio’s testing tools to verify business logic (e.g., ViewModels).
- **UI Tests**: Execute with Espresso to validate user flows (e.g., event booking).
- **Manual Testing**: Recommended for the PHP admin panel to ensure functionality.

## Known Issues
- Occasional UI lag on lower-end devices during animations (optimization needed).
- Email notification delays under poor network conditions (retry mechanism suggested).
- Offline mode inconsistencies due to Android battery optimizations.

## Contributing
Contributions are welcome! To contribute:
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m "Add your feature"`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Submit a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact
For questions or support, contact Pasan Wijekoon via:
- Email: [wijekoonpasan055@gmail.com]
- GitHub: [yourusername](https://github.com/PasanSWijekoon)

**Note**: This project was developed as part of the Handheld Device Programming II unit for the BEng in Software Engineering at the Java Institute for Advanced Technology by Pasan Wijekoon (Admission No: 200401511026).
```
