<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Users - Admin Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <link rel="icon"  href="assets/images/logo.png">
</head>
<body class="bg-gray-900 text-gray-100 min-h-screen">

    <div class="fixed inset-y-0 left-0 w-64 bg-gray-800">
        <div class="flex items-center justify-center h-16 bg-gray-700">
            <h1 class="text-xl font-bold">Admin Dashboard</h1>
        </div>
        <nav class="mt-8">
            <a class="flex items-center px-6 py-3 bg-gray-700 text-white" href="users.php">
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
                <h2 class="text-2xl font-semibold">Users Management</h2>
                <p class="text-gray-400">Manage your application users</p>
            </div>
            <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded">
                Add New User
            </button>
        </div>

        <!-- Users Table -->
        <div class="bg-gray-800 rounded-lg p-6">
            <div class="flex justify-between items-center mb-4">
                <div class="relative">
                    <input type="text" placeholder="Search users..." class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                </div>
                <div class="flex space-x-2">
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Filter by Role</option>
                        <option>Admin</option>
                        <option>User</option>
                        <option>Editor</option>
                    </select>
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Sort by</option>
                        <option>Name</option>
                        <option>Email</option>
                        <option>Date Joined</option>
                    </select>
                </div>
            </div>
            <div class="overflow-x-auto">
                <table class="w-full">
                    <thead>
                        <tr class="text-left text-gray-400 border-b border-gray-700">
                            <th class="pb-3">Name</th>
                            <th class="pb-3">Email</th>
                            <th class="pb-3">Role</th>
                            <th class="pb-3">Status</th>
                            <th class="pb-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr class="border-b border-gray-700">
                            <td class="py-3">John Doe</td>
                            <td class="py-3">john@example.com</td>
                            <td class="py-3">Admin</td>
                            <td class="py-3"><span class="bg-green-500 text-green-900 px-2 py-1 rounded text-sm">Active</span></td>
                            <td class="py-3">
                                <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-edit"></i></button>
                                <button class="text-red-500 hover:text-red-600"><i class="fas fa-trash"></i></button>
                            </td>
                        </tr>
                        <tr class="border-b border-gray-700">
                            <td class="py-3">Jane Smith</td>
                            <td class="py-3">jane@example.com</td>
                            <td class="py-3">Editor</td>
                            <td class="py-3"><span class="bg-yellow-500 text-yellow-900 px-2 py-1 rounded text-sm">Pending</span></td>
                            <td class="py-3">
                                <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-edit"></i></button>
                                <button class="text-red-500 hover:text-red-600"><i class="fas fa-trash"></i></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <!-- Pagination -->
            <div class="flex justify-between items-center mt-4">
                <p class="text-gray-400">Showing 1-10 of 100 users</p>
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
</body>
</html>