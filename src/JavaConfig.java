import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Provide simple discovery of java compilation options similar
 * to package *-config programs installed with a lot of native software.
 * <p>
 * Most of this is derived from the system properties.  An example is here:
 * <pre>
	java.runtime.name=OpenJDK Runtime Environment
	sun.boot.library.path=/usr/lib/jvm/java-6-openjdk/jre/lib/amd64
	java.vm.version=19.0-b09
	java.vm.vendor=Sun Microsystems Inc.
	java.vendor.url=http\://java.sun.com/
	path.separator=\:
	java.vm.name=OpenJDK 64-Bit Server VM
	file.encoding.pkg=sun.io
	sun.java.launcher=SUN_STANDARD
	user.country=US
	sun.os.patch.level=unknown
	java.vm.specification.name=Java Virtual Machine Specification
	user.dir=/home/stella/branches/java-config
	java.runtime.version=1.6.0_20-b20
	java.awt.graphicsenv=sun.awt.X11GraphicsEnvironment
	java.endorsed.dirs=/usr/lib/jvm/java-6-openjdk/jre/lib/endorsed
	os.arch=amd64
	java.io.tmpdir=/tmp
	line.separator=\n
	java.vm.specification.vendor=Sun Microsystems Inc.
	os.name=Linux
	sun.jnu.encoding=UTF-8
	java.library.path=/usr/lib/jvm/java-6-openjdk/jre/lib/amd64/server\:/usr/lib/jvm/java-6-openjdk/jre/lib/amd64\:/usr/lib/jvm/java-6-openjdk/jre/../lib/amd64\:/usr/lib64/xulrunner-addons\:/usr/java/packages/lib/amd64\:/usr/lib/jni\:/lib\:/usr/lib
	java.specification.name=Java Platform API Specification
	java.class.version=50.0
	sun.management.compiler=HotSpot 64-Bit Server Compiler
	os.version=2.6.35-25-generic
	user.home=/home/stella
	user.timezone=America/Los_Angeles
	java.awt.printerjob=sun.print.PSPrinterJob
	file.encoding=UTF-8
	java.specification.version=1.6
	java.class.path=/home/stella/branches/java-config/bin
	user.name=stella
	java.vm.specification.version=1.0
	java.home=/usr/lib/jvm/java-6-openjdk/jre
	sun.arch.data.model=64
	user.language=en
	java.specification.vendor=Sun Microsystems Inc.
	java.vm.info=mixed mode
	java.version=1.6.0_20
	java.ext.dirs=/usr/lib/jvm/java-6-openjdk/jre/lib/ext\:/usr/java/packages/lib/ext
	sun.boot.class.path=/usr/lib/jvm/java-6-openjdk/jre/lib/resources.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/rt.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/sunrsasign.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/jsse.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/jce.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/charsets.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/netx.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/plugin.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/rhino.jar\:/usr/lib/jvm/java-6-openjdk/jre/lib/modules/jdk.boot.jar\:/usr/lib/jvm/java-6-openjdk/jre/classes
	java.vendor=Sun Microsystems Inc.
	file.separator=/
	java.vendor.url.bug=http\://java.sun.com/cgi-bin/bugreport.cgi
	sun.io.unicode.encoding=UnicodeLittle
	sun.cpu.endian=little
	sun.desktop=gnome
	sun.cpu.isalist=
 * </pre>
 * @author stella
 *
 */
public class JavaConfig {
	private String[] args;
	private StringWriter buffer;
	private PrintWriter out;
	
	public JavaConfig(String[] args) {
		this.args=args;
		this.buffer=new StringWriter();
		this.out=new PrintWriter(buffer);
	}
	
	private String readResource(String name) throws IOException {
		InputStream in=getClass().getResourceAsStream(name);
		if (in==null) return null;
		
		StringBuilder buffer=new StringBuilder();
		InputStreamReader reader=new InputStreamReader(in, "UTF-8");
		char[] b=new char[1024];
		for (;;) {
			int r=reader.read(b);
			if (r<0) break;
			buffer.append(b, 0, r);
		}
		
		return buffer.toString();
	}
	
	public void usage() throws IOException {
		String buildNumber=readResource("build.txt");
		if (buildNumber!=null) {
			System.err.println("java-config version " + buildNumber);
		} else {
			System.err.println("java-config unknown version");
		}
		System.err.print(readResource("usage.txt"));
		System.exit(2);
	}
	
	public void error(String msg) {
		System.err.println(msg);
		System.exit(2);
	}
	
	public void reportProp(String prop) {
		String value=System.getProperty(prop);
		if (value==null) {
			error("Expected system property " + prop + " but it was not found");
		}
		
		out.println(value);
	}
	
	private File getJdkHome() {
		String javaHome=System.getProperty("java.home");
		if (javaHome==null) return null;
		File jreDir=new File(javaHome);
		
		// Probe for jre within jdk
		if (!new File(jreDir, "include").isDirectory()) {
			if (jreDir.getName().equals("jre")) {
				return jreDir.getParentFile();
			}		
		} else {
			return jreDir;
		}
		
		return null;
	}
	public void reportJdkHome() {
		File jdkDir=getJdkHome();
		if (jdkDir==null) error("Could not determine JDK location");
		out.println(jdkDir.getAbsolutePath());
	}
	
	public void reportCFlags() {
		File jdkDir=getJdkHome();
		if (jdkDir==null) error("Could not determine JDK location");
		
		StringBuilder flags=new StringBuilder();
		
		File incDir=new File(jdkDir, "include");
		if (!new File(incDir, "jni.h").exists()) error("Could not find jni.h in JDK include directory " + incDir);
		flags.append("-I").append(incDir.getAbsolutePath());
		
		// Scan for the platform specific directory.  This is stupid.
		for (File child: incDir.listFiles()) {
			if (child.isDirectory() && new File(child, "jni_md.h").exists()) {
				flags.append(" -I").append(child.getAbsolutePath());
			}
		}
		
		out.println(flags.toString());
	}

	public void reportLDFlags() throws IOException {
		String prop=System.getProperty("java.library.path");
		if (prop==null) error("Could not find java.library.path");
		String[] paths=prop.split(Pattern.quote(String.valueOf(File.pathSeparatorChar)));
		StringBuilder buffer=new StringBuilder();
		
		Set<String> found=new HashSet<String>();
		for (String path: paths) {
			File abspath=new File(path).getAbsoluteFile();
			if (!abspath.isDirectory()) continue;
			
			String canpath=abspath.getCanonicalPath();
			if (!found.add(canpath)) continue;
			
			if (buffer.length()>0) buffer.append(' ');
			buffer.append("-L").append(canpath);
		}
		
		out.println(buffer.toString());
	}
	
	public void run() throws IOException {
		if (args.length==0) {
			usage();
			System.exit(1);
		}
		for (int i=0; i<args.length; i++) {
			String flag=args[i];
			flag=flag.replaceFirst("^\\-+", "").toLowerCase();
			
			if (flag.equals("home")) reportProp("java.home");
			else if (flag.equals("jdkhome")) reportJdkHome();
			else if (flag.equals("arch")) reportProp("os.arch");
			else if (flag.equals("version")) reportProp("java.version");
			else if (flag.equals("vmversion")) reportProp("java.vm.version");
			else if (flag.equals("specversion")) reportProp("java.specification.version");
			else if (flag.equals("runtimeversion")) reportProp("java.runtime.version");
			else if (flag.equals("osversion")) reportProp("os.version");
			else if (flag.equals("osname")) reportProp("os.name");
			else if (flag.equals("datamodel")) reportProp("sun.arch.data.model");
			else if (flag.equals("endian")) reportProp("sun.cpu.endian");
			else if (flag.equals("language")) reportProp("user.language");
			else if (flag.equals("cflags")) reportCFlags();
			else if (flag.equals("ldflags")) reportLDFlags();
		}
		
		out.flush();
		System.out.print(buffer.toString());
	}
	
	public static void main(String[] args) throws Exception {
		JavaConfig m=new JavaConfig(args);
		m.run();
	}
}
