# Gap Analysis & Todo List: Tutorial Platform

## Feature Comparison

| Feature | Reference Site (world.waterballsa.tw) | Current Implementation | Status |
| :--- | :--- | :--- | :--- |
| **Landing Page** | Full marketing site with journey cards | Basic structures in `app/home` and components | [/] Partial |
| **Portfolio** | Radial/Matrix skill chart, occupation info | Timeline list with skill Tags, occupation in data | [/] Partial |
| **Leaderboard** | Ranking of students by progress/exp | Implementation exists in `LeaderboardService` and `(public)/leaderboard/page.tsx` | [/] In Progress |
| **Journeys** | Interactive learning paths with gyms/SOPs | Routes and entities implemented (`/journeys/[slug]`) | [x] Core Done |
| **Feedback** | Tutor comments and skill ratings | `ChallengePortfolio` fetches and renders feedback | [x] Core Done |
| **Community** | Discord/Facebook integration | Links present in Footer | [x] Done |

## Todo List

### 1. High Priority: Leaderboard Refinement
- [x] **Backend**: Ensure `LeaderboardController` is fully implemented and integrated with the Service.
- [x] **Frontend**: Polishing the `app/(public)/leaderboard` page (styling, animations).

### 2. Enhancement: Portfolio Visualization
- [x] **Frontend**: Implement a Radar Chart or Matrix view in `ChallengePortfolio.tsx`.
- [x] **Backend**: Ensure `ratings` JSON in `GymChallengeRecord` consistently uses the OOA/OOD/OOP mapping.

### 3. Feature: User Settings
- [x] **Frontend**: Create a profile edit page for `nickName`, `occupation`, and `avatar`.
- [x] **Backend**: Verify `MemberController` has update endpoints.

### 4. Polishing: Course Content
- [x] **Content**: Synchronize Journey, Chapter, and Lesson data.
- [x] **UI**: Enhancing `VideoPlayer` and `MarkdownRenderer`.
