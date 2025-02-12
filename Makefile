VERSION := $(shell cat version)
ANDROID_DIST_DIR := $(shell pwd)/dist/library/$(VERSION)/repo

build:
	echo "Building version $(VERSION)"
	rm -rf $(ANDROID_DIST_DIR)
	./gradlew clean -Pgroup=org.reclaimprotocol -Pversion=$(VERSION) -xtest -xlint assemble -Dmaven.repo.local=$(ANDROID_DIST_DIR) publishToMavenLocal
