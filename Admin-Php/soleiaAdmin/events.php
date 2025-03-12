<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Events - Admin Dashboard</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="icon" href="assets/images/logo.png">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Firebase Compat SDK -->
    <script src="https://www.gstatic.com/firebasejs/9.6.10/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/9.6.10/firebase-firestore-compat.js"></script>
</head>
<body class="bg-gray-900 text-gray-100 min-h-screen">
    <!-- Sidebar -->
    <div class="fixed inset-y-0 left-0 w-64 bg-gray-800">
        <div class="flex items-center justify-center h-16 bg-gray-700">
            <h1 class="text-xl font-bold">Admin Dashboard</h1>
        </div>
        <nav class="mt-8">
            <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="users.php">
                <i class="fas fa-users mr-3"></i> Users
            </a>
            <a class="flex items-center px-6 py-3 bg-gray-700 text-white" href="events.php">
                <i class="fas fa-calendar mr-3"></i> Events
            </a>
            <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="payments.php">
                <i class="fas fa-credit-card mr-3"></i> Payments
            </a>
            <a class="flex items-center px-6 py-3 text-gray-300 hover:bg-gray-700 hover:text-white" href="categories.php">
                <i class="fas fa-tags mr-3"></i> Categories
            </a>
        </nav>
    </div>

    <!-- Main Content -->
    <div class="ml-64 p-8">
        <div class="flex justify-between items-center mb-8">
            <div>
                <h2 class="text-2xl font-semibold">Events Management</h2>
                <p class="text-gray-400">Manage your events and schedules</p>
            </div>
            <button class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded">
                Create New Event
            </button>
        </div>

        <div class="bg-gray-800 rounded-lg p-6 mb-8">
            <div class="grid grid-cols-7 gap-2">
                <div class="text-center text-gray-400">Sun</div>
                <div class="text-center text-gray-400">Mon</div>
                <div class="text-center text-gray-400">Tue</div>
                <div class="text-center text-gray-400">Wed</div>
                <div class="text-center text-gray-400">Thu</div>
                <div class="text-center text-gray-400">Fri</div>
                <div class="text-center text-gray-400">Sat</div>
                <div class="bg-gray-700 p-2 rounded">1</div>
                <div class="bg-gray-700 p-2 rounded">2</div>
                <div class="bg-blue-600 p-2 rounded">3 
                    <div class="text-xs">2 Events</div>
                </div>
            </div>
        </div>

        <div class="bg-gray-800 rounded-lg p-6">
            <div class="flex justify-between items-center mb-4">
                <div class="relative">
                    <input type="text" placeholder="Search events..." class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                </div>
                <div class="flex space-x-2">
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Filter by Type</option>
                        <option>Workshop</option>
                        <option>Conference</option>
                        <option>Webinar</option>
                    </select>
                    <select class="bg-gray-700 text-white px-4 py-2 rounded-lg">
                        <option>Sort by Date</option>
                        <option>Name</option>
                        <option>Capacity</option>
                        <option>Status</option>
                    </select>
                </div>
            </div>
            <div class="overflow-y-auto max-h-96">
                <table class="w-full">
                    <thead>
                        <tr class="text-left text-gray-400 border-b border-gray-700">
                            <th class="pb-3">Image</th>
                            <th class="pb-3">Event Name</th>
                            <th class="pb-3">Category</th>
                            <th class="pb-3">Venue Location</th>
                            <th class="pb-3">Ticket Price</th>
                            <th class="pb-3">Available Tickets</th>
                            <th class="pb-3">Attendees Count</th>
                            <th class="pb-3">Organizer ID</th>
                            <th class="pb-3">Event Date</th>
                            <th class="pb-3">Created At</th>
                            <th class="pb-3">Status</th>
                            <th class="pb-3">Actions</th>
                        </tr>
                    </thead>
                    <tbody id="eventsTableBody">
                        <!-- Dynamic Events will be injected here -->
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script>
        // Firebase configuration
    

        // Initialize Firebase
        firebase.initializeApp(firebaseConfig);
        const db = firebase.firestore();

        function sendFCMNotification(organizerId, eventTitle) {
        fetch('http://192.168.1.5/SoleiaAdmin/send_fcm_v1.php', { // Replace with your server URL
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                organizerId: organizerId,
                eventTitle: eventTitle
            })
        })
        .then(response => response.json())
        .then(data => console.log("FCM Response:", data))
        .catch(error => console.error("Error sending FCM:", error));
    }

        function parseFirestoreDate(dateInput) {
            if (typeof dateInput === "object" && dateInput.seconds) {
                return new Date(dateInput.seconds * 1000);
            } else if (typeof dateInput === "string") {
                return new Date(dateInput.replace(" at ", " "));
            } else {
                return new Date();
            }
        }

        function loadEvents() {
            db.collection("Events").get().then((querySnapshot) => {
                const eventsTableBody = document.getElementById("eventsTableBody");
                eventsTableBody.innerHTML = "";
                querySnapshot.forEach((doc) => {
                    const event = doc.data();
                    const eventId = doc.id;

                    const eventDate = parseFirestoreDate(event.event_date);
                    const createdAt = parseFirestoreDate(event.created_at);

                    const eventDateFormatted = eventDate.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
                    const createdAtFormatted = createdAt.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });

                    const venueLocation = event.venue && event.venue.latitude && event.venue.longitude 
                        ? `${event.venue.latitude.toFixed(5)}°, ${event.venue.longitude.toFixed(5)}°` 
                        : 'N/A';

                    const row = document.createElement("tr");
                    row.className = "border-b border-gray-700";
                    row.dataset.id = eventId;
                    row.dataset.organizerId = event.organizer_id; // Store organizerId in row
                    row.innerHTML = `
                        <td class="py-3">
                            <img src="${event.image_url}" alt="${event.title}" class="w-12 h-12 rounded-full object-cover mx-auto" />
                        </td>
                        <td class="py-3">${event.title}</td>
                        <td class="py-3">${event.category}</td>
                        <td class="py-3">${venueLocation}</td>
                        <td class="py-3">${event.ticket_price}</td>
                        <td class="py-3">${event.available_tickets}</td>
                        <td class="py-3">${event.attendees_count}</td>
                        <td class="py-3">${event.organizer_id}</td>
                        <td class="py-3">${eventDateFormatted}</td>
                        <td class="py-3">${createdAtFormatted}</td>
                        <td class="py-3">
                            <span class="status-badge ${event.status === 'approved' ? 'bg-green-500 text-green-900' : 'bg-yellow-500 text-yellow-900'} px-2 py-1 rounded text-sm cursor-pointer">
                                ${event.status}
                            </span>
                        </td>
                        <td class="py-3">
                            <button class="text-blue-500 hover:text-blue-600 mr-2"><i class="fas fa-edit"></i></button>
                            <button class="text-red-500 hover:text-red-600"><i class="fas fa-trash"></i></button>
                        </td>
                    `;
                    eventsTableBody.appendChild(row);
                });

                attachStatusClickHandlers();
            }).catch((error) => {
                console.error("Error fetching events: ", error);
            });
        }

        function attachStatusClickHandlers() {
            const statusBadges = document.querySelectorAll(".status-badge");
            statusBadges.forEach((badge) => {
                badge.addEventListener("click", function () {
                    const row = badge.closest("tr");
                    const eventId = row.dataset.id;
                    const organizerId = row.dataset.organizerId;
                    const eventTitle = row.cells[1].textContent;

                    db.collection("Events").doc(eventId).update({
                        status: "approved"
                    }).then(() => {
                        badge.textContent = "approved";
                        badge.classList.remove("bg-yellow-500", "text-yellow-900");
                        badge.classList.add("bg-green-500", "text-green-900");
                        sendFCMNotification(organizerId, eventTitle);
                    }).catch((error) => {
                        console.error("Error updating status:", error);
                    });
                });
            });
        }

        window.onload = loadEvents;
    </script>
</body>
</html>