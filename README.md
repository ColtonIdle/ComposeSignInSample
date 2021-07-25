# ComposeSignInSample

A foundation/starting point if you want to build a compose application with:

Compose + Hilt + AAC Compose Navigation + AAC ViewModels + a signed in/out user flow

----
This is mostly an experiment to get feedback on how to _actually_ set something like this up that's actually practical to use in the future.

Things that need attention:
1. Going from SignInScreen to HomeScreen via popBackstack doesn't show HomeScreen
2. When on SignInScreen, you can't exit the app with back button press
3. Every time a dev adds a new screen that requires you to be logged in (like HomeScreen) it has the potential for the dev to forget to log the user out. It would be nice to somehow always have every screen have the same logout behavior.
