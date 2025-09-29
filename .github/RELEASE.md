# Creating Releases

To create a new release with APK:

1. Update the version in `app/build.gradle`:
   ```gradle
   versionCode 2
   versionName "1.1"
   ```

2. Commit your changes:
   ```bash
   git add .
   git commit -m "Bump version to 1.1"
   ```

3. Create and push a version tag:
   ```bash
   git tag v1.1
   git push origin v1.1
   ```

4. The GitHub Actions workflow will automatically:
   - Build the release APK
   - Create a GitHub release
   - Upload the APK as a release asset

The release will be available at: https://github.com/kmfoysal06/fuplay/releases