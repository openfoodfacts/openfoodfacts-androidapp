BASEDIR=$(CURDIR)/app
OUTPUTDIR=$(BASEDIR)/docs # you may change docs with custom folder name
PACKAGE=openfoodfacts.github.scrachx # write the package name here

html:
    javadoc "$(PACKAGE)" -d "$(OUTPUTDIR)" -encoding UTF-8

.PHONY: html
