# Pete Jewels

Pete Jewels is a luxurious Android e-commerce application for premium jewelry such as diamond rings, emerald necklaces, and sapphire bracelets. Built with modern Android development practices, it provides a seamless shopping experience with elegant UI and robust features.

## 📱 Features Implemented

*   **Firebase Authentication:** Secure user registration and login using email and password via Firebase Auth.
*   **Modern UI with Jetpack Compose:** Fully declarative UI using Material 3 design, featuring a custom luxury gold theme.
*   **Boutique (Shop):** Browse high-end jewelry with toggleable List and Grid views.
*   **AR Try-On:** An Augmented Reality feature allowing users to virtually try on jewelry before purchasing. Features camera switching and screenshot capture capabilities.
*   **Shopping Cart & Wishlist:** Seamlessly manage items you want to buy or save for later, powered by a central ViewModel state.
*   **M-Pesa Payment Integration:** Checkout process includes a mock integration with the M-Pesa Daraja API for localized payments in Kenyan Shillings (Ksh) via STK Push.

## 🛠️ Technology Stack

*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel) with Navigation Compose
*   **Backend & Services:** 
    *   Firebase Authentication (Email/Password)
    *   Firebase BoM / Firebase AI (Prepared in dependencies)
*   **Minimum SDK:** API 30 (Android 11)
*   **Target SDK:** API 36

## 🚀 How to Load and Build

If you are receiving this project and want to build it on your machine, follow these steps:

### Prerequisites
1.  **Android Studio:** Ensure you have the latest version of Android Studio installed.
2.  **Java Development Kit (JDK):** The project is configured for Java 11.
3.  **Firebase Project:** To use the authentication features, you must have a Firebase project.

### Loading the Project
1.  Extract the project folder (`PeteJewels-master`).
2.  Open **Android Studio**.
3.  Select **Open** (or File -> Open) and navigate to the extracted `PeteJewels-master` directory. Click **OK**.
4.  Wait for Gradle to sync. Android Studio will automatically download the required dependencies (Compose, Firebase, Navigation, etc.).

### Setting up Firebase
The project relies on Firebase for authentication.
1.  Ensure the `app/google-services.json` file is present. 
2.  If you are setting up your own Firebase environment, go to the [Firebase Console](https://console.firebase.google.com/), create a new project, add an Android app with the package name `com.example.pete`, and replace the existing `google-services.json` with your new one.
3.  Enable **Email/Password** authentication in your Firebase console under *Authentication > Sign-in method*.

### Building and Running
1.  Connect a physical Android device (Android 11 or higher) via USB/Wi-Fi, or start an Android Emulator.
2.  In Android Studio, click the **Run** button (the green play icon) in the toolbar, or press `Shift + F10`.
3.  Alternatively, you can build an APK from the terminal:
    ```bash
    ./gradlew assembleDebug
    ```
    The generated APK will be located in `app/build/outputs/apk/debug/`.

## 💎 What the App is About
Pete Jewels serves as a digital storefront for a premium boutique. It allows users to create an account, browse exclusive collections of gemstones and jewelry, view high-quality images and details, virtually try them on using AR, add them to a wishlist or cart, and finally checkout using localized mobile money solutions (M-Pesa).
