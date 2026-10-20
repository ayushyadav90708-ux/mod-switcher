# PrivateSMPTools (Paper 1.21.11)

A small Paper plugin source project with `/dupe [amount]` and `/smpadmin` utility commands. It does **not** grant OP or conceal activity from server owners.

## Build on Windows
1. Install JDK 21 and Maven.
2. Open Command Prompt in this project folder.
3. Run `mvn clean package`.
4. The JAR should be at `target/PrivateSMPTools-1.0.0.jar`.
5. Put the JAR in the server's `plugins` folder and restart the server.

## Authorization
The config contains `authorized-names: [VyronYT]` as a convenience. For stronger identity verification, replace name-based authorization with the player's UUID in `authorized-uuids`. The player must also have the Bukkit permissions `privatesmptools.dupe` and/or `privatesmptools.admin`; the server owner should grant these explicitly, for example using their permissions plugin. The plugin does not secretly grant permissions.

## Commands
- `/dupe` duplicates one held item.
- `/dupe 16` duplicates 16 of the held item, subject to the configured maximum and available inventory space.
- `/smpadmin creative`
- `/smpadmin survival`
- `/smpadmin vanish`
- `/smpadmin fly`
- `/smpadmin reload`

Paper API artifact version in `pom.xml` targets `1.21.11-R0.1-SNAPSHOT`; building requires internet access to Maven repositories. Verify the Paper API version is available for your server build.

## Build the JAR using GitHub Actions
1. Create a new GitHub repository and upload the **contents** of this project folder (so `pom.xml` is at the repository root).
2. Open the repository's **Actions** tab. If prompted, enable Actions.
3. The `Build Paper Plugin JAR` workflow runs on push. You can also select it and click **Run workflow**.
4. Open the completed workflow run and download the `PrivateSMPTools-JAR` artifact.
5. Extract the downloaded artifact ZIP; it contains `PrivateSMPTools-1.0.0.jar`.

The GitHub workflow builds the source; it does not grant OP or hide plugin activity. The server owner must explicitly grant the relevant permissions.
