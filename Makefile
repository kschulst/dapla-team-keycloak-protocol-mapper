.PHONY: default
default: | help

.PHONY: build ## Build the project
build: ## Build the project and install to your local maven repo
	mvn clean install

.PHONY: build-skiptests ## Build the project (skipping tests)
	mvn clean install -Dmaven.test.skip=true

.PHONY: test
test: ## Run tests
	mvn clean test

.PHONY: release-dryrun
release-dryrun: ## Simulate a release in order to detect any issues
	mvn release:prepare release:perform -Darguments="-Dmaven.deploy.skip=true" -DdryRun=true

.PHONY: release
release: ## Release a new version
	git push origin main:release

.PHONY: help
help:
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
