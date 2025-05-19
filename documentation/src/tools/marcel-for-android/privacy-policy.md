# Marcel for Android – Privacy Policy

**Marcel for Android** is an open-source application. You can consult its full source code on [GitHub](https://github.com/tambapps/marcel).

This application **does not collect any analytics or personal data** from its users.

---

## SMS Permission

Marcel includes functionality that allows users to [send SMS messages via user-defined scripts](./send-sms.md) within the app.

- **Permission Requested at Runtime**  
  The app requests the `SEND_SMS` permission only if the user explicitly initiates an action to send an SMS via a script. This is handled through Android’s **runtime permission system**, which means **users always choose whether to allow access**, and it’s never requested without direct interaction.

- **Optional Feature**  
  Granting the SMS permission is **completely optional**. Marcel will work normally and provide full access to all other features even if you choose not to grant this permission. Only the SMS-sending feature will be unavailable.

- **Purpose of SMS Usage**  
  The permission is used exclusively to allow user-triggered scripts to send SMS messages. The functionality is visible in the app's interface and is fully under the user's control.

- **No Access to Other Messages or Logs**  
  Marcel **does not access, read, or collect** incoming SMS messages, call logs, or any messages sent outside the app.

- **Local-Only Storage**  
  Any SMS messages sent from within Marcel may be saved **locally** in the app for later reference. These messages **never leave your device** and are **not shared** with anyone.

- **No Background or Hidden Actions**  
  Marcel never sends SMS messages in the background. Every SMS is sent only in response to an explicit user action.

If you do not wish to use the SMS feature, simply ignore or deny the permission request. The rest of the application will remain fully functional.


