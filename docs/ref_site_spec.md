# Reference Site Specification: Waterballsa World

## Overview
The reference site `https://world.waterballsa.tw` is an educational platform focusing on software engineering excellence (Design Patterns, DDD, AI x BDD). It features course journeys, leaderboards, and user portfolios.

## Functional Architecture

### 1. Landing Page
- **Course Showcases**: Displays various "Journeys" (Software Design Patterns, AI x BDD).
- **CTA**: Links to course details and registration.
- **Social Proof**: Links to Discord and Facebook communities.

### 2. User Portfolio (`/users/[id]/portfolio`)
- **Profile Info**: User name, occupation (e.g., Junior Programmer).
- **Skill Radar/Matrix**: Visual representation of skills across categories:
    - OOA: Structural Analysis, Distinguishing Structure vs Behavior.
    - OOD: Abstraction, Well-Defined Context, Design Patterns Form.
    - OOP: Implementation Proficiency.
- **Journey Progress**: Tracks progress in specific courses (Software Career, DDD, etc.).

### 3. Navigation
- Global navigation: Home, Courses, Leaderboard, All Units.

## UI/UX Elements
- Modern, clean layout probably using components like those found in the FE project (Next.js, Tailwind).
- Dynamic content: Likely uses Mermaid or similar for skill diagrams.
- Educational Focus: Progress bars, certificates, and journey-based navigation.
