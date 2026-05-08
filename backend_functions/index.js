const functions = require('firebase-functions');
const admin = require('firebase-admin');
const axios = require('axios');

admin.initializeApp();
const db = admin.firestore();

// Replace with your Safaricom Daraja Credentials
const CONSUMER_KEY = "YOUR_CONSUMER_KEY";
const CONSUMER_SECRET = "YOUR_CONSUMER_SECRET";
const SHORTCODE = "174379"; // Sandbox Paybill
const PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"; // Sandbox Passkey

// This function gets called by the Android app via Retrofit
exports.triggerStkPush = functions.https.onRequest(async (req, res) => {
    try {
        const { phoneNumber, amount, userId, itemsJson } = req.body;
        
        // 1. Get OAuth Token from Safaricom
        const auth = Buffer.from(`${CONSUMER_KEY}:${CONSUMER_SECRET}`).toString('base64');
        const tokenResponse = await axios.get("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials", {
            headers: { Authorization: `Basic ${auth}` }
        });
        const accessToken = tokenResponse.data.access_token;

        // 2. Prepare STK Push Payload
        const timestamp = new Date().toISOString().replace(/[^0-9]/g, '').slice(0, -3);
        const password = Buffer.from(`${SHORTCODE}${PASSKEY}${timestamp}`).toString('base64');

        const pushData = {
            BusinessShortCode: SHORTCODE,
            Password: password,
            Timestamp: timestamp,
            TransactionType: "CustomerPayBillOnline",
            Amount: amount,
            PartyA: phoneNumber,
            PartyB: SHORTCODE,
            PhoneNumber: phoneNumber,
            CallBackURL: "https://us-central1-YOUR-PROJECT-ID.cloudfunctions.net/mpesaCallback", // Replace with your deployed function URL
            AccountReference: "Pete Jewels",
            TransactionDesc: "Jewelry Purchase"
        };

        // 3. Send STK Push Request
        const pushResponse = await axios.post("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest", pushData, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });

        const checkoutRequestId = pushResponse.data.CheckoutRequestID;
        
        // 4. Save "PENDING" order in Firestore using the CheckoutRequestID as the document ID
        await db.collection('orders').doc(checkoutRequestId).set({
            userId: userId,
            amount: amount,
            phoneNumber: phoneNumber,
            itemsJson: itemsJson,
            status: "PENDING",
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        return res.status(200).json({
            success: true,
            orderId: checkoutRequestId,
            message: "STK Push Initiated Successfully"
        });

    } catch (error) {
        console.error("STK Push Error:", error);
        return res.status(500).json({ success: false, message: error.message });
    }
});

// This function is the Webhook that Safaricom calls
exports.mpesaCallback = functions.https.onRequest(async (req, res) => {
    try {
        const callbackData = req.body.Body.stkCallback;
        const checkoutRequestId = callbackData.CheckoutRequestID;
        const resultCode = callbackData.ResultCode;

        // 0 means success in Daraja API
        const status = resultCode === 0 ? "SUCCESS" : "FAILED";

        // Update Firestore Document
        await db.collection('orders').doc(checkoutRequestId).update({
            status: status,
            callbackData: callbackData,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        // Safaricom expects a success response
        return res.status(200).send("Success");
    } catch (error) {
        console.error("Callback Error:", error);
        return res.status(500).send("Error");
    }
});
