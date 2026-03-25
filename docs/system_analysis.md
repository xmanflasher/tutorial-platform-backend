# Frontend Current Status: Tutorial Platform

## Architecture Overview
The frontend is built with **Next.js 16** and **React 19**, utilizing the **App Router** and a structure inspired by **Clean Architecture** or **Hexagonal Architecture**.

### Directory Structure
- `src/app`: Page routing and layouts.
- `src/components`: UI components (e.g., `ChallengePortfolio`, `VideoPlayer`).
- `src/services`: API client logic.
- `src/context`: State management (Auth, Journey).
- `src/adapters`: Interface with external systems.
- `src/types`: TypeScript definitions.

## Key Components & Flow

### 1. Challenge Portfolio (挑戰作品集) <!-- id: SA-01 --> [Phase 1 Implementation]
- **Purpose**: Displays a user's challenge history and tutor feedback.
- **Data Fetching**: Calls `GET /api/users/{id}/journeys/gyms/challenges/records`.
- **Features**:
    - Timeline view of submissions.
    - Image gallery with zoom capabilities (for UML diagrams).
    - Markdown rendering for tutor feedback.
    - Skill rating visualization (Radar/Matrix style based on OOA/OOD/OOP).

### 2. Journey Subsystem (旅程子系統) <!-- id: SA-02 --> [Phase 1 & 3 Implementation]
- **Unified Routing**: Consolidated `journeys` under `(public)` to prevent Next.js 404 route collisions and share global Context smoothly.
- **Dynamic Routing**: Uses `[slug]` for different learning paths.
- **Immersive Full-Screen**: Pages like `roadmap` and `lessons` use dynamic layout toggles to hide the Sidebar for focused, immersive viewing while retaining global App contexts.
- **Gyms & SOPs**: Sub-routes for specific exercise areas.

### 3. Onboarding Subsystem (新手導覽系統) <!-- id: SA-03 --> [Phase 2 Implementation]
- **Purpose**: First-time user guidance, role identification, and Demo Simulation helper.
- **Flow**: Role Selection -> 6-step Guided Tour.
- **Role Sync**: Captures user role (HR/Tech Lead/etc.) and syncs with `Member` profile in backend.
- **Desktop Sprite (Spirit / Waterball Fairy)**: Persistent floating helper for revisiting guide content. **[Update]**: Refactored the `group` trigger strictly around the icon bounds to prevent unwanted accidental expansion of the chat panel. Provides tools to trigger `Demo Mode` backend endpoints (auto-completing gyms and missions).

### 4. Course & Gym Viewer (課程與道館檢視器) <!-- id: SA-04 --> [Phase 3 Implementation]
- **Purpose**: Single dynamic route layout for interacting with Gym content and Lessons.
- **Fixes**: 
  - Restored `VideoPlayer` (YouTube API) layout behavior, fixing CSS rendering logic that hid the iFrame.
  - Implemented manual routing fallback (`useRouter().back()`) for better user navigation flow.
  
### 5. Checkout & Ordering Flow (結帳與訂單流程) <!-- id: SA-05 --> [Phase 4 Implementation]
- **Purpose**: Course purchasing architecture allowing Zingala installments, credit cards, or ATM.
- **Architecture**: Separated via `orderService.ts` and mock boundaries to permit safe testing. Handled duplicate purchases properly relying on global `orderStore`.

### 4. Auth & Logout mechanism
- **Absolute Logout**: Implemented a multi-layer cleanup (clearing `localStorage`, invalidating server session, and injecting expired `JSESSIONID` cookies).
- **Session Management**: REST-friendly logout endpoint at `/api/auth/logout`.

## Component Relationship Diagram
```mermaid
graph TD
    App[Root Layout] --> AuthProvider
    AuthProvider --> JourneyProvider
    JourneyProvider --> Pages
    Pages --> PageLayout[Layout]
    PageLayout --> Nav[NavBar]
    PageLayout --> MainContent
    MainContent --> CP[ChallengePortfolio]
    MainContent --> VP[VideoPlayer]
    CP --> SG[SubmissionGallery]
    CP --> FC[FeedbackCard]
    FC --> MR[MarkdownRenderer]
```

## Tech Stack
- **Framework**: Next.js 16 (App Router)
- **Styling**: Tailwind CSS 4
- **Icons**: Lucide React
- **Markdown**: react-markdown
- **Media**: react-youtube, react-player
- **Notifications**: Sonner
