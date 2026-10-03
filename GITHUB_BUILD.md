# Build on GitHub

This repository is configured for GitHub Actions.

1. Upload/push the entire project folder to a GitHub repository.
2. Open the **Actions** tab.
3. Run **Build Note Block Songs** (or push a commit to trigger it).
4. When it finishes, open the workflow run and download the **note-block-songs-jar** artifact.
5. Use the JAR from `build/libs/` for Minecraft 1.21.11.

The workflow installs Java 21 and Gradle 9.2.1, so GitHub does not depend on a missing `gradle-wrapper.jar`.

For local builds, install Gradle 9.2.1 and run:

```text
gradle clean build
```

The JAR is generated in `build/libs/`.
