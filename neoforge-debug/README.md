# NeoForge debug module

This is an isolated NeoForge subproject included by the root `settings.gradle`.
It has no dependency on the production Fabric tasks and its output is never
added to the ModWrapper release JAR. The module loads the selected Fabric
artifact as a runtime mod through Sinytra Connector and provides a tiny
NeoForge probe for diagnostics or temporary compatibility hooks.

## Run

1. Build the ModWrapper artifact from the repository root (it embeds all
   version-specific Fabric sub-mods):

   `..\gradlew.bat :ModWrapper:jar`

2. Set exact coordinates for the NeoForge and Connector builds used by the
   target server. They can be supplied in `gradle.properties` or on the command
   line (`-Pneo_version=... -Pconnector_version=...
   -Pconnector_launchpad_version=... -Pconnector_forgified_version=...`).

3. Start a client or server:

   `..\gradlew.bat :neoforge-debug:runClient`

   `..\gradlew.bat :neoforge-debug:runServer`

   Running from the module directory remains supported with
   `..\gradlew.bat -p neoforge-debug runClient`.

## IntelliJ IDEA

Open the repository root as a Gradle project and click **Reload All Gradle
Projects**. `neoforge-debug` then appears as a regular Gradle module alongside
the version modules. The module uses the normal Gradle property resolution;
command-line `-P` values take precedence.
When `neo_version` is empty, IDEA can still index the source and resources, but
NeoForge run tasks cannot start until the coordinates are configured.

The default `fabric_mod_jar` value is the `ModWrapper/build/libs` directory;
the newest wrapper JAR is selected automatically. Before each run, only the
entry matching `project.minecraft_version` is extracted from that wrapper and
staged as `run/neoforge-debug/mods/gca-fabric-debug.jar`. A base value such as
`26.3` also matches generated `26.3-snapshot-*` entries. This avoids Connector
loading the latest nested version. Use `-Pfabric_mod_jar=...` to select a
particular wrapper file. The output JAR of this project contains only the debug
probe, while the Fabric mod and Connector remain external runtime inputs. Put
Connector-compatible dependencies such as Carpet in that `mods` directory as
well.

## Compatibility workflow

Keep each Connector/NeoForge failure as a minimal reproduction. Add temporary
mixins or event listeners under this module, verify the fix in the NeoForge run,
then port the durable behavior to the version-specific Fabric source or submit
the compatibility change upstream. Do not add NeoForge classes to common
sources: Connector still loads the Fabric artifact through Fabric Loader.
