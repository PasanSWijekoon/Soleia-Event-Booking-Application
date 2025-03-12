<?php

require "PHPmail/SMTP.php";
require "PHPmail/PHPMailer.php";
require "PHPmail/Exception.php";

use PHPMailer\PHPMailer\PHPMailer;
use PHPMailer\PHPMailer\Exception;

// Set headers for JSON response
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json");

// Get JSON input
$data = json_decode(file_get_contents("php://input"));

// Validate input
if (isset($data->to_email) && isset($data->event_title)) {
    $to_email = $data->to_email;
    $event_title = $data->event_title;
    $recipient_name = isset($data->recipient_name) ? $data->recipient_name : "Valued Customer";

    $mail = new PHPMailer(true);

    try {
        // Server settings
        $mail->isSMTP();
        $mail->Host = 'smtp.gmail.com';
        $mail->SMTPAuth = true;
        $mail->Username = ''; // Your email
        $mail->Password = 'kkkk'; // App password
        $mail->SMTPSecure = 'ssl';
        $mail->Port = 465;

        // Recipients
        $mail->setFrom('a@gmail.com', 'Soleia Events');
        $mail->addReplyTo('33@gmail.com', 'Soleia Events');
        $mail->addAddress($to_email, $recipient_name);

        // Subject
        $subject = "Your Ticket for {$event_title}";

        // ✅ Dynamic Email Body
        $emailTemplate = "
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                .email-container { max-width: 600px; margin: auto; background: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
                .header { background-color: #020123; color: white; padding: 10px; text-align: center; border-radius: 8px 8px 0 0; }
                .content { padding: 20px; }
                .footer { text-align: center; font-size: 12px; color: #777777; padding: 10px; }
            </style>
        </head>
        <body>
            <div class='email-container'>
                <div class='header'>
                    <h2>Thank You for Your Purchase!</h2>
                </div>
                <div class='content'>
                    <p>Dear {$recipient_name},</p>
                    <p>Thank you for your ticket purchase for <strong>{$event_title}</strong>. We expect you there!</p>
                    <p>We look forward to seeing you at the event.</p>
                </div>
                <div class='footer'>
                    <p>&copy; " . date("Y") . " Soleia Events. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>";

        $mail->isHTML(true);
        $mail->Subject = $subject;
        $mail->Body = $emailTemplate;

        $mail->send();
        echo json_encode(["status" => "success", "message" => "Email sent successfully!"]);
    } catch (Exception $e) {
        echo json_encode(["status" => "error", "message" => "Email sending failed: " . $mail->ErrorInfo]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "Invalid input."]);
}

?>
