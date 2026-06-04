# Milestone 2 Todo

This todo list tracks the implementation scope from the Map Extended, Notifications, Profile Upload, Share Family, Responsive, OWASP, OpenAPI, and bonus specifications.

## Foundation

- [x] Add new REST API contracts to app data layer.
- [x] Add `profileImageUrl` to user/profile/family member/presence DTO and domain models.
- [x] Add profile photo upload repository support for `POST /api/me/photo`.
- [x] Add notification repository support for subscribe, unsubscribe, broadcast, and greeting endpoints.
- [x] Add local SharedPreferences-backed profile toggles for notification and location-sharing preferences.
- [x] Extend `openapi.yml` for new REST endpoints.
- [ ] Add provided `google-services.json` under `app/`.
- [x] Change `applicationId` to `com.labpro.mad`.

## Map Extended

- [x] Replace current favorite-location flow with marked-location records containing name, description, coordinates, and timestamps.
- [x] Add Room entities/DAO for marked locations and marked-location photos.
- [x] Store marked-location photos in app filesystem storage.
- [x] Delete all associated photo files when a marked location is deleted.
- [x] Add camera and gallery flows for marked-location photos.
- [x] Add edit and delete UI for marked-location detail.
- [x] Add Google Maps navigation action from long-press/marked-location bottom sheet.
- [x] Move WebSocket presence sending out of `MapViewModel` so location can be sent while app is closed.
- [x] Enforce the location-sharing privacy toggle before sending presence.

## Notifications

- [x] Add local notification preference state on Profile.
- [x] Add API methods and repository methods for all notification endpoints.
- [x] Add Family Detail bottom sheet for manual family notification and quick greetings.
- [x] Add Firebase Messaging Gradle/plugin setup once `google-services.json` is available.
- [x] Add `FirebaseMessagingService` for token refresh and received messages.
- [x] Subscribe/unsubscribe the FCM token when the Profile notification toggle changes.
- [x] Add Android 13+ notification permission request.
- [x] Display incoming notifications while foregrounded/backgrounded/terminated.
- [x] Suppress notification display when local notification preference is disabled.

## Profile

- [x] Display remote profile photo on the Profile page.
- [x] Upload profile photo from gallery.
- [x] Upload profile photo from camera capture.
- [x] Validate profile photo MIME type and 500 KB max size before upload.
- [x] Add Profile toggles for notifications and location sharing.
- [x] Render `profileImageUrl` for family/member rows wherever users are shown.
- [x] Add Profile entry points for Analytics and Customize Pin if bonus is implemented.

## Share Family

- [x] Add `nimons360://family/<family_id>?code=ABC123` deep-link intent filter.
- [x] Handle incoming family deep links in `MainActivity`/NavGraph.
- [x] Add Share Family Link button on Family Detail for current members.
- [x] Open Android Sharesheet with the deep link and a share message.
- [x] Ensure non-members can join from the deep-linked Family Detail page using the code.

## Responsive

- [x] Audit every screen in portrait and landscape.
- [x] Add landscape XML variants where needed.
- [x] Move transient Compose input state into `rememberSaveable` or ViewModel state.
- [x] Verify field values survive orientation changes.

## OWASP

- [x] Add client-side validation for login, create family, join family, edit profile, marked location, and notification forms.
- [x] Replace regular token storage or document rationale/fix for secure token storage.
- [x] Disable cleartext traffic where possible.
- [x] Reduce sensitive logging from OkHttp logging interceptor.
- [x] Document M4, M8, and M9 analysis plus fixes in `README.md`.

## Bonus

- [x] Instagram Story map screenshot sharing.
- [x] Analytics page with distance stats, graph, recent locations, and CSV export.
- [x] QR-code family sharing from the deep link.
- [x] Custom pin download with foreground service progress notification.
- [x] Accessibility Scanner before/after evidence and fixes in `README.md`.
