# Smart Clipboard AI — Manual Test Checklist

## Setup
- Install debug APK on emulator or physical device
- Ensure no existing app data (uninstall first if needed)

## 1. Clean App Launch
- [ ] Open Smart Clipboard AI
- [ ] Expected: Dashboard shows "No items yet" with AI banner and Screenshot import card

## 2. Share Text
- [ ] From another app (e.g., browser), share plain text to Smart Clipboard AI
- [ ] Expected: Transparent receiver screen appears with "Text saved"
- [ ] Expected: Screen dismisses automatically
- [ ] Check Main Dashboard: Text item appears with type "Text", source "Shared"

## 3. Share URL
- [ ] Share a URL (e.g., `https://developer.android.com`)
- [ ] Expected: Receiver shows "Link saved"
- [ ] Check Dashboard: URL item appears with type "Link"

## 4. Share Image
- [ ] Share an image from Gallery
- [ ] Expected: Receiver shows "File saved"
- [ ] Check Dashboard: Image item appears with thumbnail
- [ ] Verify thumbnail loads correctly (160dp tall, cropped)

## 5. Share PDF/File
- [ ] Share a PDF from file manager
- [ ] Expected: Receiver shows "File saved" or "Saved N item(s)"
- [ ] Check Dashboard: FILE item appears (no thumbnail)

## 6. Quick Settings Tile Clipboard Capture
- [ ] If tile is available: Add "Clip Save" tile to Quick Settings
- [ ] Copy some text to clipboard
- [ ] Tap the tile from Quick Settings
- [ ] Expected: Transparent UI appears showing "Saved clipboard text"
- [ ] Check Dashboard: Text item with source "Clipboard tile"

## 7. Media Permission Request
- [ ] Open Main Dashboard
- [ ] Tap "Allow access" on Screenshot import card
- [ ] Grant image/media permission when prompted
- [ ] Expected: Permission banner disappears, scan starts

## 8. Screenshot Import
- [ ] After granting permission, screenshot scan runs
- [ ] Expected: Message shows "Imported N screenshots" or "No screenshots found"
- [ ] Check Dashboard: SCREENSHOT items appear with thumbnails if imported
- [ ] Tap "Scan screenshots" again → No duplicates created

## 9. Thumbnail Display and Fallback
- [ ] IMAGE and SCREENSHOT cards should show 160dp tall thumbnails
- [ ] If image cannot load, see "Preview unavailable" + type label as fallback
- [ ] Verify no crash on invalid URIs

## 10. Filters and Counts
- [ ] Main Dashboard shows total count: "N total · T Text · L Link · I Image · F File · S Screenshot"
- [ ] Tap each filter chip (ALL, TEXT, LINK, IMAGE, FILE, SCREENSHOT)
- [ ] Expected: Only matching items shown
- [ ] Expected: "No items match the selected filter" if empty

## 11. Delete Item
- [ ] Tap "Delete" on any item card
- [ ] Expected: "Delete item?" dialog appears
- [ ] Tap "Delete" → Item removed, snackbar "Item deleted"
- [ ] Tap "Cancel" → Dialog dismissed, item remains

## 12. Clear All
- [ ] Tap "Clear All" in top app bar (only visible when items exist)
- [ ] Expected: "Clear all items?" dialog
- [ ] Tap "Clear" → All items removed, snackbar "All items cleared"
- [ ] Tap "Cancel" → Dialog dismissed, items remain

## 13. Selection Mode
- [ ] Tap "Select" in top bar when items exist
- [ ] Expected: Checkboxes appear on all item cards, "Done" appears in top bar
- [ ] Tap "Done" → Returns to normal mode, selection cleared
- [ ] Enter selection mode, check some checkboxes
- [ ] Expected: Selected count shown, "Review handoff" enabled
- [ ] Tap "Select visible" → All visible items selected
- [ ] Tap "Clear" → Selection cleared (only visible when items selected)

## 14. Handoff Draft Editing
- [ ] Select 1+ items, tap "Review handoff"
- [ ] Expected: Bottom sheet opens with "Review handoff draft"
- [ ] Title/body shown and editable
- [ ] Body field is multi-line (min 180dp height)
- [ ] Edit title → displayed correctly
- [ ] Edit body → displayed correctly

## 15. Share Draft Chooser
- [ ] In handoff sheet, tap "Share draft"
- [ ] Expected: Sheet dismissed, Android chooser appears
- [ ] Choose a text receiver (e.g., Keep, Notes, Gmail)
- [ ] Expected: Draft text pasted with title, items, sources

## 16. Calendar Draft Screen
- [ ] Add/select an item containing date/time text (e.g. "Meeting 2026-05-20 14:00")
- [ ] Open handoff sheet
- [ ] Expected: "Calendar draft" button appears
- [ ] Tap "Calendar draft"
- [ ] Expected: Calendar app opens with title, description, begin/end time
- [ ] Save or discard in Calendar app → No event saved automatically

## 17. Android 14 Selected Photo Access
- [ ] On API 34+ device: Grant partial photo access
- [ ] Verify only accessible screenshots are imported
- [ ] No crash on partial access

## 18. API 24-28 MediaStore No-Crash (if emulator available)
- [ ] On API 24-28 emulator: Open app
- [ ] Grant storage permission
- [ ] Run screenshot scan
- [ ] Expected: No crash from RELATIVE_PATH column access

## Regression: Existing Features
- [ ] Share text works
- [ ] Share URL works
- [ ] Delete item works
- [ ] Clear all works
- [ ] Screenshot import works
- [ ] Thumbnails render correctly
- [ ] Clipboard tile works (if set up)