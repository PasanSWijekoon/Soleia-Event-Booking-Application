<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

require 'vendor/autoload.php'; // Use Composer for Google Client Library
use Google\Client;
use Google\Auth\Credentials\ServiceAccountCredentials;

$data = json_decode(file_get_contents("php://input"), true);

if (!isset($data['organizerId']) || !isset($data['eventTitle'])) {
    echo json_encode(["status" => "error", "message" => "Invalid input"]);
    exit;
}

$organizerId = $data['organizerId'];
$eventTitle = $data['eventTitle'];

// Firebase Firestore setup
require_once 'soleia-6c5bb-firebase-adminsdk-fbsvc-62c0eb7458.json'; // Adjust path
use Kreait\Firebase\Factory;

$factory = (new Factory)->withServiceAccount('path/to/soleia-service-account.json');
$firestore = $factory->createFirestore();
$database = $firestore->database();

$docRef = $database->collection('users')->document($organizerId);
$doc = $docRef->snapshot();

if ($doc->exists()) {
    $fcmToken = $doc->data()['fcmToken'];
    if ($fcmToken) {
        // FCM V1 setup
        $client = new Client();
        $client->setAuthConfig('path/to/soleia-service-account.json');
        $client->addScope('https://www.googleapis.com/auth/firebase.messaging');
        $accessToken = $client->fetchAccessTokenWithAssertion()['access_token'];

        $url = "https://fcm.googleapis.com/v1/projects/soleia-6c5bb/messages:send";
        $payload = [
            "message" => [
                "token" => $fcmToken,
                "notification" => [
                    "title" => "Event Approved",
                    "body" => "Your event \"$eventTitle\" has been approved by an admin!"
                ]
            ]
        ];

        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, [
            'Authorization: Bearer ' . $accessToken,
            'Content-Type: application/json'
        ]);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        curl_close($ch);

        if ($httpCode == 200) {
            echo json_encode(["status" => "success", "message" => "Notification sent", "response" => $response]);
        } else {
            echo json_encode(["status" => "error", "message" => "Failed to send notification", "response" => $response]);
        }
    } else {
        echo json_encode(["status" => "error", "message" => "No FCM token found"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Organizer not found"]);
}
?>