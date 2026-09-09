# Enterprise Issue & Project Management Platform

[![Spring Boot 3.3.4](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL 18](https://img.shields.io/badge/PostgreSQL-18-blue.svg)](https://www.postgresql.org/)
[![React 19](https://img.shields.io/badge/React-19-61dafb.svg)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.x-blue.svg)](https://www.typescriptlang.org/)
[![Tailwind CSS v4](https://img.shields.io/badge/Tailwind%20CSS-v4-38b2ac.svg)](https://tailwindcss.com/)
[![Tests](https://img.shields.io/badge/Tests-36%20Backend%20%7C%205%20Frontend%20Passed-success.svg)](#testing--verification)

A production-grade, enterprise-ready **Issue Tracking & Project Management Platform** (Jira / Linear clone) designed for agile engineering teams. Built with a high-performance **Spring Boot 3 + Java 21** REST backend, native **PostgreSQL 18** database with Flyway versioned migrations, and a modern **React 19 + TypeScript + Tailwind CSS** frontend featuring interactive drag-and-drop Kanban boards, sprint management, real-time activity feeds, in-app notifications, and KPI analytics.

---

## 📑 Table of Contents
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [Database Schema & Architecture](#-database-schema--architecture)
- [Role-Based Access Control (RBAC)](#-role-based-access-control-rbac)
- [REST API Specification](#-rest-api-specification)
- [Tech Stack](#-tech-stack)
- [Local Setup & Quickstart Guide](#-local-setup--quickstart-guide)
- [Pre-Seeded Demo Credentials](#-pre-seeded-demo-credentials)
- [Testing & Verification](#-testing--verification)

---

## 🌟 Key Features

1. **Enterprise Multi-Tenancy & Project Hierarchy**:
   - Organization $\rightarrow$ Project Workspaces $\rightarrow$ Sprints & Milestones $\rightarrow$ Issues & Tasks.
   - Customizable project identifiers with auto-incrementing issue keys (e.g. PCE-1, PCE-2).
2. **Interactive Drag-and-Drop Kanban Board**:
   - 5-column Agile workflow (TODO, IN_PROGRESS, IN_REVIEW, DONE, CLOSED).
   - Smooth drag-and-drop transitions powered by @dnd-kit with optimistic UI updates.
   - Live full-text search and instant multi-attribute filters (Priority, Type, Assignee).
3. **Sprint & Milestone Engine**:
   - Time-boxed sprint planning with start dates, due dates, and completion status.
   - Live sprint completion progress calculations and open/closed issue distribution.
4. **Project Analytics & Executive Dashboard**:
   - Interactive charts powered by **Recharts** (status distribution bar charts, priority breakdown pie charts).
   - Real-time personal workspace metrics: Assigned tasks count, pending resolutions, active projects.
5. **Real-Time Collaboration & Auditing**:
   - Issue comment threads with author attribution and markdown formatting.
   - Immutable audit logging (udit_logs) tracking every status transition, assignment change, and priority update.
6. **In-App Notification Center**:
   - Unread notification counter badge in the navigation bar.
   - Automated event triggers on task assignments and discussion mentions.
7. **Strict Security & Role-Based UI Adaptation**:
   - Stateless JWT Bearer token authentication with BCrypt password hashing.
   - Dynamic UI adapting visibility of buttons and forms based on user role (ADMIN, PROJECT_MANAGER, DEVELOPER, VIEWER).

---

## 🏗️ System Architecture

`mermaid
flowchart TD
    subgraph Client Layer ["Client Layer (Browser)"]
        UI["React 19 SPA (TypeScript)"]
        Router["React Router v7"]
        Query["TanStack React Query"]
        DND["@dnd-kit Drag-and-Drop"]
        Charts["Recharts Visualizations"]
    end

    subgraph Gateway Layer ["API Gateway & Routing"]
        ViteProxy["Vite Dev Server (Port 5173)"]
        CORS["CORS & Security Filter Chain"]
    end

    subgraph Backend Layer ["Spring Boot 3.3.4 (Port 8080)"]
        AuthFilter["JwtAuthenticationFilter"]
        Controllers["REST API Controllers"]
        Services["Business Service Layer"]
        Repositories["Spring Data JPA Repositories"]
    end

    subgraph Persistence Layer ["Database Layer (Port 5432)"]
        Flyway["Flyway Migration Engine (V1..V11)"]
        Postgres[("PostgreSQL 18 Enterprise Database")]
    end

    UI --> Router
    UI --> Query
    Query --> ViteProxy
    ViteProxy --> CORS
    CORS --> AuthFilter
    AuthFilter --> Controllers
    Controllers --> Services
    Services --> Repositories
    Repositories --> Postgres
    Flyway --> Postgres
`

---

## 🗄️ Database Schema & Architecture

The database is built on **PostgreSQL 18** and version-controlled using **11 Flyway SQL migrations**:

`mermaid
erDiagram
    ORGANIZATIONS ||--o{ PROJECTS : contains
    USERS ||--o{ PROJECT_MEMBERS : belongs_to
    PROJECTS ||--o{ PROJECT_MEMBERS : has
    PROJECTS ||--o{ MILESTONES : contains
    PROJECTS ||--o{ ISSUES : tracks
    USERS ||--o{ ISSUES : reports
    USERS ||--o{ ISSUES : assigned_to
    MILESTONES ||--o{ ISSUES : groups
    ISSUES ||--o{ ISSUE_COMMENTS : receives
    ISSUES ||--o{ AUDIT_LOGS : records
    USERS ||--o{ NOTIFICATIONS : receives

    ORGANIZATIONS {
        bigserial id PK
        varchar name
        text description
    }

    PROJECTS {
        bigserial id PK
        varchar name
        varchar key
        text description
        bigint organization_id FK
    }

    USERS {
        bigserial id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar role
    }

    ISSUES {
        bigserial id PK
        varchar issue_key UK
        varchar title
        text description
        varchar type
        varchar status
        varchar priority
        bigint project_id FK
        bigint reporter_id FK
        bigint assignee_id FK
        bigint milestone_id FK
        date due_date
    }

    MILESTONES {
        bigserial id PK
        varchar title
        text description
        varchar status
        date due_date
        bigint project_id FK
    }

    ISSUE_COMMENTS {
        bigserial id PK
        text comment
        bigint issue_id FK
        bigint user_id FK
    }

    NOTIFICATIONS {
        bigserial id PK
        bigint user_id FK
        varchar title
        text message
        varchar type
        boolean is_read
    }

    AUDIT_LOGS {
        bigserial id PK
        bigint user_id FK
        varchar action
        varchar entity_type
        bigint entity_id
        text metadata
    }
`

---

## 🛡️ Role-Based Access Control (RBAC)

| Capability / Action | 👑 ADMIN | 👔 PROJECT_MANAGER | 💻 DEVELOPER | 👁️ VIEWER |
| :--- | :---: | :---: | :---: | :---: |
| **Create Projects & Organizations** | ✅ Yes | ✅ Yes | ❌ No (Hidden) | ❌ No (Hidden) |
| **Delete Projects & Issues** | ✅ Yes | ❌ No (Hidden) | ❌ No (Hidden) | ❌ No (Hidden) |
| **Create & Complete Sprints** | ✅ Yes | ✅ Yes | ❌ No (Hidden) | ❌ No (Hidden) |
| **Add / Remove Team Members** | ✅ Yes | ✅ Yes | ❌ No (Hidden) | ❌ No (Hidden) |
| **Create New Tasks / Bugs** | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No (Hidden) |
| **Kanban Drag-and-Drop Workflow** | ✅ Yes | ✅ Yes | ✅ Yes | 🔒 Read-Only |
| **Change Status / Priority / Assignee** | ✅ Yes | ✅ Yes | ✅ Yes | 🔒 Disabled |
| **Post Comments & Discussions** | ✅ Yes | ✅ Yes | ✅ Yes | 🔒 Read-Only |
| **Delete Comments** | ✅ Any comment | ⚠️ Own only | ⚠️ Own only | ❌ Hidden |
| **View Analytics & Dashboards** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |

---

## 📡 REST API Specification

### 1. Authentication & Users (/api/auth)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| POST | /api/auth/register | Public | Register new user account |
| POST | /api/auth/login | Public | Authenticate user & return JWT Bearer token |
| GET | /api/auth/me | Authenticated | Retrieve current user profile |

### 2. Organizations & Projects (/api/organizations, /api/projects)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| POST | /api/organizations | ADMIN, PM | Create organization |
| GET | /api/organizations | Authenticated | List all organizations |
| POST | /api/projects | ADMIN, PM | Create a new project workspace |
| GET | /api/projects | Authenticated | Get paginated projects with stats |
| GET | /api/projects/{id} | Authenticated | Get project details by ID |
| PUT | /api/projects/{id} | ADMIN, PM | Update project metadata |
| DELETE | /api/projects/{id} | ADMIN | Delete project permanently |
| POST | /api/projects/{id}/members | ADMIN, PM | Assign team member to project |
| GET | /api/projects/{id}/members | Authenticated | List members of a project |
| DELETE | /api/projects/{id}/members/{userId} | ADMIN, PM | Remove member from project |

### 3. Issues & Jira Workflow (/api/projects/{id}/issues, /api/issues)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| POST | /api/projects/{id}/issues | ADMIN, PM, DEV | Create issue with auto-key (e.g. PCE-1) |
| GET | /api/projects/{id}/issues | Authenticated | Search & filter project issues |
| GET | /api/issues/{id} | Authenticated | Get issue details by ID |
| GET | /api/issues/key/{issueKey} | Authenticated | Get issue details by key |
| PUT | /api/issues/{id} | ADMIN, PM, DEV | Update issue details & attributes |
| PATCH | /api/issues/{id}/status | ADMIN, PM, DEV | Transition status (TODO $\rightarrow$ IN_PROGRESS $\rightarrow$ DONE) |
| DELETE | /api/issues/{id} | ADMIN | Delete issue |
| POST | /api/issues/{id}/comments | ADMIN, PM, DEV | Post comment to issue |
| GET | /api/issues/{id}/comments | Authenticated | List comments for issue |
| DELETE | /api/issues/comments/{id} | Author, ADMIN | Delete comment |
| GET | /api/issues/{id}/audit-logs | Authenticated | Retrieve complete audit trail |

### 4. Milestones & Sprints (/api/projects/{id}/milestones, /api/milestones)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| POST | /api/projects/{id}/milestones | ADMIN, PM | Create a new sprint / milestone |
| GET | /api/projects/{id}/milestones | Authenticated | List project milestones & completion % |
| PATCH | /api/milestones/{id}/close | ADMIN, PM | Close and complete active milestone |

### 5. Analytics & Dashboard (/api/projects/{id}/analytics, /api/dashboard/me)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| GET | /api/projects/{id}/analytics | Authenticated | Status & priority breakdown charts data |
| GET | /api/dashboard/me | Authenticated | User personal dashboard metrics & tasks |

### 6. In-App Notifications (/api/notifications)
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| GET | /api/notifications | Authenticated | List user notifications stream |
| GET | /api/notifications/unread-count | Authenticated | Get unread notifications count |
| PATCH | /api/notifications/{id}/read | Authenticated | Mark single notification as read |
| PATCH | /api/notifications/mark-all-read | Authenticated | Mark all notifications as read |

---

## 💻 Tech Stack

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.3.4** (Web, Data JPA, Security, Validation)
- **PostgreSQL 18** (Native relational database)
- **Flyway** (Database schema versioning & migration)
- **JJWT 0.12.6** (JSON Web Token security)
- **Lombok & MapStruct** (Boilerplate reduction & DTO mapping)
- **SpringDoc OpenAPI 2.6.0** (Interactive Swagger API documentation)
- **JUnit 5, Mockito & H2 Database** (Unit & Integration testing)

### Frontend
- **React 19**
- **TypeScript 5.x**
- **Vite 8.2** (Ultra-fast build tooling)
- **Tailwind CSS v4** (Modern utility-first styling)
- **@dnd-kit** (Accessible drag-and-drop engine)
- **TanStack React Query v5** (Server state management & caching)
- **Axios** (HTTP client with JWT interceptors)
- **Recharts** (Interactive data visualization charts)
- **Lucide React** (Modern iconography)
- **Vitest & React Testing Library** (Frontend test suite)

---

## 🚀 Local Setup & Quickstart Guide

### Prerequisites
- **Java 21 JDK** installed (java -version).
- **Node.js 20+** & **npm** installed (
ode -v).
- **PostgreSQL 18** running locally on port 5432 with database projectmanager, username pmuser, and password pmpassword.

---

### Step 1: Start the Spring Boot Backend

`powershell
cd backend
.\mvnw.cmd spring-boot:run
`
*Backend will start on: **http://localhost:8080***  
*Interactive Swagger UI docs available at: **http://localhost:8080/swagger-ui/index.html***

---

### Step 2: Start the React Frontend

Open a new terminal window:

`powershell
cd frontend
npm install
npm run dev
`
*Frontend will start on: **http://localhost:5173***

---

## 🔑 Pre-Seeded Demo Credentials

Use any of the following pre-configured credentials to test different permission tiers:

| Role | Email Address | Password |
| :--- | :--- | :--- |
| 👑 **System Administrator** | dmin@example.com | password123 |
| 💻 **Software Developer** | developer@example.com | password123 |

*(You can also use the one-click demo buttons on the login card at http://localhost:5173)*

---

## 🧪 Testing & Verification

### Running Backend Unit & Integration Tests
`powershell
cd backend
.\mvnw.cmd test
`
*Result: **36 / 36 tests passed (BUILD SUCCESS)***

### Running Frontend Tests & Typecheck
`powershell
cd frontend
npm run lint       # TypeScript strict type checking (0 errors)
npm run test:run   # Vitest unit test suite (5 / 5 tests passed)
npm run build      # Production minification & bundling
`

---

## 📜 License
This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
