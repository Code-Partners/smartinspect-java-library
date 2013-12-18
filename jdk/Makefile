LIBRARY  = com/gurock/smartinspect/jdk
SOURCES  = $(wildcard $(LIBRARY)/*.java)
VERSION  = $(shell cat ../../../../version)

MANIFEST = Manifest.mf
DIST     = dist
OUTPUT   = ../../../../dist/bin/java
PACKAGE  = SmartInspect.JDK.jar
DEP      = $(OUTPUT)/SmartInspect.jar

# Force the correct version (1.4.2)
JAR      = "/cygdrive/c/Program Files/JavaSDK14/bin/jar.exe"
JAVAC    = "/cygdrive/c/Program Files/JavaSDK14/bin/javac.exe"

all: addversion prepare build clean

prepare:
	# Ensure clrf characters.
	@unix2dos compile.bat $(SOURCES) 2> /dev/null

revert:
	git checkout -- Manifest.mf
	
addversion: revert
	if [ -z "`grep SIVERSION ${MANIFEST}`" ]; \
	then \
		echo "Invalid Manifest file: Missing SIVERSION tag."; \
		/bin/false; \
	fi
	
	# Add SmartInspect Version to the Manifest
	sed -ie s/\\\$$SIVERSION/${VERSION}/g ${MANIFEST}
	rm -f ${MANIFEST}e

build:
	mkdir -p ${OUTPUT} ${DIST}
	@echo "Compiling SmartInspect JDK Adapter"
	@${JAVAC} -g:none -d ${DIST} ${SOURCES} -classpath $(DEP)
	@echo "Packaging SmartInspect JDK Adapter"
	@${JAR} cfm ${PACKAGE} ${MANIFEST} -C ${DIST} .
	mv ${PACKAGE} ${OUTPUT}

clean:
	rm -rf ${DIST}

count:
	@echo -n "src/libs/java/jdk: "
	@wc -l ${SOURCES} | awk '/total/ { print $$1; }'
