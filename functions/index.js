const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Firebase Admin SDK
admin.initializeApp();

/**
 * 🔔 Function: Triggered whenever a new announcement is added
 * to the "announcements" collection in Firestore.
 * Sends a push notification to all devices subscribed to the "announcements" topic.
 */
exports.notifyOnNewAnnouncement = functions.firestore
    .document("announcements/{announcementId}")
    .onCreate(async (snapshot, context) => {
        const data = snapshot.data();

        // Prepare notification payload
        const payload = {
            notification: {
                title: data.title || "New Announcement",
                body: data.message || "Tap to view details.",
                sound: "default",
            },
            data: {
                title: data.title || "New Announcement",
                message: data.message || "",
            }
        };

        try {
            // Send to all subscribers of topic 'announcements'
            const response = await admin.messaging().sendToTopic("announcements", payload);
            console.log("✅ Notification sent successfully:", response);
            return response;
        } catch (error) {
            console.error("❌ Error sending notification:", error);
            return null;
        }
    });
