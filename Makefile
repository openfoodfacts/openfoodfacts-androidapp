# Base directory of the project
BASEDIR := $(CURDIR)/app

# Output directory for generated documentation
OUTPUTDIR := $(BASEDIR)/docs

# Package for which Javadoc is to be generated
PACKAGE := openfoodfacts.github.scrachx

# Javadoc target
html:
	@echo "Generating Javadoc for package: $(PACKAGE)"
	@echo "Output directory: $(OUTPUTDIR)"
	javadoc -sourcepath "$(BASEDIR)/src/main/java" \
	        -subpackages "$(PACKAGE)" \
	        -d "$(OUTPUTDIR)" \
	        -encoding UTF-8 \
	        -charset UTF-8 \
	        -quiet

.PHONY: html
