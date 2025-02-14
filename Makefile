VERSION := $(shell cat version)
ANDROID_DIST_DIR := $(shell pwd)/dist/library/$(VERSION)/repo

build:
	@echo "Building version $(VERSION)"
	rm -rf $(ANDROID_DIST_DIR)
	./gradlew clean -Pgroup=org.reclaimprotocol -Pversion=$(VERSION) -xtest -xlint assemble -Dmaven.repo.local=$(ANDROID_DIST_DIR) publishToMavenLocal
	@echo "Build completed successfully"
	@echo "You can now use the following dependency in your project:"
	@echo "implementation 'org.reclaimprotocol:inapp_sdk:$(VERSION)'"
	@echo "To use the SDK in your project, add the following to your settings.gradle file:"
	@echo "maven { url '$(ANDROID_DIST_DIR)' }"
