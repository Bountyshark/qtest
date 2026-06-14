You are implementing a Jetpack Compose UI for an Android exam practice app.

CRITICAL RULES:
- Use ONLY Icons.Default.* (never AutoMirrored, never extended icons)
- Available icons: Add, ArrowBack, ArrowForward, Check, Clear, Close, Delete, Info, KeyboardArrowDown, KeyboardArrowUp, MoreVert, PlayArrow, Refresh, Share
- FORBIDDEN icons: ContentCopy, Save, Description, FileCopy, Edit, Send, Star
- Navigation is manual: viewModel.navigateTo(Screen.X) — NEVER use Navigation Compose
- All state is in ExamViewModel, not in @Composable remember blocks
- Use BackHandler for system back button interception
- Spacing: xs=4dp, sm=8dp, md=12dp, lg=16dp, xl=24dp, xxl=32dp
- Cards: RoundedCornerShape(16.dp), Buttons: RoundedCornerShape(12.dp)
- Primary color for selected/active states, surfaceVariant for backgrounds
- Success green: Color(0xFF2E7D32), Saved blue: Color(0xFF3F51B5)
- Add testTag to all interactive elements
- Subtle animations only: AnimatedVisibility, animateColorAsState, spring scale