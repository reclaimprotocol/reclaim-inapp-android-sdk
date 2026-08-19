VERSION := $(shell cat version)
ANDROID_DIST_DIR := $(shell pwd)/dist/library/$(VERSION)/repo

# The Dokka bundled with AGP 9.1 (used by withJavadocJar) can't parse JDK 25+ version
# strings and fails javaDocReleaseGeneration, so build on a JDK 17-21 like jitpack does.
BUILD_JAVA_HOME := $(shell \
	for candidate in "$$JAVA_HOME" \
		"$$(/usr/libexec/java_home -v 21 2>/dev/null)" \
		"$$(/usr/libexec/java_home -v 17 2>/dev/null)" \
		/opt/homebrew/opt/openjdk@21 /opt/homebrew/opt/openjdk@17 \
		/Library/Java/JavaVirtualMachines/*/Contents/Home; do \
		[ -x "$$candidate/bin/java" ] || continue; \
		major=`"$$candidate/bin/java" -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -1`; \
		[ -n "$$major" ] && [ "$$major" -ge 17 ] && [ "$$major" -le 21 ] || continue; \
		echo "$$candidate"; exit 0; \
	done)

.PHONY: build check_jdk check_aar_metadata

check_jdk:
ifeq ($(strip $(BUILD_JAVA_HOME)),)
	$(error No JDK 17-21 found. Install one (e.g. `brew install openjdk@21`) or point JAVA_HOME at it)
endif
	@echo "Using JDK at $(BUILD_JAVA_HOME)"

# compileSdk is baked into the published AAR as minCompileSdk, and consumers fail
# checkDebugAarMetadata unless they compile against at least that level. Compiling against 37
# requires AGP 9.1.1+, which no stable React Native release ships, so a 37 here makes the SDK
# uninstallable for RN hosts. Keep in sync with the add-to-app module's COMPILE_SDK.
MAX_MIN_COMPILE_SDK := 36

build: check_jdk
	@echo "Building version $(VERSION)"
	rm -rf $(ANDROID_DIST_DIR)
	JAVA_HOME=$(BUILD_JAVA_HOME) INAPP_SDK_VERSION=$(VERSION) ./gradlew clean assemble publishToMavenLocal -xtest -xlint -Pgroup=org.reclaimprotocol -Pversion=$(VERSION) -Dmaven.repo.local=$(ANDROID_DIST_DIR)
	@$(MAKE) check_aar_metadata
	@echo "Build completed successfully"
	@echo "You can now use the following dependency in your project:"
	@echo "implementation 'org.reclaimprotocol:inapp_sdk:$(VERSION)'"
	@echo "To use the SDK in your project, add the following to your settings.gradle file:"
	@echo "maven { url '$(ANDROID_DIST_DIR)' }"

# Fail the build if any published AAR demands a compileSdk above MAX_MIN_COMPILE_SDK.
check_aar_metadata:
	@violations=""; \
	for aar in `find $(ANDROID_DIST_DIR) -name '*.aar'`; do \
		found=`unzip -p "$$aar" META-INF/com/android/build/gradle/aar-metadata.properties 2>/dev/null | sed -n 's/^minCompileSdk=//p'`; \
		if [ -n "$$found" ] && [ "$$found" -gt "$(MAX_MIN_COMPILE_SDK)" ]; then \
			violations="$$violations\n  $$found  $$aar"; \
		fi; \
	done; \
	if [ -n "$$violations" ]; then \
		echo "ERROR: AARs declare minCompileSdk above $(MAX_MIN_COMPILE_SDK):" >&2; \
		printf "$$violations\n" >&2; \
		echo "Lower compileSdk in library/build.gradle, then rebuild." >&2; \
		exit 1; \
	fi; \
	echo "OK: all AARs declare minCompileSdk <= $(MAX_MIN_COMPILE_SDK)"
gen_verification_meta:
	@echo ./gradlew --write-verification-metadata sha256 assemble
