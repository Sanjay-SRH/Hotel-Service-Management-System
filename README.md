
# Service Request Management System – Full Technical Documentation

## Team: Intellisix  
### Members  
Suhasini Madala  
K Y S Yashwanth  
Alekhya Vemulapalli  
Rishi Raj Nalla  
Sanjay Kumar Tamminani  
Chandala Tharun Sai Pranav  


# 1. Project Overview

The Service Request Management System is a complete backend built using **Java**, **Spring Boot**, and **custom JSON persistence**.  
The system allows customers to raise service requests, staff to complete them, admins to manage them, and everyone to receive relevant notifications.

This README covers:

- Full file explanations  
- Business logic and security rules  
- Complete workflow  
- Full API documentation (every endpoint + body + restriction)  
- Role-based access rules  
- Multi-user (multi-client, multi-staff) behavior  
- JSON storage format  
- Team contributions  


# 2. Folder Structure and File Responsibilities

## A. Controller Layer (Handles All API Endpoints)

### 1. AuthRestController  
Handles login authentication for Admin, Staff, and Customer.  
- Validates username, password, and correct role.  
- Returns profile details on success.  

### 2. UserRestController  
Provides registration endpoints for:  
- Customer  
- Staff  
- Admin  

Uses `UserService` to persist users into JSON.

### 3. ServiceRequestRestController  
Handles the entire request workflow:
- Create request  
- Get requests  
- Assign staff  
- Update status  
- Cancel request  

Calls `ServiceRequestService` for business logic.

### 4. NotificationRestController  
Endpoints to:
- Send notifications  
- View notifications  
- Mark notifications as read  

Backed by `NotificationService`.

### 5. ReviewRestController  
Allows customers to submit reviews & ratings.  
Admins can fetch all reviews.

### 6. AdminStaffPerformanceController  
Calculates staff performance using:
- Completed requests count  
- Reviewed completed requests  
- Average rating  

Uses `ReviewService`.

### 7. DashboardController  
Provides:
- Health check  
- Summary (pending, assigned, completed, etc.)  
- Unread notifications count  

## B. Service Layer (Business Logic)

### 1. UserService  
- Registers new staff/customers/admins  
- Authenticates login  
- Updates user information  
- Retrieves room numbers for request ID generation  

### 2. ServiceRequestService  
Handles everything related to service requests:
- Create  
- Assign staff  
- Update status  
- Cancel  
- Get by client/staff  

Ensures workload limit (max 5 active at a time).

### 3. ReviewService  
- Submits reviews  
- Ensures request is completed before reviewing  
- Computes staff performance metrics  

### 4. NotificationService  
- Creates notifications  
- Fetch unread/all notifications  
- Marks notifications read  

### 5. CustomUserDetailsService  
Used by Spring Security to authenticate users from JSON.

### 6. RequestIdUtil  
Generates ID format:  
```
<SERVICE TYPE>-<ROOM NUMBER>-<RANDOM>
Example: RE-101-345
```

## C. Model Layer (Data Structures)

### UserAccount (abstract)
Fields:
- id  
- username  
- password  
- name  
- role  
- createdAt  

Inherited by:
- Client  
- Staff  
- Admin  

### Client  
Adds:
- roomNumber  

### Staff  
Adds:
- availability  
- performanceRating  

### Admin  
Adds:
- department  

### ServiceRequest  
Contains:
- clientId  
- serviceType  
- description  
- priority  
- assignedStaffId  
- status  
- timestamps  

### Notification  
Fields:
- recipientId  
- message  
- read  

### Review  
Fields:
- requestId  
- rating  
- message  

### Role (Enum)  
Values:
- Admin  
- Staff  
- Customer  


## D. Storage Layer (JSON Persistence)

### Authentication  
Reads/writes:
- users.json  
- clients.json  
- staff.json  
- admins.json  

Functions:
- findByUsername  
- findById  
- persistUser  
- updateUser  

### ServiceRequestRepository  
Manages:
- service_requests.json  

Provides:
- save  
- update  
- findById  
- findAll  
- findByClient  
- findByStaff  

### NotificationRepository  
Reads/writes:
- notifications.json  

Provides:
- save  
- update  
- findAll  
- findByRecipient  

### Review JSON management  
Handled within Review model itself.



# 3. Full System Workflow (Multi-Customer, Multi-Staff)

## Step 1: Customer Registration
Multiple customers can register with unique usernames.  
Each customer gets a unique clientId: `C001`, `C002`, …

## Step 2: Staff Registration
Admin registers staff.  
Each staff gets an ID: `S001`, `S002`, …

## Step 3: Login
Each user logs in with role validation.

## Step 4: Customer Creates Request
Each request gets a unique ID based on:
- Service type  
- Room number  
- Random component  

Example:  
`RE-101-389`

## Step 5: Admin Assigns Staff
Rules enforced:
- Staff cannot exceed **5 active requests**.  
- Customer A cannot view B’s requests.  
- Staff X cannot view requests assigned to Staff Y.  

## Step 6: Staff Updates Progress
Statuses allowed:
- PENDING  
- ASSIGNED  
- IN_PROGRESS  
- COMPLETED  
- CANCELLED  

## Step 7: Notifications
Notifications automatically sent to:
- Client  
- Staff  
- Admin  

## Step 8: Review
Customer can review **only completed** requests.

## Step 9: Staff Performance Dashboard
Admin can view performance using:
- Completed count  
- Reviewed count  
- Average rating  


# 4. Access Control Rules 

### Customers:
- Can only view **their own** requests.  
- Cannot view or update other customers' requests.

### Staff:
- Can only view **requests assigned to them**.  
- Cannot update requests assigned to others.

### Admin:
- Full access to all requests.  

These rules are enforced by:
- ServiceRequestService  
- UserService  
- Role-based validation in controllers  


# 5. Service Request Management System – API Documentation

# **1. AUTHENTICATION APIs**

## **Register Customer**
**POST** `/api/users/customer`

```json
{
  "name": "John Doe",
  "username": "john123",
  "password": "pass123",
  "roomNumber": "101"
}
```

---

## **Register Staff**
**POST** `/api/users/staff`

```json
{
  "username": "staff01",
  "password": "pass123",
  "name": "Ravi"
}
```


## **Login**
**POST** `/api/auth/login`

```json
{
  "username": "john123",
  "password": "pass123",
  "role": "Customer"
}
```


#  **2. SERVICE REQUEST APIs**

## **Create Request**
**POST** `/api/requests`

```json
{
  "clientId": "C001",
  "serviceType": "REPAIR",
  "description": "Leakage in bathroom",
  "priority": "HIGH"
}
```

## **Get All Requests**
**GET** `/api/requests/all`

Access: **Admin only**


## **Get Request by ID**
**GET** `/api/requests/{id}`

Rules:
- Customer: only their own requests  
- Staff: only assigned requests  
- Admin: full access  


## **Get Requests by Client**
**GET** `/api/requests/client/{clientId}`

Rule: Customer can only access their own clientId.


## **Get Requests by Staff**
**GET** `/api/requests/staff/{staffId}`

Rule: Staff can only view assigned requests.


## **Assign Staff**
**PUT** `/api/requests/{id}/assign-staff`

```json
{
  "staffId": "S001"
}
```

Rule: Admin only.


## **Update Status**
**PUT** `/api/requests/{id}/status`

```json
{
  "newStatus": "IN_PROGRESS",
  "notes": "Work started on the issue",
  "actorId": "S001"
}
```


## **Cancel Request**
**POST** `/api/requests/{id}/cancel`

Rule: Only the customer may cancel their own request.


#  **3. NOTIFICATION APIs**

## **Send Notification**
**POST** `/api/notifications`

```json
{
  "recipientId": "C001",
  "message": "Your request is now being processed."
}
```


## **Get All Notifications**
**GET** `/api/notifications/user/{userId}`


## **Get Unread Notifications**
**GET** `/api/notifications/user/{userId}/unread`

## **Mark All as Read**
**POST** `/api/notifications/user/{userId}/mark-all-read`

#  **4. REVIEW APIs**

## **Submit Review**
**POST** `/api/reviews`

```json
{
  "requestId": "REQ001",
  "rating": "5",
  "message": "Great service!"
}
```


## **Get All Reviews**
**GET** `/api/reviews`


#  **5. ADMIN APIs**

## **Get Staff Performance**
**GET** `/api/admin/staff/{staffId}/performance`

```json
{
  "staffId": "S001",
  "totalCompletedRequests": 10,
  "reviewedCompletedRequests": 8,
  "averageRating": 4.6
}
```


#  **6. DASHBOARD APIs**

## **Summary**
**GET** `/api/dashboard/summary`

## **Unread Notification Count**
**GET** `/api/dashboard/unread-notifications/{userId}`

## **Health Check**
**GET** `/api/dashboard/health`



#  **7. Sample IDs**

- Customer: `C001`, `C002`  
- Staff: `S001`, `S002`  
- Request: auto-generated like `RE-101-345`


# 6. JSON Storage Files

users.json  
staff.json  
clients.json  
admins.json  
service_requests.json  
reviews.json  
notifications.json  


# 7. Architecture Summary

Controllers → Services → Repositories → File Persistence → JSON Storage

# 8. Team Contributions

## Yashwanth  
AdminStaffPerformanceController  
ReviewRestController  
Review DTO  
Review model  
ReviewService  
ServiceRequestRepository  

## Sanjay  
AuthRestController  
NotificationRestController  
LoginRequestDTO  
Notification model  
NotificationService  
Authentication  

## Pranav  
SecurityConfig  
CreateRequestDTO  
ServiceRequest  
ServiceRequestService  
ServiceRequestManagementApplication  

## Rishi Raj  
ServiceRequestRestController  
StaffPerformanceDTO  
UserAccount  
UserService  
ServiceRequestFilePersistence  

## Alekhya  
UserRestController  
RegisterStaffDTO  
Client  
CustomUserDetailsService  
NotificationRepository  

## Suhasini  
RegisterCustomerDTO  
Admin  
Role  
Staff  
RequestIdUtil  
NotificationFilePersistence  

