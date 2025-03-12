<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Categories - Admin Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link
      href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"
      rel="stylesheet"
    />
    <link rel="icon" href="assets/images/logo.png" />

    <!-- Firebase SDK v8 (non-modular) -->
    <script src="https://www.gstatic.com/firebasejs/8.10.0/firebase-app.js"></script>
    <script src="https://www.gstatic.com/firebasejs/8.10.0/firebase-firestore.js"></script>
  </head>
  <body class="bg-gray-900 text-gray-100 min-h-screen">

    <div class="fixed inset-y-0 left-0 w-64 bg-gray-800">
      <div class="flex items-center justify-center h-16 bg-gray-700">
        <h1 class="text-xl font-bold">Admin Dashboard</h1>
      </div>
      <nav class="mt-8">
        <a
          class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white"
          href="users.php"
        >
          <i class="fas fa-users mr-3"></i>
          Users
        </a>
        <a
          class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white"
          href="events.php"
        >
          <i class="fas fa-calendar mr-3"></i>
          Events
        </a>
        <a
          class="flex items-center px-6 py-3 bg-gray-700 text-white"
          href="categories.php"
        >
          <i class="fas fa-tags mr-3"></i>
          Categories
        </a>
        <a
          class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white"
          href="payments.php"
        >
          <i class="fas fa-credit-card mr-3"></i>
          Payments
        </a>
      </nav>
    </div>

    <!-- Main Content -->
    <div class="ml-64 p-8">
      <!-- Header -->
      <div class="flex justify-between items-center mb-8">
        <div>
          <h2 class="text-2xl font-semibold">Categories Management</h2>
          <p class="text-gray-400">Manage your application categories</p>
        </div>
        <button
          class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded"
        >
          Add New Category
        </button>
      </div>

      <!-- Categories Table -->
      <div class="bg-gray-800 rounded-lg p-6">
        <div class="flex justify-between items-center mb-4">
          <div class="relative">
            <input
              type="text"
              placeholder="Search categories..."
              class="bg-gray-700 text-white px-4 py-2 rounded-lg"
            />
          </div>
        </div>
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="text-left text-gray-400 border-b border-gray-700">
                <th class="pb-3">Category Name</th>
                <th class="pb-3">Picture</th>
                <th class="pb-3">Status</th>
                <th class="pb-3">Actions</th>
              </tr>
            </thead>
            <tbody>
              <!-- Data will be dynamically loaded here -->
            </tbody>
          </table>
        </div>
        <!-- Pagination -->
        <div class="flex justify-between items-center mt-4">
          <p class="text-gray-400">Showing 1-10 of 50 categories</p>
          <div class="flex space-x-2">
            <button class="bg-gray-700 px-3 py-1 rounded">Previous</button>
            <button class="bg-blue-600 px-3 py-1 rounded">1</button>
            <button class="bg-gray-700 px-3 py-1 rounded">2</button>
            <button class="bg-gray-700 px-3 py-1 rounded">3</button>
            <button class="bg-gray-700 px-3 py-1 rounded">Next</button>
          </div>
        </div>
      </div>
    </div>

    <script>
      // Firebase configuration
    

      // Initialize Firebase
      firebase.initializeApp(firebaseConfig);
      const db = firebase.firestore();

      // Fetch categories from Firestore
      async function loadCategories() {
        const categoriesRef = db.collection("Category"); // Firestore collection name
        const snapshot = await categoriesRef.get();
        const categories = snapshot.docs.map((doc) => doc.data());

        // Dynamically generate rows for each category
        const tableBody = document.querySelector("tbody");
        categories.forEach((category) => {
          const row = document.createElement("tr");
          row.classList.add("border-b", "border-gray-700");

          row.innerHTML = `
                    <td class="py-3">${category.Name}</td>
                    <td class="py-3"><img src="${category.Picture}" alt="${category.Name}" class="w-20 h-20 object-cover rounded-lg"></td>
                    <td class="py-3"><span class="bg-green-500 text-green-900 px-2 py-1 rounded text-sm">Active</span></td>
                    <td class="py-3">
                        <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-edit"></i></button>
                        <button class="text-red-500 hover:text-red-600"><i class="fas fa-trash"></i></button>
                    </td>
                `;

          tableBody.appendChild(row);
        });
      }

      // Load categories when the page loads
      window.onload = loadCategories;
    </script>
  </body>
</html>
