# CoCo Net

CoCo Net is an Android app that connects coconut farmers and distributors. Farmers can post ads, create orders and locate distributors. Distributors can browse products, manage orders, and locate farmers.

## Tech Stack

- Java
- Android SDK 35 (min SDK 24)
- Firebase (Authentication, Firestore, Analytics)
- Google Maps, Location, and Places API

## Project Structure

```
app/src/main/java/com/s23010222/coconet/
├── adapter/   # RecyclerView adapters
├── model/     # Data models (Order, FarmerPost, etc.)
├── ui/        # Activities (auth, farmer, distributor, onboarding)
└── util/      # Helpers (notifications, storage)
```

## Setup

1. Open the project in Android Studio.
2. Add your Google Maps API key in `app/src/main/AndroidManifest.xml` (replace `Add Your Link Here`).
3. Configure Firebase by adding your `google-services.json` file to the `app/` directory.
4. Sync Gradle and run the app on an emulator or device.

## Requirements

- Android Studio
- JDK 11
- A Firebase project
- A Google Cloud project with Maps SDK enabled
