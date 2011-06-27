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
		
