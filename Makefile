build:
	./gradlew clean -Pgroup=org.reclaimprotocol -Pversion=1.0.0 -xtest -xlint assemble -Dmaven.repo.local=$$(pwd)/library/build/repo publishToMavenLocal
