<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Firebase Auth | Sign In</title>
    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="icon"  href="assets/images/logo.png">
    <!-- Inter Font -->
    <link
      href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
      rel="stylesheet"
    />

    <!-- Firebase Initialization Script -->
    <script type="module">
      // Import Firebase SDKs
      import { initializeApp } from "https://www.gstatic.com/firebasejs/9.0.0/firebase-app.js";
      import {
        getAuth,
        GoogleAuthProvider,
        signInWithPopup,
        signInWithEmailAndPassword,
      } from "https://www.gstatic.com/firebasejs/9.0.0/firebase-auth.js";

      // Your Firebase config
    

      // Initialize Firebase
      const app = initializeApp(firebaseConfig);
      const auth = getAuth(app);

      // Google Sign-In
      const googleSignInBtn = document.getElementById("googleSignInBtn");
      googleSignInBtn.addEventListener("click", async () => {
        const provider = new GoogleAuthProvider();
        try {
          const result = await signInWithPopup(auth, provider);
          const user = result.user;
          console.log("Google Sign-In successful:", user);

          // Store user data in localStorage or sessionStorage to access on the homepage
          sessionStorage.setItem("user", JSON.stringify(user));

          // Redirect to home page
          window.location.href = "home.php"; // Redirect to home page after sign-in
        } catch (error) {
          console.error("Google Sign-In Error:", error.message);
          alert("Error during Google Sign-In");
        }
      });

      // Email and Password Sign-In
      const emailSignInForm = document.getElementById("emailSignInForm");
      emailSignInForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const email = document.getElementById("email").value;
        const password = document.getElementById("password").value;
        try {
          const userCredential = await signInWithEmailAndPassword(
            auth,
            email,
            password
          );
          const user = userCredential.user;
          console.log("Email Sign-In successful:", user);

          // Store user data in sessionStorage
          sessionStorage.setItem("user", JSON.stringify(user));

          // Redirect to home page
          window.location.href = "home.php"; // Redirect to home page after sign-in
        } catch (error) {
          console.error("Email Sign-In Error:", error.message);
          alert("Error during Email Sign-In");
        }
      });
    </script>
  </head>
  <body class="bg-gray-900 flex items-center justify-center min-h-screen font-sans">
    <div class="bg-gray-800 rounded-2xl shadow-2xl p-8 max-w-md w-full mx-4 transition-all duration-300">
        <div class="text-center mb-8">
            <h1 class="text-3xl font-bold text-white mb-2">Welcome Back</h1>
            <p class="text-gray-400">Sign in to continue to your account</p>
        </div>

        <!-- Google Sign In Button -->
        <button id="googleSignInBtn" class="w-full flex items-center justify-center gap-3 bg-gray-700 hover:bg-gray-600 text-white py-3.5 px-4 rounded-xl mb-6 transition-colors duration-300">
            <svg class="w-5 h-5" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24">
                <path fill="currentColor" d="M12.545 10.239v3.821h5.445c-.712 2.315-2.647 3.972-5.445 3.972a5.94 5.94 0 1 1 0-11.88c1.498 0 2.866.549 3.921 1.453l2.814-2.814A9.822 9.822 0 0 0 12.545 2C7.021 2 2.545 6.477 2.545 12s4.476 10 10 10c5.523 0 10-4.477 10-10a9.9 9.9 0 0 0-1.167-4.765l-6.299 6.3Z"/>
            </svg>
            Continue with Google
        </button>

        <div class="flex items-center mb-8">
            <div class="flex-1 border-t border-gray-600"></div>
            <span class="px-4 text-gray-400 text-sm">Or with email</span>
            <div class="flex-1 border-t border-gray-600"></div>
        </div>

        <!-- Email and Password Login Form -->
        <form id="emailSignInForm" class="space-y-6">
            <div>
                <label for="email" class="block text-sm font-medium text-gray-300 mb-2">Email</label>
                <input type="email" id="email" class="w-full bg-gray-700 border border-gray-600 text-white rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all" placeholder="name@company.com" required>
            </div>
            <div>
                <label for="password" class="block text-sm font-medium text-gray-300 mb-2">Password</label>
                <input type="password" id="password" class="w-full bg-gray-700 border border-gray-600 text-white rounded-lg px-4 py-3 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all" placeholder="••••••••" required>
            </div>
            <button type="submit" class="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-3.5 px-4 rounded-lg transition-colors duration-300">
                Sign In
            </button>
        </form>

        <p class="text-center mt-6 text-gray-400">
            Don't have an account? 
            <a href="#" class="text-blue-500 hover:text-blue-400 transition-colors duration-300">Sign up here</a>
        </p>
    </div>
</body>
</html>