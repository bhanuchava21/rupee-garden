# Rupee Garden - Features

## Overview
Rupee Garden is a gamified savings tracker Android app built with Kotlin + Jetpack Compose. Users earn XP by saving money daily, grow virtual trees, and track spending habits.

---

## Core Features

### 1. Daily Session System
- Start a daily session to "plant a seed"
- Watch your plant grow through 4 stages (Seed → Sprout → Young → Full)
- Growth cycle: 30 seconds for testing (Seed: 0-5s, Sprout: 5-15s, Young: 15-30s, Full: 30s+)
- Session resets at midnight daily
- Resume active sessions if app is closed

### 2. Save or Spend Decision
- At end of day, choose: **Saved** or **Spent**
- **Saved**: Healthy green tree, +50 XP
- **Spent**: Log amount, category, and note; withered brown tree, +10 XP
- Starting a session awards +5 XP

### 3. XP & Leveling System
- **XP Sources:**
  - Start session: +5 XP
  - Saved day: +50 XP
  - Spent day: +10 XP
- **Level formula:** `floor(totalXP / 200) + 1`
- Progress bar shows XP to next level

### 4. Streak Tracking
- Consecutive save days build your streak
- Spending resets streak to 0
- Tracks current streak and longest streak

---

## Screens

### Home Screen
- Minimal toolbar with 3 action icons (Garden, Insights, Settings)
- Stats card: Level, XP, Streak, Saved/Spent counts
- Monthly budget progress bar
- Quick stats: Saved/Spent days this month
- Navigation cards to Garden and Insights

### Session Screen (Growing Session)
- Timer ring showing elapsed time
- Animated plant visual that grows over time
- "End Day" button to complete session

### End Day Check Screen
- Two choices: "I Saved" or "I Spent"
- Spending form with:
  - Category selector (7 categories with emojis)
  - Amount input (₹)
  - Optional note

### Completion Screen
- Result display with animated plant
- XP summary breakdown
- **Confetti animation** when saving
- Buttons: Continue, View Garden

### Garden Screen
- **Isometric 4x4 tree grid** on grass platform
- Trees are green (saved) or brown (spent)
- **Tap trees** to view entry details
- Month navigation (previous/next)
- Period filters: Day, Week, Month
- Stats: Saved days, Spent days, Total trees
- Entry list below garden

### Insights Screen
- Month selector with navigation
- Monthly budget card with progress
- Monthly overview: Saved/Spent days, XP earned, Savings rate
- **Spending by category** breakdown with progress bars

### Settings Screen
- **Monthly Budget editor** with text input and quick presets (₹5k, 10k, 15k, 20k)
- **Achievements button** → Achievements screen
- Demo data: Load/Clear sample data
- About section with app info

### Achievements Screen
- 2-column grid of achievement badges
- Counter: X/17 unlocked
- Locked achievements shown dimmed

---

## Achievements (17 Total)

### Streak Achievements
| Badge | Name | Requirement |
|-------|------|-------------|
| 🌱 | First Save | Complete your first save day |
| 🔥 | Week Warrior | 7-day streak |
| ⭐ | Month Master | 30-day streak |
| 💯 | Century Saver | 100-day streak |

### Level Achievements
| Badge | Name | Requirement |
|-------|------|-------------|
| ⬆️ | Rising Star | Reach Level 5 |
| 🔟 | Double Digits | Reach Level 10 |
| 🏅 | Quarter Century | Reach Level 25 |
| 🏆 | Half Century | Reach Level 50 |

### Total Saves Achievements
| Badge | Name | Requirement |
|-------|------|-------------|
| 🌿 | Getting Started | Save for 10 days total |
| 🌳 | Dedicated Saver | Save for 50 days total |
| 🏰 | Savings Champion | Save for 100 days total |

### XP Achievements
| Badge | Name | Requirement |
|-------|------|-------------|
| ✨ | XP Hunter | Earn 1,000 XP |
| 💫 | XP Master | Earn 5,000 XP |
| 🌟 | XP Legend | Earn 10,000 XP |

### Garden Achievements
| Badge | Name | Requirement |
|-------|------|-------------|
| 🌲 | First Tree | Plant your first tree |
| 🏡 | Full Garden | 16+ trees in a month |

---

## Spending Categories
| Emoji | Category |
|-------|----------|
| 🍔 | Food & Dining |
| 🚗 | Transport |
| 🛍️ | Shopping |
| 🎬 | Entertainment |
| 📱 | Bills & Utilities |
| 💊 | Health |
| 📦 | Other |

---

## UI/UX Features

### Animations
- **Confetti** on successful save (Completion screen)
- **Pulsing plant** animation on Completion screen
- Plant growth stages during session

### Compact Toolbars
- All screens use minimal, space-efficient headers
- Home screen: Icons only (no title)
- Other screens: Back button + title in single row

### Theme
- Dark mode support
- Color palette:
  - Green Primary: `#5C9E4A` (success/saved)
  - Spent Red: `#C4856A` (spent/warning)
  - XP Gold: `#D4A854` (XP display)
  - Grass Green: `#90C67C` (garden)
  - Tree Green: `#4CAF50` (healthy trees)
  - Withered Brown: `#8D6E63` (spent trees)

---

## Data Persistence
- **DataStore Preferences** for local storage
- Stores: User progress, Entries, Active session, Achievements
- JSON serialization with Kotlinx Serialization

---

## Demo Data
- Load sample data: Oct 2025 - Jan 2026 (4 months)
- ~100 entries with 70% save rate
- Random categories and amounts for spent days
- Clear all data option in Settings

---

## Technical Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean Architecture
- **Navigation:** Navigation Compose
- **Storage:** DataStore Preferences
- **Serialization:** Kotlinx Serialization

---

## Future Enhancement Ideas
- Daily reminders (notifications)
- Sound effects
- Home screen widget
- Garden themes (zen, tropical, forest)
- Seasonal garden changes
- Spending trend charts
- Data export (CSV)
- Cloud sync
