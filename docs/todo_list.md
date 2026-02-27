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
- [ ] **Backend**: Ensure `LeaderboardController` is fully implemented and integrated with the Service.
- [ ] **Frontend**: Polishing the `app/(public)/leaderboard` page (styling, animations).

### 2. Enhancement: Portfolio Visualization
- [ ] **Frontend**: Implement a Radar Chart or Matrix view in `ChallengePortfolio.tsx` to replace or supplement the current tag-based ratings.
- [ ] **Backend**: Ensure `ratings` JSON in `GymChallengeRecord` consistently uses the OOA/OOD/OOP mapping.

### 3. Feature: User Settings
- [ ] **Frontend**: Create a profile edit page to allow users to update `nickName`, `occupation`, and `avatar`.
- [ ] **Backend**: Verify `MemberController` has update endpoints (likely exists but needs verification).

### 4. Polishing: Course Content
- [ ] **Content**: Synchronize Journey, Chapter, and Lesson data with the official platform's structure.
- [ ] **UI**: Enhancing `VideoPlayer` and `MarkdownRenderer` for better educational experience.
