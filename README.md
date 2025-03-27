# Carpet Cleaning Service App

Your *Carpet Cleaning Service App* with *Kotlin + Jetpack Compose* is shaping up to be a full-fledged service app! 🚀  

---

### *Updated Features & Implementation Plan*

#### *1. Authentication (Login & Signup)*  
- Implement *Firebase Authentication* (Google Sign-In, Email & Password).  
- Store user details (name, email, phone, profile picture) in *Firestore*.  
- Allow *profile photo upload* using Firebase Storage.  

#### *2. User Profile Section*  
- Display user details (name, email, phone).  
- Allow users to *edit profile details*.  
- *Change profile picture* feature.  

#### *3. Dashboard (Home Screen)*  
- *Welcome message* with the user’s name.  
- Display *past & upcoming bookings*.  
- “*Book a Cleaner*” button.  

#### *4. Booking a Cleaner*  
- *Step 1: Enter carpet dimensions (length & width)*.  
- *Step 2*: Calculate estimated price using a formula.  
- *Step 3: Select **date & time* (Date Picker + Time Picker).  
- *Step 4: Enter **location* or use *GPS* for the current location.  
- *Step 5*: Confirm booking (summary screen).  
- *Step 6: Save booking to Firestore & add to **Google Calendar*.  

#### *5. Google Calendar API Integration*  
- *Automatically schedule* the cleaning appointment in Google Calendar.  
- Set *reminders* for upcoming bookings.  

#### *6. Booking Management (CRUD Operations)*  
- *Create*: Users can add a new booking.  
- *Read*: Show all bookings (past/upcoming).  
- *Update*: Modify a scheduled cleaning.  
- *Delete*: Allow cancellation of a booking.  

#### *7. Notifications & Reminders*  
- *Push notifications* for booking confirmations, reminders.  
- Alert users *before their scheduled cleaning*.  

---

### *Tech Stack*  
- ✅ *Kotlin + Jetpack Compose* – UI & State Management  
- ✅ *Firebase Authentication* – Login & Signup  
- ✅ *Firestore Database* – Store user details & bookings  
- ✅ *Firebase Storage* – Store profile pictures  
- ✅ *Google Calendar API* – Add bookings to calendar  
- ✅ *Fused Location API* – Fetch user’s location  
- ✅ *Firebase Cloud Messaging* – Push notifications  

---


---

### *Installation Instructions*  

To get started with the Carpet Cleaning Service app:

1. **Clone this repository**:  
   ```bash
   git clone https://github.com/yourusername/carpetcleaningserviceapp.git
