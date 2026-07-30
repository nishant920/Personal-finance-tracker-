# 💰 Personal Finance Tracker - Backend API

A high-performance, secure RESTful backend API built with **Spring Boot 3**, **Spring Security 6 (JWT)**, and **PostgreSQL**.

Unlike traditional budget trackers that only record past expenses, this system calculates your **true real-time spendable balance** by reserving funds for upcoming financial commitments before your next income.

🔗 **Frontend Repository**: [Finance Tracker Frontend Repo](https://github.com/nishant920/Finance-tracker-frontend-.git)

---

## 🔑 Demo / Direct Login Credentials

To immediately test the live application without needing email verification, use these pre-seeded demo credentials:

* **E-mail**: `dummy@gmail.com`
* **Password**: `password`

*(Note: The backend automatically seeds this verified account on startup via `DataInitializer`).*

---

## 🎯 Core Product Vision & Formula

### **The Problem**
Checking your bank account balance often gives a false sense of security. Seeing ₹50,000 in your account might tempt you to spend ₹20,000—forgetting that ₹30,000 in EMIs and bills are due before your next paycheck.

### **The Solution: Free-to-Spend Balance Engine**
The backend continuously calculates your **Free-to-Spend Balance** using the core financial formula:

$$\text{Free-to-Spend Balance} = \text{Current Bank Balance} - \sum (\text{PENDING Commitments})$$

* **Current Bank Balance**: Your live total cash / account balance.
* **PENDING Commitments**: Sum of all unpaid financial obligations due before your next income (e.g. Rent, Car EMI, Insurance).

---

## 💡 Key Architectural Logic: Decoupling Commitments & Transactions

A critical concept in this system is that **Marking a Commitment as PAID is intentionally decoupled from recording a Spend Transaction**:

### **1. Why Marking a Commitment as PAID Increases Free-to-Spend Balance**
* **Before Payment (`PENDING`)**:  
  Suppose your Bank Balance is **₹50,000** and you have a Pending EMI of **₹12,000**.  
  $$\text{Free-to-Spend} = ₹50,000 - ₹12,000 = \mathbf{₹38,000}$$  
  *(The system holds back ₹12,000 so you don't accidentally spend your bill money).*

* **When you click "Mark as Paid" (`PAID`)**:  
  The status changes from `PENDING` ➔ `PAID`.  
  Because the bill is paid, **the system no longer needs to hold back that money**.  
  $$\text{Free-to-Spend} = ₹50,000 - ₹0 = \mathbf{₹50,000}$$  
  *Your Free-to-Spend balance increases by ₹12,000 because that money is unlocked!*

### **2. Completing the Full Cycle with a Transaction**
* Marking a commitment as `PAID` updates the *obligation state* (un-reserving the funds).
* When actual cash leaves your bank account, you record a **`SPEND` transaction of ₹12,000**.
* The transaction deducts ₹12,000 from your `currentBalance` ($₹50,000 \rightarrow ₹38,000$).
* **Final Result**:
  $$\text{Free-to-Spend} = ₹38,000 - ₹0 = \mathbf{₹38,000}$$

This two-tier design allows users to manage upcoming obligation schedules independently from raw bank transaction logs.

---

## ✨ Core Features

### 🔑 **1. Authentication & Security**
* **User Registration & Email Verification**: BCrypt password hashing, UUID-based verification tokens with 30-minute expiry, and automated SMTP email dispatch.
* **Resend Verification**: Endpoint to reissue verification tokens for unverified users.
* **Stateless JWT Authentication**: Secure, token-based authentication via Spring Security filter chain (`JwtFilter`).

### 💵 **2. Balance Management**
* **`GET /api/balance/free-to-spend`**: Fetches the authenticated user's real-time Free-to-Spend number.
* **`PUT /api/balance`**: Updates current account balance.

### ⚠️ **3. Pre-Spend Risk Assessment**
* **`POST /api/balance/check-risk`**: Read-only pre-flight check evaluated before executing a spend.
* Calculates whether a proposed spend will eat into committed obligation money.
* Returns a risk flag (`true`/`false`), remaining spendable buffer, and dynamic user-facing warning messages (e.g. *"This spend eats into ₹-3000.00 of your committed money"*).

### 📋 **4. Financial Commitments (Obligations)**
* **`POST /api/commitments`**: Create recurring/one-time commitments (`MONTHLY`, `WEEKLY`, `ONE_TIME`) defaulted to `PENDING`.
* **`GET /api/commitments`**: List user commitments.
* **`PATCH /api/commitments/{id}/pay`**: Transitions status to `PAID`, immediately recalculating Free-to-Spend balance.
* **`DELETE /api/commitments/{id}`**: Delete commitments.

### 💳 **5. Real-Time Monetary Transactions**
* **`POST /api/transactions`**: Record `SPEND` (subtracts balance) or `INCOME` (adds balance) transactions with `@Transactional` database integrity.
* **`GET /api/transactions`**: Retrieve transaction history ordered newest-first.
* **`DELETE /api/transactions/{id}`**: Delete transactions with automatic reversal of balance impact.

---

## 🛠️ Technology Stack

| Component | Technology |
| :--- | :--- |
| **Language & Framework** | Java 17, Spring Boot 3.4.2 |
| **Security** | Spring Security 6, JJWT (JSON Web Token 0.11.5) |
| **Database** | PostgreSQL 15 |
| **ORM / Data Access** | Spring Data JPA, Hibernate |
| **Mail Service** | JavaMailSender (Spring Boot Starter Mail) |
| **Containerization** | Docker, Docker Compose |
| **Build Tool** | Apache Maven |

---

## 📡 REST API Reference Summary

### 🔓 Public Endpoints (No Token Required)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/save` | Register new user account |
| `POST` | `/api/auth/login` | Authenticate user & receive JWT token |
| `GET` | `/api/auth/verify?token={token}` | Verify email address via token |
| `POST` | `/api/auth/resend-verification?email={email}` | Resend verification email |
| `GET` | `/api/health` | Health check endpoint |

### 🔒 Authenticated Endpoints (Header: `Authorization: Bearer <token>`)

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/balance/free-to-spend` | Get current Free-to-Spend balance |
| `PUT` | `/api/balance` | Set / update current bank balance |
| `POST` | `/api/balance/check-risk` | Pre-spend risk evaluation (`{ amount }`) |
| `GET` | `/api/commitments` | Get all commitments for current user |
| `POST` | `/api/commitments` | Add new commitment |
| `PATCH` | `/api/commitments/{id}/pay` | Mark commitment status as `PAID` |
| `DELETE` | `/api/commitments/{id}` | Delete commitment |
| `GET` | `/api/transactions` | Get recent transactions (newest-first) |
| `POST` | `/api/transactions` | Record a new `SPEND` or `INCOME` transaction |
| `DELETE` | `/api/transactions/{id}` | Delete transaction (reverts balance) |

---

## 🚀 Environment Setup & Deployment

### **1. Local `.env` Configuration**
Create a `.env` file in the root directory (excluded from Git):

```env
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_digit_app_password
```

### **2. Running with Docker Compose (Recommended)**
Build and run the Spring Boot container and PostgreSQL database with a single command:

```bash
docker compose up -d --build
```

### **3. Running Locally with Maven**
```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

---

## 🔗 Related Repositories

* **Frontend SPA Repo**: [Finance Tracker Frontend](https://github.com/nishant920/Finance-tracker-frontend-.git)
