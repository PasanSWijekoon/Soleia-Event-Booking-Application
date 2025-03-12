<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Payments - Admin Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="icon"  href="assets/images/logo.png">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
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
            <a class="flex items-center px-6 py-3 bg-gray-700 text-white" href="payments.php">
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
                <h2 class="text-2xl font-semibold">Payments Management</h2>
                <p class="text-gray-400">Track and manage all transactions</p>
            </div>
            <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded">
                Export Report
            </button>
        </div>

        <!-- Payment Stats -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div class="bg-gray-800 p-6 rounded-lg">
                <h3 class="text-gray-400">Total Revenue</h3>
                <p class="text-3xl font-bold mt-2">$45,678</p>
                <p class="text-green-500 text-sm mt-2">+8% from last month</p>
            </div>
            <div class="bg-gray-800 p-6 rounded-lg">
                <h3 class="text-gray-400">Pending Payments</h3>
                <p class="text-3xl font-bold mt-2">23</p>
                <p class="text-yellow-500 text-sm mt-2">5 require action</p>
            </div>
            <div class="bg-gray-800 p-6 rounded-lg">
                <h3 class="text-gray-400">Failed Transactions</h3>
                <p class="text-3xl font-bold mt-2">3</p>
                <p class="text-red-500 text-sm mt-2">-2 from last month</p>
            </div>
        </div>

        <!-- Transactions Table -->
        <div class="bg-gray-800 rounded-lg p-6">
            <div class="flex justify-between items-center mb-4">
                <div class="relative">
                    <input type="text" placeholder="Search transactions..." class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                </div>
                <div class="flex space-x-2">
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Payment Status</option>
                        <option>Completed</option>
                        <option>Pending</option>
                        <option>Failed</option>
                    </select>
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Payment Method</option>
                        <option>Credit Card</option>
                        <option>PayPal</option>
                        <option>Bank Transfer</option>
                    </select>
                </div>
            </div>
            <table class="w-full">
                <thead>
                    <tr class="text-left text-gray-400 border-b border-gray-700">
                        <th class="pb-3">Transaction ID</th>
                        <th class="pb-3">Customer</th>
                        <th class="pb-3">Amount</th>
                        <th class="pb-3">Date</th>
                        <th class="pb-3">Status</th>
                        <th class="pb-3">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="border-b border-gray-700">
                        <td class="py-3">#TRX-789</td>
                        <td class="py-3">John Doe</td>
                        <td class="py-3">$299.99</td>
                        <td class="py-3">2025-02-21</td>
                        <td class="py-3"><span class="bg-green-500 text-green-900 px-2 py-1 rounded text-sm">Completed</span></td>
                        <td class="py-3">
                            <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-eye"></i></button>
                            <button class="text-gray-500 hover:text-gray-400"><i class="fas fa-download"></i></button>
                        </td>
                    </tr>
                    <tr class="border-b border-gray-700">
                        <td class="py-3">#TRX-790</td>
                        <td class="py-3">Jane Smith</td>
                        <td class="py-3">$199.99</td>
                        <td class="py-3">2025-02-21</td>
                        <td class="py-3"><span class="bg-yellow-500 text-yellow-900 px-2 py-1 rounded text-sm">Pending</span></td>
                        <td class="py-3">
                            <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-eye"></i></button>
                            <button class="text-gray-500 hover:text-gray-400"><i class="fas fa-download"></i></button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>