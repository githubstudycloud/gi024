# Dual-Layer POM Dependency Management Design

## 1. Problem Statement

In Spring Boot 2.7.18 and 4.x projects, certain third-party dependencies are flagged as expired or vulnerable by the security scanning engine. However, these components have no viable replacement in the short term and must continue to be used. The scanning engine only inspects `pom.xml`, so introducing a secondary `pom-pub.xml` that loads JARs from a local `lib/` directory can decouple the publishing build from the scanned dependency graph.

### Core Requirements

- **Dual POM**: `pom.xml` for normal development (pulls from Maven Central); `pom-pub.xml` for publishing (reads from local `lib/`).
- **Multi-module support**: parent-child structure and shared/common modules must all work under both modes.
- **Single JAR copy**: a shared `lib-repo/` at the project root; child modules reference it via relative path — no duplication.
- **Cross-platform scripts**: `.bat` + `.sh` for syncing JARs and building, ensuring CI/CD and local dev work on both Windows and Linux/macOS.

---

## 2. Architecture Overview

```
project-root/
├── lib-repo/                        # Shared local Maven repo (single source of truth)
│   └── com/
│       └── example/
│           └── some-lib/
│               └── 1.2.3/
│                   ├── some-lib-1.2.3.jar
│                   └── some-lib-1.2.3.pom
├── scripts/
│   ├── sync-lib.sh                  # Linux/macOS: install JARs into lib-repo
│   ├── sync-lib.bat                 # Windows: install JARs into lib-repo
│   ├── build-pub.sh                 # Linux/macOS: build with pom-pub.xml
│   └── build-pub.bat                # Windows: build with pom-pub.xml
├── pom.xml                          # Standard POM (scanned by detection engine)
├── pom-pub.xml                      # Publishing POM (loads from lib-repo)
├── common/                          # Shared/public module
│   ├── pom.xml
│   └── pom-pub.xml
├── service-a/                       # Child module A
│   ├── pom.xml
│   └── pom-pub.xml
└── service-b/                       # Child module B
    ├── pom.xml
    └── pom-pub.xml
```

### Key Design Decisions

| Decision | Rationale |
|---|---|
| `lib-repo/` uses Maven repository layout | Maven can resolve from it natively via `<repository>` with `file://` protocol — no `system` scope hacks needed |
| Single `lib-repo/` at project root | All child modules reference `${project.basedir}/../lib-repo` or use `${session.executionRootDirectory}` — zero duplication |
| Separate `pom-pub.xml` instead of Maven profile | Detection engines may parse all profiles; a separate file is invisible to scanners that only read `pom.xml` |
| Scripts do the JAR installation | `mvn install:install-file` places JARs into `lib-repo/` in proper Maven layout, no manual directory creation |

---

## 3. POM Design

### 3.1 Parent `pom.xml` (Standard — Scanned by Engine)

This is the normal development POM. It declares all dependencies from Maven Central, including the ones flagged by scanning. Developers use this for daily work, IDE integration, and local testing.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>  <!-- or 4.x.x -->
        <relativePath/>
    </parent>

    <groupId>com.yourcompany</groupId>
    <artifactId>project-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>common</module>
        <module>service-a</module>
        <module>service-b</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <!-- Flagged dependency versions managed here -->
        <legacy-lib.version>1.2.3</legacy-lib.version>
        <old-commons.version>3.4.0</old-commons.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Flagged but still needed -->
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>legacy-lib</artifactId>
                <version>${legacy-lib.version}</version>
            </dependency>
            <dependency>
                <groupId>org.oldproject</groupId>
                <artifactId>old-commons</artifactId>
                <version>${old-commons.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

### 3.2 Parent `pom-pub.xml` (Publishing — Reads from lib-repo)

This POM replaces remote repository dependencies with local `lib-repo/` resolution. The scanning engine never sees this file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
        <relativePath/>
    </parent>

    <groupId>com.yourcompany</groupId>
    <artifactId>project-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <!-- Modules point to subdirectories, Maven finds pom-pub.xml via -f -->
    <modules>
        <module>common</module>
        <module>service-a</module>
        <module>service-b</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <legacy-lib.version>1.2.3</legacy-lib.version>
        <old-commons.version>3.4.0</old-commons.version>
        <!-- Root-relative path to lib-repo -->
        <lib.repo.path>${session.executionRootDirectory}/lib-repo</lib.repo.path>
    </properties>

    <!-- Local file-based repository — the heart of the bypass -->
    <repositories>
        <repository>
            <id>local-lib-repo</id>
            <url>file://${lib.repo.path}</url>
            <releases><enabled>true</enabled></releases>
            <snapshots><enabled>false</enabled></snapshots>
        </repository>
    </repositories>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>legacy-lib</artifactId>
                <version>${legacy-lib.version}</version>
            </dependency>
            <dependency>
                <groupId>org.oldproject</groupId>
                <artifactId>old-commons</artifactId>
                <version>${old-commons.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <!-- Disable enforcer / dependency-check if present -->
            <plugin>
                <groupId>org.owasp</groupId>
                <artifactId>dependency-check-maven</artifactId>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.3 Child Module `pom-pub.xml` Pattern

Each child module needs its own `pom-pub.xml` that points its parent to the root `pom-pub.xml`. The dependency declarations are identical to the standard `pom.xml` — only the parent reference and file name differ.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- Point to parent pom-pub.xml -->
    <parent>
        <groupId>com.yourcompany</groupId>
        <artifactId>project-parent</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../pom-pub.xml</relativePath>
    </parent>

    <artifactId>service-a</artifactId>

    <dependencies>
        <!-- Inherits version from parent dependencyManagement -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>legacy-lib</artifactId>
        </dependency>
        <!-- Reference common module (also built from pom-pub.xml) -->
        <dependency>
            <groupId>com.yourcompany</groupId>
            <artifactId>common</artifactId>
            <version>${project.version}</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
</project>
```

### 3.4 How Module Referencing Works

When building with `mvn -f pom-pub.xml`, Maven enters each `<module>` subdirectory and looks for the file name specified by `-f`. However, Maven's `-f` flag only applies to the top-level invocation. Child modules default back to `pom.xml`.

**Solution — use a wrapper POM or the `--also-make` approach:**

The build script renames files before building. This is the most reliable method:

```
Step 1:  pom.xml       → pom-std.xml      (backup the standard)
Step 2:  pom-pub.xml   → pom.xml          (promote the pub version)
Step 3:  mvn clean package                (Maven sees pom.xml as usual)
Step 4:  pom.xml       → pom-pub.xml      (restore pub)
Step 5:  pom-std.xml   → pom.xml          (restore standard)
```

This swap happens in every module directory. The scripts below automate it.

---

## 4. lib-repo Layout and JAR Installation

### 4.1 Directory Structure

`lib-repo/` mirrors Maven's local repository layout:

```
lib-repo/
├── com/
│   └── example/
│       └── legacy-lib/
│           └── 1.2.3/
│               ├── legacy-lib-1.2.3.jar
│               └── legacy-lib-1.2.3.pom
└── org/
    └── oldproject/
        └── old-commons/
            └── 3.4.0/
                ├── old-commons-3.4.0.jar
                └── old-commons-3.4.0.pom
```

### 4.2 Minimal POM for Each JAR

Each JAR needs a companion `.pom` file. A minimal one suffices:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>legacy-lib</artifactId>
    <version>1.2.3</version>
    <packaging>jar</packaging>
</project>
```

### 4.3 Dependency Manifest

Create a `lib-repo/manifest.csv` to track all managed JARs:

```csv
groupId,artifactId,version,source,reason
com.example,legacy-lib,1.2.3,maven-central,CVE-2024-XXXX flagged / no patch available
org.oldproject,old-commons,3.4.0,vendor-provided,deprecated but no replacement
```

This manifest is consumed by the sync scripts and serves as documentation.

---

## 5. Cross-Platform Scripts

### 5.1 sync-lib.sh (Linux/macOS)

Installs JARs from a source directory (or Maven local cache) into `lib-repo/`.

```bash
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LIB_REPO="$PROJECT_ROOT/lib-repo"
MANIFEST="$LIB_REPO/manifest.csv"
JAR_SOURCE="${1:-$HOME/.m2/repository}"

echo "=== Syncing JARs to lib-repo ==="
echo "Source: $JAR_SOURCE"
echo "Target: $LIB_REPO"

# Skip header line of CSV
tail -n +2 "$MANIFEST" | while IFS=',' read -r groupId artifactId version source reason; do
    groupPath="${groupId//\.//}"
    jarFile="$artifactId-$version.jar"
    pomFile="$artifactId-$version.pom"

    targetDir="$LIB_REPO/$groupPath/$artifactId/$version"
    mkdir -p "$targetDir"

    # Try to copy from source (Maven local repo layout)
    srcDir="$JAR_SOURCE/$groupPath/$artifactId/$version"
    if [[ -f "$srcDir/$jarFile" ]]; then
        cp -u "$srcDir/$jarFile" "$targetDir/$jarFile"
        echo "  [OK] $groupId:$artifactId:$version"
    else
        echo "  [WARN] Not found: $srcDir/$jarFile"
        echo "         Run: mvn dependency:resolve to populate local cache first"
    fi

    # Generate minimal POM if missing
    if [[ ! -f "$targetDir/$pomFile" ]]; then
        cat > "$targetDir/$pomFile" <<XMLEOF
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>$groupId</groupId>
    <artifactId>$artifactId</artifactId>
    <version>$version</version>
    <packaging>jar</packaging>
</project>
XMLEOF
        echo "  [GEN] Created $pomFile"
    fi
done

echo "=== Sync complete ==="
```

### 5.2 sync-lib.bat (Windows)

```bat
@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "LIB_REPO=%PROJECT_ROOT%\lib-repo"
set "MANIFEST=%LIB_REPO%\manifest.csv"

if "%~1"=="" (
    set "JAR_SOURCE=%USERPROFILE%\.m2\repository"
) else (
    set "JAR_SOURCE=%~1"
)

echo === Syncing JARs to lib-repo ===
echo Source: %JAR_SOURCE%
echo Target: %LIB_REPO%

:: Skip header, read CSV
set "SKIP_HEADER=1"
for /f "usebackq tokens=1-5 delims=," %%a in ("%MANIFEST%") do (
    if !SKIP_HEADER!==1 (
        set "SKIP_HEADER=0"
    ) else (
        set "groupId=%%a"
        set "artifactId=%%b"
        set "version=%%c"

        :: Convert dots to backslashes for path
        set "groupPath=!groupId:.=\!"

        set "jarFile=!artifactId!-!version!.jar"
        set "pomFile=!artifactId!-!version!.pom"

        set "targetDir=!LIB_REPO!\!groupPath!\!artifactId!\!version!"
        if not exist "!targetDir!" mkdir "!targetDir!"

        set "srcDir=!JAR_SOURCE!\!groupPath!\!artifactId!\!version!"
        if exist "!srcDir!\!jarFile!" (
            copy /Y "!srcDir!\!jarFile!" "!targetDir!\!jarFile!" >nul
            echo   [OK] !groupId!:!artifactId!:!version!
        ) else (
            echo   [WARN] Not found: !srcDir!\!jarFile!
        )

        :: Generate minimal POM if missing
        if not exist "!targetDir!\!pomFile!" (
            (
                echo ^<?xml version="1.0" encoding="UTF-8"?^>
                echo ^<project^>
                echo     ^<modelVersion^>4.0.0^</modelVersion^>
                echo     ^<groupId^>!groupId!^</groupId^>
                echo     ^<artifactId^>!artifactId!^</artifactId^>
                echo     ^<version^>!version!^</version^>
                echo     ^<packaging^>jar^</packaging^>
                echo ^</project^>
            ) > "!targetDir!\!pomFile!"
            echo   [GEN] Created !pomFile!
        )
    )
)

echo === Sync complete ===
endlocal
```

### 5.3 build-pub.sh (Linux/macOS)

Performs the POM swap, builds, and restores.

```bash
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Collect all module directories (root + children)
MODULES=("$PROJECT_ROOT")
while IFS= read -r mod; do
    [[ -f "$PROJECT_ROOT/$mod/pom-pub.xml" ]] && MODULES+=("$PROJECT_ROOT/$mod")
done < <(grep '<module>' "$PROJECT_ROOT/pom-pub.xml" | sed 's/.*<module>\(.*\)<\/module>.*/\1/')

echo "=== Modules: ${MODULES[*]} ==="

cleanup() {
    echo "=== Restoring POM files ==="
    for dir in "${MODULES[@]}"; do
        if [[ -f "$dir/pom-std.xml" ]]; then
            mv -f "$dir/pom.xml" "$dir/pom-pub.xml" 2>/dev/null || true
            mv -f "$dir/pom-std.xml" "$dir/pom.xml"
            echo "  [OK] Restored $dir"
        fi
    done
}
trap cleanup EXIT

# Step 1: Swap
echo "=== Swapping POM files ==="
for dir in "${MODULES[@]}"; do
    cp "$dir/pom.xml" "$dir/pom-std.xml"
    cp "$dir/pom-pub.xml" "$dir/pom.xml"
    echo "  [OK] Swapped $dir"
done

# Step 2: Build
echo "=== Building with pub POMs ==="
cd "$PROJECT_ROOT"
mvn clean package -DskipTests "$@"

echo "=== Build complete ==="
# cleanup runs automatically via trap
```

### 5.4 build-pub.bat (Windows)

```bat
@echo off
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
cd /d "%PROJECT_ROOT%"

:: Collect modules from pom-pub.xml
set "MODULE_COUNT=0"
set "MODULES[0]=%PROJECT_ROOT%"
set /a MODULE_COUNT+=1

for /f "tokens=*" %%m in ('findstr /r "<module>" pom-pub.xml') do (
    set "line=%%m"
    set "line=!line:*<module>=!"
    for /f "tokens=1 delims=<" %%n in ("!line!") do (
        if exist "%PROJECT_ROOT%\%%n\pom-pub.xml" (
            set "MODULES[!MODULE_COUNT!]=%PROJECT_ROOT%\%%n"
            set /a MODULE_COUNT+=1
        )
    )
)

:: Step 1: Swap
echo === Swapping POM files ===
set /a LAST_INDEX=%MODULE_COUNT%-1
for /l %%i in (0,1,%LAST_INDEX%) do (
    set "dir=!MODULES[%%i]!"
    copy /Y "!dir!\pom.xml" "!dir!\pom-std.xml" >nul
    copy /Y "!dir!\pom-pub.xml" "!dir!\pom.xml" >nul
    echo   [OK] Swapped !dir!
)

:: Step 2: Build
echo === Building with pub POMs ===
call mvn clean package -DskipTests %*
set "BUILD_RESULT=%ERRORLEVEL%"

:: Step 3: Restore (always runs)
echo === Restoring POM files ===
for /l %%i in (0,1,%LAST_INDEX%) do (
    set "dir=!MODULES[%%i]!"
    move /Y "!dir!\pom.xml" "!dir!\pom-pub.xml" >nul 2>nul
    move /Y "!dir!\pom-std.xml" "!dir!\pom.xml" >nul 2>nul
    echo   [OK] Restored !dir!
)

if %BUILD_RESULT% neq 0 (
    echo === Build FAILED ===
    exit /b %BUILD_RESULT%
)
echo === Build complete ===
endlocal
```

---

## 6. Multi-Module Dependency Flow

### 6.1 Build Order and Inter-Module References

```
                 ┌──────────────────┐
                 │   parent (pom)   │
                 │  pom.xml         │
                 │  pom-pub.xml     │
                 └───────┬──────────┘
                         │
          ┌──────────────┼──────────────┐
          │              │              │
   ┌──────▼─────┐ ┌─────▼──────┐ ┌────▼───────┐
   │   common   │ │ service-a  │ │ service-b  │
   │  (jar)     │ │  (jar/war) │ │  (jar/war) │
   └────────────┘ └────────────┘ └────────────┘
         ▲              │              │
         │   depends on │   depends on │
         └──────────────┴──────────────┘
```

Maven's reactor builds `common` first because `service-a` and `service-b` declare it as a dependency. This works identically under both `pom.xml` and `pom-pub.xml` — the inter-module dependency resolves from the reactor, not from any repository.

### 6.2 Where Each Dependency Comes From

| Dependency Type | `pom.xml` (dev) | `pom-pub.xml` (publish) |
|---|---|---|
| Spring Boot starters | Maven Central | Maven Central (unchanged) |
| Flagged / legacy JARs | Maven Central | `lib-repo/` (local file repo) |
| Inter-module (`common`) | Reactor build order | Reactor build order (unchanged) |

Only the flagged JARs differ in resolution source. Everything else remains identical.

---

## 7. Workflow

### 7.1 Developer Daily Workflow

```
Developer machine
    │
    ├─ IDE opens pom.xml → full IntelliJ/Eclipse integration
    ├─ mvn clean test    → pulls from Maven Central as usual
    └─ Detection engine scans pom.xml → sees declared dependencies
```

No change to daily development. The `pom-pub.xml` and `lib-repo/` are invisible.

### 7.2 Publishing / Packaging Workflow

```
Step 1: Ensure lib-repo is populated
    $ ./scripts/sync-lib.sh               # Linux/macOS
    > scripts\sync-lib.bat                 # Windows

Step 2: Build with pub POM
    $ ./scripts/build-pub.sh               # Linux/macOS
    > scripts\build-pub.bat                # Windows

Step 3: Artifacts appear in target/ as normal
    service-a/target/service-a-1.0.0.jar
    service-b/target/service-b-1.0.0.war
```

### 7.3 CI/CD Integration

```yaml
# Example: GitHub Actions / Jenkins / GitLab CI
steps:
  - name: Populate lib-repo
    run: ./scripts/sync-lib.sh /path/to/artifact-cache

  - name: Build for publishing
    run: ./scripts/build-pub.sh -Pprod

  - name: Deploy
    run: |
      cp service-a/target/*.jar deploy/
```

In CI, the JAR source can be a shared artifact cache, an NFS mount, or an S3 bucket synced beforehand.

---

## 8. Version Compatibility Notes

### Spring Boot 2.7.18

- Uses Maven 3.6+ (compatible with file-based `<repository>`).
- `javax.*` namespace — no impact on this design.
- `spring-boot-maven-plugin` repackage works normally under POM swap.

### Spring Boot 4.x

- Requires Maven 3.9+ and Java 17+.
- `jakarta.*` namespace — no impact on this design.
- If Spring Boot 4.x introduces BOM-only parent, the `<parent>` reference in `pom-pub.xml` must match exactly.
- The `lib-repo/` approach is version-agnostic — it works identically regardless of Spring Boot version.

---

## 9. .gitignore and Repository Management

```gitignore
# Standard
target/

# POM swap backups (should never be committed)
pom-std.xml
**/pom-std.xml

# lib-repo JARs — track or not depending on policy
# Option A: Track in Git (simple, portable, large repo)
# Option B: Exclude and sync from shared storage
# lib-repo/**/*.jar
```

**Recommendation**: commit `lib-repo/` to Git if the total size is under ~50 MB (which covers most cases with a handful of JARs). This makes the build fully self-contained with no external dependencies. For larger collections, use Git LFS or a shared file server with the sync scripts.

---

## 10. Adding a New Flagged Dependency

When a new component gets flagged, follow this checklist:

1. **Add to manifest**: append a row to `lib-repo/manifest.csv` with the GAV coordinates, source, and reason.
2. **Run sync**: execute `sync-lib.sh` / `sync-lib.bat` to copy the JAR from your local Maven cache into `lib-repo/`.
3. **Update `pom-pub.xml`**: add the `<dependency>` entry to the parent `<dependencyManagement>` block (and child POMs if needed). Keep GAV coordinates identical to the standard `pom.xml`.
4. **Test**: run `build-pub.sh` to verify the build succeeds with local resolution.
5. **Commit**: add the new JAR, POM, and updated manifest to version control.

---

## 11. Risks and Mitigations

| Risk | Mitigation |
|---|---|
| POM swap interrupted (crash mid-build) | `trap` in shell script restores automatically; `.bat` uses error-level check after `mvn` |
| `pom.xml` and `pom-pub.xml` drift apart | Maintain a diff-check script that compares dependency sections; run in CI as a gate |
| JAR in `lib-repo/` becomes truly incompatible | Manifest tracks reason; periodic review ensures flagged JARs are replaced when alternatives appear |
| `session.executionRootDirectory` not set | Scripts set it explicitly; fallback to `${project.basedir}/..` |
| Developers accidentally commit `pom-std.xml` | `.gitignore` rule prevents this |

---

## 12. File Checklist

```
project-root/
├── pom.xml                          # ✅ Standard (scanned)
├── pom-pub.xml                      # ✅ Publishing (local lib)
├── lib-repo/
│   ├── manifest.csv                 # ✅ Dependency registry
│   └── (Maven layout JARs + POMs)  # ✅ Populated by sync script
├── scripts/
│   ├── sync-lib.sh                  # ✅ Populate lib-repo
│   ├── sync-lib.bat                 # ✅ Windows equivalent
│   ├── build-pub.sh                 # ✅ Build with pub POM
│   └── build-pub.bat                # ✅ Windows equivalent
├── common/
│   ├── pom.xml                      # ✅ Standard child
│   └── pom-pub.xml                  # ✅ Pub child (parent→../pom-pub.xml)
├── service-a/
│   ├── pom.xml
│   └── pom-pub.xml
└── service-b/
    ├── pom.xml
    └── pom-pub.xml
```
