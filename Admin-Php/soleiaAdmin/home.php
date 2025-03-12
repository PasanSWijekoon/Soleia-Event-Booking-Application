<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Admin Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
      rel="stylesheet"
    />
    <link rel="icon"  href="assets/images/logo.png">
    <script type="module">
      // Firebase imports and config (preserved from original)
      import { initializeApp } from "https://www.gstatic.com/firebasejs/9.0.0/firebase-app.js";
      import { getAuth } from "https://www.gstatic.com/firebasejs/9.0.0/firebase-auth.js";


      const app = initializeApp(firebaseConfig);
      const auth = getAuth(app);

      const user = JSON.parse(sessionStorage.getItem("user"));
      if (!user) {
        window.location.href = "index.php";
      }
    </script>
  </head>
  <body class="bg-gray-900 text-gray-100 min-h-screen">
    <!-- Sidebar -->
    <div class="fixed inset-y-0 left-0 w-64 bg-gray-800">
      <div class="flex items-center justify-center h-16 bg-gray-700">
        <h1 class="text-xl font-bold">Admin Dashboard</h1>
      </div>
      <nav class="mt-8">
        <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="users.php">
            <i class="fas fa-users mr-3"></i>
            Users
        </a>
        <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="events.php">
            <i class="fas fa-calendar mr-3"></i>
            Events
        </a>
        <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="payments.php">
            <i class="fas fa-credit-card mr-3"></i>
            Payments
        </a>
        <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="categories.php">
            <i class="fas fa-tags mr-3"></i>
            Categories
        </a>
    </nav>
    </div>

    <!-- Main Content -->
    <div class="ml-64 p-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <div>
          <h2 class="text-2xl font-semibold" id="welcome-message">
            Welcome back
          </h2>
          <p class="text-gray-400">Here's what's happening today</p>
        </div>
        <div class="flex items-center">
          <span class="mr-4" id="user-email"></span>
          <button
            onclick="window.location.href='index.php'"
            class="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded"
          >
            Sign Out
          </button>
        </div>
      </div>

      <!-- Dashboard Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Users Card -->
        <div class="bg-gray-800 p-6 rounded-lg">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">Total Users</h3>
            <i class="fas fa-users text-blue-500"></i>
          </div>
          <p class="text-3xl font-bold mt-4">1,234</p>
          <p class="text-gray-400 text-sm mt-2">+12% from last month</p>
        </div>

        <!-- Events Card -->
        <div class="bg-gray-800 p-6 rounded-lg">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">Active Events</h3>
            <i class="fas fa-calendar text-green-500"></i>
          </div>
          <p class="text-3xl font-bold mt-4">56</p>
          <p class="text-gray-400 text-sm mt-2">Next event in 2 days</p>
        </div>

        <!-- Payments Card -->
        <div class="bg-gray-800 p-6 rounded-lg">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">Revenue</h3>
            <i class="fas fa-credit-card text-yellow-500"></i>
          </div>
          <p class="text-3xl font-bold mt-4">Rs 45,678</p>
          <p class="text-gray-400 text-sm mt-2">+8% from last month</p>
        </div>

        <!-- Categories Card -->
        <div class="bg-gray-800 p-6 rounded-lg">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold">Categories</h3>
            <i class="fas fa-tags text-purple-500"></i>
          </div>
          <p class="text-3xl font-bold mt-4">24</p>
          <p class="text-gray-400 text-sm mt-2">3 new this month</p>
        </div>
      </div>

      <!-- Recent Activity Table -->
      <div class="mt-8 bg-gray-800 rounded-lg p-6">
        <h3 class="text-xl font-semibold mb-4">Recent Activity</h3>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="text-left text-gray-400 border-b border-gray-700">
                <th class="pb-3">Type</th>
                <th class="pb-3">Description</th>
                <th class="pb-3">Date</th>
                <th class="pb-3">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr class="border-b border-gray-700">
                <td class="py-3">User</td>
                <td class="py-3">New user registration</td>
                <td class="py-3">2 minutes ago</td>
                <td class="py-3">
                  <span
                    class="bg-green-500 text-green-900 px-2 py-1 rounded text-sm"
                    >Complete</span
                  >
                </td>
              </tr>
              <tr class="border-b border-gray-700">
                <td class="py-3">Payment</td>
                <td class="py-3">New payment received</td>
                <td class="py-3">1 hour ago</td>
                <td class="py-3">
                  <span
                    class="bg-blue-500 text-blue-900 px-2 py-1 rounded text-sm"
                    >Processing</span
                  >
                </td>
              </tr>
              <tr>
                <td class="py-3">Event</td>
                <td class="py-3">Event updated</td>
                <td class="py-3">3 hours ago</td>
                <td class="py-3">
                  <span
                    class="bg-yellow-500 text-yellow-900 px-2 py-1 rounded text-sm"
                    >Pending</span
                  >
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <script>
      // Update user info
      const user = JSON.parse(sessionStorage.getItem("user"));
      if (user) {
        document.getElementById(
          "welcome-message"
        ).textContent = `Welcome back, ${user.displayName || "Admin"}`;
        document.getElementById("user-email").textContent = user.email;
      }
    </script>
  </body>
</html>
