MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules
SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c
COMMIT_ID := $(shell git rev-parse --short HEAD)

.PHONY: wrapper
wrapper:  ## Generate graddle wrapper https://docs.gradle.org/7.1.1/userguide/gradle_wrapper.html#gradle_wrapper
	gradle wrapper

.PHONY: clean
clean:
	./gradlew clean

.PHONY: build
build:
	./gradlew build

.PHONY: test
test:
	./gradlew test

.PHONY: gradle
gradle:
	./gradlew

.PHONY: tasks
tasks:  ## List available gradle tasks
	./gradlew tasks