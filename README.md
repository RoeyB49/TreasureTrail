
![wellcome_pic](https://github.com/user-attachments/assets/a02e8358-7e13-47d8-a3e2-b4269110504b)

# Lost & Found App

## Overview
Lost & Found is an Android application that helps users post and discover lost and found items. Users can upload details, including images, descriptions, and locations, while others can search by category and connect with users to coordinate item returns. The app is powered by Firebase for authentication and data storage.

## Features
- **User Authentication**
  - Register/Login via email and password
  - Sign in with Google

- **Post Management**
  - Create posts with details, category, location, and images
  - Edit posts after creation
  - Delete posts
  - View personal and public posts

- **Search & Discovery**
  - Browse all user posts
  - Search items by category
  - Connect with users for item recovery

## Data Structure (Firebase)
Each post in Firebase contains:
```json
{
  "postId": "unique_post_id",
  "userId": "user_id",
  "title": "Lost Wallet",
  "category": "Wallet",
  "details": "Brown leather wallet lost near the park.",
  "location": "Central Park, NY",
  "imageUrl": "https://image-url.com/wallet.jpg",
  "timestamp": 1678901234567
}
```

## Tech Stack
- **Frontend:** Android (Kotlin)
- **Backend:** Firebase Firestore (NoSQL Database)
- **Authentication:** Firebase Auth (Email & Google Sign-In)
- **Storage:** Firebase Storage (For images)


