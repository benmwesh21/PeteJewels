const express = require('express');
const axios = require('axios');
const admin = require('firebase-admin');

// 1. Initialize Firebase Admin
// You must download a serviceAccountKey.json from Firebase Console -> Project Settings -> Service Accounts
const serviceAccount = require('./serviceAccountKey.json');
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
const db = admin.firestore();

const app = express();
app.use(express.json());

// Replace with your Safaricom Daraja Credentials
const CONSUMER_KEY = "QYN7RbS4Uiwn8Qat1GDH5iLPuc4q6lHH3ZDg3zZXCgJMNX7J";
const CONSUMER_SECRET = "RjB7aiKLARGijG6wk6XF5esnhBZ1DzNbTBphO38bxpIJBMCVwONaKK1NrO9W2EQn";
const SHORTCODE = "174379"; // Sandbox Paybill
const PASSKEY = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";

// Replace this with the Ngrok URL you get when you run `ngrok http 3000`
const NGROK_URL = "https://simple-darkness-chewing.ngrok-free.dev";

app.post('/api/triggerStkPush', async (req, res) => {
    try {
        const { phoneNumber, amount, userId, itemsJson } = req.body;
        
        // 1. Get OAuth Token
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
            CallBackURL: `${NGROK_URL}/api/mpesaCallback`, // Daraja sends webhook here
            AccountReference: "Pete Jewels",
            TransactionDesc: "Jewelry Purchase"
        };

        // 3. Send STK Push Request
        const pushResponse = await axios.post("https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest", pushData, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });

        const checkoutRequestId = pushResponse.data.CheckoutRequestID;
        
        // 4. Save "PENDING" order in Firestore
        await db.collection('orders').doc(checkoutRequestId).set({
            userId: userId,
            amount: amount,
            phoneNumber: phoneNumber,
            itemsJson: itemsJson,
            status: "PENDING",
            createdAt: admin.firestore.FieldValue.serverTimestamp()
        });

        res.status(200).json({
            success: true,
            orderId: checkoutRequestId,
            message: "STK Push Initiated Successfully"
        });

    } catch (error) {
        console.error("STK Push Error:", error.response ? error.response.data : error.message);
        res.status(500).json({ success: false, message: error.message });
    }
});

// This is the Webhook that Safaricom calls
app.post('/api/mpesaCallback', async (req, res) => {
    try {
        const callbackData = req.body.Body.stkCallback;
        const checkoutRequestId = callbackData.CheckoutRequestID;
        const resultCode = callbackData.ResultCode;

        // 0 means success in Daraja API
        const status = resultCode === 0 ? "SUCCESS" : "FAILED";

        // Update Firestore Document so the Android App can react
        await db.collection('orders').doc(checkoutRequestId).update({
            status: status,
            callbackData: callbackData,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

        res.status(200).send("Success");
    } catch (error) {
        console.error("Callback Error:", error);
        res.status(500).send("Error");
    }
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
