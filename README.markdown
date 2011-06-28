java-config
-----------
Java configuration option command line tool.  This does for java what similar tools
for packages such as freetype (freetype-config) provide.  I wrote this so that I could
find what I needed quickly to build JNI libraries for all unixish platforms.

Using
-----
Copy the java-config.jar file into your project.  Run it as "java -jar java-config.jar" with the
correct java executable and it will report vital statistics about your installation including
cflags and ldflags for jni linking.

Syntax
------
	Syntax: java -jar java-config.jar [FLAGS]
	
	Flags:
		--home				Java Home
		--jdkhome			JDK home (discovered by heuristic)
		--arch				JRE architecture (ie. amd64)
		--version			Java version
		--vmversion			Virtual machine version
		--specversion			Java specification version (ie. 1.6)
		--runtimeversion		Runtime version
		--osversion			Operating System version
		--osname			Operating System name (ie. Linux)
		--datamodel			JRE data model (ie. 32 or 64)
		--endian			System endian-ness (ie. little or big)
		--cflags			CFLAGS to use when compiling a JNI library
		--ldflags			LDFLAGS to use when linking to the jvm
		
Example Usage
-------------
I wrote this tool primarily to aid in building JNI libraries.  All of the other options are present because they are easy to get at and I was forever writing little one liner java programs to pull them out of the system properties.

Here is an example Linux makefile for building a shared library:

	JAVA_CMD ?= java
	CFLAGS ?= -O3
	FREETYPE_CFLAGS ?= $(shell freetype-config --cflags)
	JAVA_CFLAGS ?= $(shell $(JAVA_CMD) -jar java-config.jar --cflags)


	libmapnik-jni.so: mapnikjni.cpp mapnikjni.h
		g++ -o libmapnik-jni.so \
			-fPIC -shared \
			$(CFLAGS) \
			$(FREETYPE_CFLAGS) \
			$(JAVA_CFLAGS) \
			mapnikjni.cpp \
			$(LDFLAGS) \
			-lmapnik2

And here is an example OSX makefile for building a jnilib:
	JAVA_CMD ?= java
	CFLAGS ?= -O3
	FREETYPE_CFLAGS ?= $(shell freetype-config --cflags)
	JAVA_CFLAGS ?= $(shell $(JAVA_CMD) -jar java-config.jar --cflags)


	libmapnik-jni.jnilib: mapnikjni.cpp mapnikjni.h
		g++ -dynamiclib -o libmapnik-jni.jnilib \
			$(CFLAGS) \
			$(FREETYPE_CFLAGS) \
			$(JAVA_CFLAGS) \
			mapnikjni.cpp \
			$(LDFLAGS) \
			-lmapnik2 -framework JavaVM

I used to use autoconf or other such craziness for this stuff, but JNI libraries are usually simple enough to require just quick incantations like the above.  Bundling the java-config.jar with your source and using it to discover compilation options on the fly makes it all self-configuring.

