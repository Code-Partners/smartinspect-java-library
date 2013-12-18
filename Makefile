LIBRARY  = com/gurock/smartinspect
SOURCES  = $(wildcard $(LIBRARY)/*.java)
VERSION  = $(shell cat ../../../version)

MANIFEST = Manifest.mf
DIST     = dist
OUTPUT   = ../../../dist/bin/java
PACKAGE  = SmartInspect.jar
DLL      = native/SmartInspect.Java.dll
SLN      = native/SmartInspect.Java.sln

# Force the correct version (1.4.2)
JAR      = "/cygdrive/c/Program Files/JavaSDK14/bin/jar.exe"
JAVAC    = "/cygdrive/c/Program Files/JavaSDK14/bin/javac.exe"

all: addversion prepare build clean dll adapters

prepare:
	# Ensure clrf characters.
	@unix2dos compile.bat $(SOURCES) 2> /dev/null
	
dll:
	rm -f $(DLL)
	devenv $(SLN) /Build Release
	mv $(DLL) $(OUTPUT)
	
revert:
	# Revert files which have been changed.
	git checkout -- com/gurock/smartinspect/SmartInspect.java
	git checkout -- ${MANIFEST}

addversion: revert
	# Checking SmartInspect Version
	if [ -z "`grep SIVERSION com/gurock/smartinspect/SmartInspect.java`" ]; \
	then \
		echo "Invalid Source file: Missing SIVERSION tag."; \
		/bin/false; \
	fi
	# Add the version to the library itself (for the TCP client banner)
	sed -ie s/\\\$$SIVERSION/${VERSION}/g com/gurock/smartinspect/SmartInspect.java
	rm -f com/gurock/smartinspect/SmartInspect.javae
	
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
	@echo "Compiling SmartInspect Java Library"
	@${JAVAC} -g:none -d ${DIST} ${SOURCES}
	@echo "Packaging SmartInspect Java Library"
	@${JAR} cfm ${PACKAGE} ${MANIFEST} -C ${DIST} .
	mv ${PACKAGE} ${OUTPUT}

clean:
	rm -rf ${DIST}

count:
	@echo -n "src/libs/java: "
	@wc -l ${SOURCES} | awk '/total/ { print $$1; }'

adapters:
	make -C jdk
