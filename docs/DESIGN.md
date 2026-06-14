# Design System — Exam Grader & Tracker

## 1. Color Palette

### Primary — Indigo (trust, focus, academia)
| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `primary` | `#3F51B5` | `#9FA8DA` | App bars, primary buttons, links |
| `onPrimary` | `#FFFFFF` | `#1A1A2E` | Text/icon on primary |
| `primaryContainer` | `#C5CAE9` | `#3949AB` | Filled cards, chip bg |
| `onPrimaryContainer` | `#1A1A2E` | `#E8EAF6` | Text on primary container |

### Secondary — Teal (growth, positivity, correctness)
| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `secondary` | `#00796B` | `#80CBC4` | Correct counts, secondary actions |
| `onSecondary` | `#FFFFFF` | `#1A1A2E` | Text on secondary |
| `secondaryContainer` | `#B2DFDB` | `#00695C` | Timer pills, stat badges |

### Tertiary — Amber/Warm (caution, drafts)
| Token | Light | Dark | Usage |
|-------|-------|------|-------|
| `tertiary` | `#F57C00` | `#FFB74D` | Draft badges, "Continue" buttons |
| `onTertiary` | `#FFFFFF` | `#1A1A2E` | Text on tertiary |
| `tertiaryContainer` | `#FFE0B2` | `#E65100` | Draft chip backgrounds |

### Error/Success
- **Error**: `#D32F2F` / `#EF9A9A` — Incorrect answers, destructive actions
- **On Error**: `#FFFFFF` / `#1A1A2E`
- **Error Container**: `#FFCDD2` / `#C62828`

### Surface & Background
- **Surface**: `#FAFAFA` (light) / `#121212` (dark)
- **Background**: `#F5F5F5` (light) / `#121212` (dark)
- **SurfaceVariant**: `#E8EAF6` (light) / `#2D2D3A` (dark)
- **Outline**: `#BDBDBD` / `#616161`
- **OutlineVariant**: `#E0E0E0` / `#3A3A4A`

### Semantic tokens (for exam grading)
- **correct**: `#2E7D32` — correct answer indicators
- **incorrect**: `#D32F2F` — wrong answer indicators
- **blank**: `#9E9E9E` — unanswered question indicators
- **saved**: `#3F51B5` — saved (no key) answer indicators

## 2. Typography

Use system `FontFamily.Default` (Roboto on Android).

| Style | Weight | Size | Line Height | Usage |
|-------|--------|------|-------------|-------|
| `displayLarge` | Bold | 32sp | 40sp | Hero / empty state title |
| `headlineMedium` | Bold | 24sp | 32sp | Screen titles |
| `titleLarge` | Black | 20sp | 28sp | Top app bar title |
| `titleMedium` | Bold | 16sp | 24sp | Card titles, section headers |
| `titleSmall` | Bold | 14sp | 20sp | Subsection headers |
| `bodyLarge` | Normal | 16sp | 24sp | Body text |
| `bodyMedium` | Normal | 14sp | 20sp | Secondary body text |
| `bodySmall` | Normal | 12sp | 16sp | Captions, metadata |
| `labelLarge` | Bold | 14sp | 20sp | Button text |
| `labelMedium` | SemiBold | 12sp | 16sp | Chips, small buttons |
| `labelSmall` | Medium | 11sp | 16sp | Tiny labels, badges |

## 3. Spacing Scale

Use Compose `dp` values exclusively:

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Inner padding in badge/chips |
| `sm` | 8dp | Between related elements |
| `md` | 12dp | Between grouped elements |
| `lg` | 16dp | Card padding, screen edges |
| `xl` | 24dp | Section spacing, large gaps |
| `xxl` | 32dp | Major section breaks |

## 4. Shape System

| Token | Value | Usage |
|-------|-------|-------|
| `none` | 0dp | Full-bleed elements |
| `small` | 8dp | Buttons, small cards |
| `medium` | 12dp | Cards, dialogs |
| `large` | 16dp | Large cards, text fields |
| `full` | 50% | Circular avatars, option bubbles |

## 5. Elevation / Shadow

| Level | Elevation | Usage |
|-------|-----------|-------|
| 0 | 0dp | Flat surfaces |
| 1 | 1dp | Cards resting state |
| 2 | 2dp | FAB, raised buttons |
| 3 | 4dp | Bottom bar, top app bar |
| 4 | 8dp | Dialogs, menus |

## 6. Icons (Whitelist)

Only `material-icons-core` is enabled. Use only:

| Icon | Usage |
|------|-------|
| `Icons.Default.Add` | Create new |
| `Icons.AutoMirrored.Filled.ArrowBack` | Navigate back |
| `Icons.AutoMirrored.Filled.ArrowForward` | Navigate forward |
| `Icons.Default.Check` | Confirm, copy |
| `Icons.Default.Clear` | Clear answer |
| `Icons.Default.Close` | Dismiss, cancel |
| `Icons.Default.Delete` | Delete action |
| `Icons.Default.Info` | Info/help |
| `Icons.Default.KeyboardArrowDown` | Expand |
| `Icons.Default.KeyboardArrowUp` | Collapse |
| `Icons.Default.MoreVert` | Menu |
| `Icons.Default.PlayArrow` | Start exam |
| `Icons.Default.Refresh` | Retake |
| `Icons.Default.Share` | Share CSV |
| `Icons.Default.Search` | Search |
| `Icons.Default.Edit` | Edit |
| `Icons.Default.Menu` | Navigation menu |
| `Icons.Default.Home` | Dashboard home |
| `Icons.Default.Star` | Favorite/bookmark |
| `Icons.Default.Warning` | Warning/alert |
| `Icons.Default.Done` | Completed |
| `Icons.Default.DateRange` | Date |

## 7. Animation Rules

- **Duration**: All animations `tween(durationMillis = 300)` unless noted
- **Springs**: Option selection uses `spring(dampingRatio = 0.6, stiffness = 400)`
- **Fade in/out**: `fadeIn + fadeOut` with 200ms duration
- **Scale**: Bouncy spring for option tap feedback
- **Content transitions**: `AnimatedVisibility` with `fadeIn + slideInVertically`

## 8. Component Library

All components live in `com.example.ui.components` package.

### `AppTopBar`
Standard top app bar with:
- Optional navigation icon (back arrow)
- Title text
- Optional action buttons (max 3)
- Underline divider

### `GradedCard`
Elevated card with configurable:
- Container color (defaults to `surface`)
- Border (optional, with color and width)
- Corner shape (defaults to `medium` 12dp)
- On-click handler
- Leading icon, title, subtitle, trailing content slots

### `StatChip`
Compact colored chip for displaying a single stat (count + label).
Colors: `primary`, `secondary`, `tertiary`, `error`, `correct` (#2E7D32), `blank` (#9E9E9E).

### `DraftBadge`
Small tertiary-colored pill showing "Draft" label.

### `QuestionBubble`
Circular option selector (1-4) with:
- Animated scale on select (bouncy spring)
- Animated color transition
- Selected/unselected/answered states
- Clear button overlay when answered

### `OMRGrid`
Collapsible bubble grid showing all question answers in a matrix layout.
- 6 columns per row
- Color-coded (correct=green, incorrect=red, blank=gray, saved=blue)
- Shows correct key answer below wrong answers

### `ConfirmDialog`
Reusable confirmation dialog with:
- Title, message, confirm button text, dismiss button text
- Optional destructive (error-colored) confirm button

### `EmptyState`
Full-screen centered placeholder with:
- Large icon in circular container
- Title text
- Body text
- Optional CTA button

## 9. Screen Layout Patterns

- **Dashboard**: TopAppBar + LazyColumn of exam cards + FAB
- **ExamDetail**: TopAppBar (back) + LazyColumn of details + attempts list
- **Create**: TopAppBar (back) + scrollable Column with form sections
- **TakeExam**: TopAppBar (section name + timer + submit) + LazyColumn of questions + bottom navigation bar
- **Results**: TopAppBar (back + share) + LazyColumn of scorecard + OMR grid + section breakdown

## 10. Dark Mode

- Dynamic color on Android 12+ (Material You)
- Custom dark scheme fallback for pre-Android 12
- All semantic tokens have dark variants
- Images/icons use `onSurface`/`onBackground` tints that adapt
