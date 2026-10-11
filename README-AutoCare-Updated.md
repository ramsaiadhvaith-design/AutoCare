# AutoCare – updated project notes

## Technology
- Frontend: React + Vite
- Backend: Spring Boot (Java)
- Database: MySQL

## Start the backend (Windows PowerShell)
Open a terminal:

```powershell
cd .\autocare-backend\autocare-backend
$env:MAVEN_HOME = "C:\tools\apache-maven-3.9.16"
$env:Path = "$env:MAVEN_HOME\bin;$env:Path"
mvn spring-boot:run
```

Keep the backend terminal open. Confirm your local MySQL server is running and the database settings in `src/main/resources/application.properties` match your machine. Do not publish that file if it contains your database password.

## Start the frontend
Open a second terminal:

```powershell
cd .\autocare-frontend
npm install
npm run dev
```

Open the local URL printed by Vite (normally `http://localhost:5173`).

## Authentication added
- Registration and login call the Spring Boot API and store account records in MySQL.
- Passwords are stored as salted PBKDF2 hashes, not as plain text.
- Successful login creates a temporary bearer session token that expires after 12 hours.
- Vehicle and booking API routes require that token; logout revokes it.
- If the backend is restarted, existing in-memory sessions are cleared, so log in again.

## Suggested test order
1. Register with a name, valid email, and password of at least 8 characters.
2. Log out, then log in with the same email and password.
3. Try registering the same email again; it should show a helpful message.
4. Add a vehicle, refresh the page, and confirm it remains listed.
5. Create a service booking, change its status, and delete a booking.
6. Log out and confirm the home page no longer shows the signed-in account.

## Important limitation
Vehicle and booking records are shared across signed-in accounts in this project version; records are not yet filtered by account owner. This is suitable for a local class demonstration with test data, but user-specific data ownership should be added before real-world deployment.

## Role-based access (Customer and Mechanic)

The application now supports two backend-enforced roles:

- **CUSTOMER**: can view/manage vehicle records and create/view service bookings. Customers cannot change a booking's status or delete bookings.
- **MECHANIC**: can view vehicles and service bookings, update booking statuses, and delete bookings. Mechanics cannot create bookings or edit/delete vehicle records.

New accounts are created as `CUSTOMER` by default. For security, the public registration form does not let users promote themselves to mechanic. To create a mechanic account for a project demo:

1. Register a separate account through AutoCare (use an email you can identify as the mechanic account).
2. Open MySQL Workbench and connect to the same local MySQL server/database (`autocare_db`) used by the application.
3. Run the following SQL, replacing the email with the exact email used for that account:

```sql
UPDATE user_accounts
SET role = 'MECHANIC'
WHERE email = 'mechanic@example.com';

UPDATE user_accounts
SET role = 'CUSTOMER'
WHERE role IS NULL OR role = '';
```

4. Log out of AutoCare, then log in again as that account. Roles are included in the login session, so an already-active session will not change roles automatically.
5. The mechanic account should show a **Service Desk** button. It can update booking statuses and delete bookings. Customer accounts should see the booking form but not the status-edit/delete controls.

If MySQL Workbench reports that `user_accounts` does not exist, start the Spring Boot backend once and log in/register so Hibernate can create/update the table, then run the SQL again.

**Important scope note:** This is action-level role-based access control. The current database schema does not yet assign each vehicle or booking to a specific customer, so records are shared across accounts. Do not describe this version as providing private per-customer data isolation. In a production deployment, add owner relationships, stricter record-level authorization, HTTPS, and durable server-side sessions.

## Starting the app on Windows

Backend terminal:

```powershell
cd .\AutoCare\autocare-backend\autocare-backend
$env:MAVEN_HOME = "C:\tools\apache-maven-3.9.16"
$env:Path = "$env:MAVEN_HOME\bin;$env:Path"
mvn spring-boot:run
```

Frontend terminal:

```powershell
cd .\AutoCare\autocare-frontend
npm install
npm run dev
```
