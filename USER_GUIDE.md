# NeoBuk Design System Guide

## 🎨 Color Palette

We have implemented a custom color system focused on trust, growth, and technology.

| Role | Color Name | Hex | Usage |
|------|------------|-----|-------|
| **Primary** | Deep Teal | `#0F766E` | Main branding, buttons, app bar, active states. Represents trust & growth. |
| **Accent** | Soft Cyan | `#67E8F9` | Highlights, floating action buttons, secondary actions. Use sparingly. |
| **Background** | Neutral Gray | `#F9FAFB` | App background. |
| **Surface** | White | `#FFFFFF` | Cards, dialogs, bottom sheets. |
| **Text Primary** | Slate Dark | `#0F172A` | Headings, main content. |
| **Text Secondary** | Slate Light | `#475569` | Subtitles, captions, less important text. |

## 🛠 Usage in Code

### Colors
Access colors via `MaterialTheme.colorScheme`:

```kotlin
// Primary (Deep Teal)
MaterialTheme.colorScheme.primary

// Accent (Soft Cyan)
MaterialTheme.colorScheme.secondary

// Background
MaterialTheme.colorScheme.background

// Text Colors
MaterialTheme.colorScheme.onBackground // Primary Text
MaterialTheme.colorScheme.onSurfaceVariant // Secondary Text (Custom mapped)
```

### Typography
The project uses standard Material 3 typography which you can customize in `app/src/main/java/com/neobuk/app/ui/theme/Type.kt`.

### Theme Configuration
The theme is defined in `app/src/main/java/com/neobuk/app/ui/theme/Theme.kt`.
Verified: `dynamicColor` is set to `false` by default to enforce your custom palette.

## 📱 Dark Mode Support
We have included a preliminary Dark Mode map:
- **Primary**: Deep Teal (Maintained)
- **Background**: Slate 900 (`#0F172A`)
- **Surface**: Slate 800 (`#1E293B`)
- **Text**: Slate 100 (`#F1F5F9`)

## 🚀 Future Design Steps
1. **Typography**: Consider adding a custom font family like 'Inter' or 'Roboto' to match the modern aesthetic.
2. **Components**: Build custom buttons and cards that strictly adhere to the corner radius and elevation of your design system.
