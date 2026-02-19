# 📱 Habit Tracker — Android

Native Android app for the **Habit Tracker** platform, built with **Kotlin + Jetpack Compose**. Syncs with the [Habit Tracker Web App](https://github.com/MrCode-2005) via a shared **Supabase** backend.

## ✨ Features

- 📋 **Task Management** — Time blocks (Morning/Evening/Night), recursive subtasks, Eisenhower priority matrix
- ⭐ **Habit Tracking** — Daily completion, streak calculation, GitHub-style contribution heatmap
- 🎯 **Focus Mode** — Immersive timer with audio/video backgrounds, tree growth gamification, cross-device sync
- 🏆 **Goals** — Long-term objectives with live countdown timers
- 📅 **Calendar** — Full month view with event management
- 💰 **Expenses** — Category-based tracking with charts and education fee management
- 📊 **Analytics** — 8+ chart types for productivity insights
- 🎨 **Customization** — 7 theme presets, font picker, glassmorphism toggle

## 🛠️ Tech Stack

| Layer | Technology |
|:---|:---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository Pattern |
| DI | Hilt |
| Local DB | Room (SQLite) |
| Remote | supabase-kt |
| Charts | Vico |
| Media | ExoPlayer |
| Background | WorkManager + Foreground Service |

## 🎨 Design System

| Color | Hex | Usage |
|:---|:---|:---|
| Primary | `#6366f1` | Brand, buttons, active states |
| Success | `#10b981` | Completed, positive |
| Warning | `#f59e0b` | Medium priority |
| Danger | `#ef4444` | Delete, errors |
| Info | `#3b82f6` | Information |
| Dark BG | `#0f172a` | App background (dark) |
| Light BG | `#f8f9fa` | App background (light) |

## 📁 Project Structure

```
com.vishnu.habittracker/
├── data/           # Room entities, DAOs, Supabase API, Repositories
├── di/             # Hilt dependency injection modules
├── domain/         # Models, UseCases (streak calc, subtask logic)
├── ui/             # Compose screens (dashboard, habits, focus, etc.)
├── service/        # FocusTimerService (Foreground Service)
└── util/           # Extensions, date helpers
```

## 🔗 Supabase Sync

Both the web app and this Android app connect to the **same Supabase instance**, enabling real-time cross-platform data sync. Row Level Security (RLS) ensures each user sees only their own data.

## 📄 License

MIT
