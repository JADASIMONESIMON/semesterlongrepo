# EduEnroll Application ReadMe

## Project Overview
EduEnroll is a JavaFX-based desktop application designed for managing user information within an educational institution. The application incorporates a login system, user registration, and CRUD (Create, Read, Update, Delete) operations for user records. It uses a MySQL database for data storage and incorporates modular controllers for each functionality.

---

## Project Structure
- **`MainApplication`**: Entry point of the application that initializes the primary stage and handles the transition from the splash screen to the login interface.
- **Controllers**:
  - **`LoginController`**: Manages user authentication and redirection to other application views.
  - **`SignUpController`**: Handles user registration with input validation.
  - **`DB_GUI_Controller`**: Manages database records, user CRUD operations, CSV import/export, and GUI interactions.
  - **`SpalshScreenController`**: Displays a splash screen with introductory text.
- **FXML Files**:
  - `about.fxml`: Displays the about section.
  - `db_interface_gui.fxml`: Main database GUI interface.
  - `login.fxml`: Login page interface.
  - `signUp.fxml`: User registration page.
  - `splashscreen.fxml`: Splash screen displayed at startup.

---

## Features
### Login System
- **Controller**: `LoginController`
- Provides authentication using credentials saved during registration.
- Redirects to the main GUI on successful login or displays an error alert for invalid login attempts.

### User Registration
- **Controller**: `SignUpController`
- Validates input fields:
  - Username: Lowercase letters only.
  - Name: Capitalized first letters.
  - Email: Must end with `@farmingdale.edu`.
  - Password: Must contain uppercase, lowercase, and numeric characters.
- Stores credentials securely and redirects to the login screen after successful registration.

### CRUD Operations
- **Controller**: `DB_GUI_Controller`
- Adds, edits, and deletes user records stored in a MySQL database.
- Validates user inputs before performing operations.
- Features data import/export via CSV files.
- Displays user details in a TableView, with additional image upload functionality.

### Splash Screen
- **Controller**: `SpalshScreenController`
- Displays a welcome message at application startup.

### Theming
- Switch between light and dark themes dynamically.

---

## How to Run
1. **Setup Requirements**:
   - JDK 11+.
   - JavaFX library configured.
   - MySQL database with proper credentials.
2. **Build & Run**:
   - Compile using your preferred IDE or command-line tools.
   - Launch the `MainApplication` class.

---

## Development Notes
- **Dependencies**: JavaFX, MySQL Connector, logging utilities.
- **Database Connectivity**:
  - Handled via `DbConnectivityClass`.
  - Ensure database schema is properly set up before running the application.
- **Logging**: Uses `MyLogger` for logging critical events.

---

## Future Enhancements
- Add password hashing for secure storage.
- Implement better error handling mechanisms.
- Extend user roles and permissions.

---

Feel free to update this ReadMe with new features or modifications!
