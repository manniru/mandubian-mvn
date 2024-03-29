/*
 * BuildPomFromDeps 
 * 
 * Groovy Script building all GDATA Poms from the result of dependency analysis 
 * generated by JBOSS TattleTale.
 * Directly and deeply inspired by David Carter who gratefully proposed
 * his Ruby script on his site http://github.com/dcarter/Google-Data-APIs-Mavenized
 * (great idea to use TattleTale to get dependencies!!!)
 * 
 * I decided to rewrite his Ruby script in Groovy because I wanted everything to 
 * be written in the Maven+Java mood: maven generating maven in a summary...
 * 
 * Important Note on "mvn deploy:deploy-file":
 * You need to have the command "mvn" in your system path: I had to use Java.Runtime 
 * to "system-call" directly "mvn" because I never succeeded to call 
 * "mvn deploy:deploy-file" using MavenEmbedder (I never found a way to set the 
 * "-Dfile" option so deploy-file couldn't work)

 * Note on NexusIndexer called from Maven:
 * I wanted to call NexusIndexer directly from GMaven script (I succeeded to do it 
 * in a simple script). But I encountered some Plexus+Classloader compatibility 
 * issues when trying to instantiate a MavenEmbedder by myself in the current GMaven 
 * context and I couldn't find any PlexusContainer in the M2Eclipse MavenEmbedder 
 * context. As I wanted to propose a real GData maven repository asap, I decided 
 * to go straight even if it is less clean.
 */

// def project = [ basedir: ".", build : [ directory : "target" ] ]
// graphviz/dependencies.dot contains all needed dependency information for each JAR.
// Line format: gdata_analytics_2_0 -> gdata_core_1_0;
// creates a map[artifactId] = List(dependency artifactId)
def dotfile= new File(project.build.directory + '/tattletaled/graphviz/dependencies.dot')
def map = [:]
def writer = dotfile.eachLine { 
	def matcher = it =~ /(\w+_\w+)_(\d+_\d+)_?(\w+)?\s?->\s?(\w+_\w+)_(\d+_\d+)_?(\w+)?;/
	matcher.each { 
		def name1 = it[1]?.replaceAll(/_/, '-')
		def version1 = it[2]?.replaceAll(/_/, '.')
		def scope1 = it[3]?.replaceAll(/_/, '.')
		def name2 = it[4]?.replaceAll(/_/, '-')
		def version2 = it[5]?.replaceAll(/_/, '.')
		def scope2 = it[6]?.replaceAll(/_/, '.')

		def ref1 = name1+"-"+version1
		if(scope1)
			ref1 += "-" + scope1
		def ref2 = name2+"-"+version2
		if(scope2)
			ref2 += "-" + scope2

		if(!map[ref1])
			map[ref1] = []
				                           
		map[ref1] << ref2
	}
}

// GDATA Maven Version retrieved from executing POM
def gdataVersion = "${project.properties['gdata.version']}"	
// GDATA Maven GroupId
def gdataGroupId = "com.google.gdata"
	
// creates directory to store generated poms
def pomDirName = "${project.build.directory}/poms"	
def pomDir = new File( pomDirName )
pomDir.mkdirs()
	
// creates directory to store the Maven repository
def repoDirName = "${project.basedir}/repository"	
def repoDir = new File( repoDirName )
repoDir.mkdirs()

// adds jsr305 to gdata-core because for an unknown reason Tattletale doesn't detect this required dependency
map.find{it.key.contains("gdata-core")}?.value<<"jsr305"

// For each Artifact in map, it builds the dependency maven string to put in POM
map.each { artifactId, deps ->
	def depsStr = ""
		deps.each { depArtifactId ->
			def depGroupId = gdataGroupId
			def depVersion = gdataVersion
			def matcher = depArtifactId =~ /(google-collect-)(\d+\.\d+\-\w+)/
			if(matcher.matches())
			{
				depGroupId = "com.google.collections"
				depArtifactId = "google-collections"
				depVersion = matcher[0][2] 
			}
			else if(depArtifactId=="jsr305")
			{
				depGroupId = "com.google.code.findbugs"
				depArtifactId = "jsr305"
				depVersion = "1.3.9"
			}
			depsStr += """\
		<dependency>
			<groupId>$depGroupId</groupId>
			<artifactId>$depArtifactId</artifactId>
			<version>$depVersion</version>
		</dependency>
			"""

		}
	
	// GString of the POM (I love GStrings... thks Groovy ;) )
	def pomContent = """\
<?xml version="1.0" encoding="UTF-8"?>
<project 
	xmlns="http://maven.apache.org/POM/4.0.0" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
	http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>$gdataGroupId</groupId>
	<artifactId>$artifactId</artifactId>
	<version>$gdataVersion</version>
	<name>$artifactId</name>
	<description>jar $artifactId from gdata-java-client project mavenized by mandubian.org using Maven deploy-file + Nexus indexing</description>
	<url>http://code.google.com/p/gdata-java-client/</url>
	<licenses>
		<license>
			<name>Apache 2</name>
			<url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
	    	</license>
	    </licenses>
	<scm>
		<url>http://code.google.com/p/gdata-java-client/source/browse/</url>
	</scm>
	<developers>
		<developer>
			<id>gdata-team</id>
			<name>The Google GData Team</name>
			<url>http://code.google.com/p/gdata-java-client/people/list</url>
			<organization>Google</organization>
		</developer>
	</developers>	

	<dependencies>
$depsStr
	</dependencies>
</project>
"""	

	// writes POM file to target/poms
	File pomFile = new File(pomDir, artifactId+"-"+gdataVersion+".pom")
	if(pomFile.exists()){
		pomFile.renameTo(new File(pomFile.getAbsolutePath()+".backup"))
		pomFile.delete()
	}
	pomFile.write(pomContent)
	
	// launches mvn deploy:deploy-file
	try {
		Runtime runtime = Runtime.getRuntime()			
		Process proc = runtime.exec("""\
			mvn deploy:deploy-file \
				-Dfile=${project.build.directory}/unpacked/gdata/java/lib/${artifactId}.jar \
	        	-DgroupId=${gdataGroupId} \
				-DartifactId=${artifactId} \
				-Dversion=${gdataVersion} \
				-Dpackaging=jar \
				-DgeneratePom=false \
				-Durl=file://${repoDir} \
				-DpomFile=target/poms/${artifactId}-${gdataVersion}.pom \
				""")
					
		BufferedReader stdout = new BufferedReader (
		    new InputStreamReader(proc.getInputStream()));
		while ((s = stdout.readLine()) != null) {
		    println(s);
		}
		
		proc = runtime.exec("""\
				mvn deploy:deploy-file \
					-Dfile=${project.build.directory}/unpacked/gdata/java/lib-src/${artifactId}-src.jar \
		        	-DgroupId=${gdataGroupId} \
					-DartifactId=${artifactId} \
					-Dversion=${gdataVersion} \
					-Dpackaging=jar \
					-DgeneratePom=false \
					-Durl=file://${repoDir} \
					-DpomFile=target/poms/${artifactId}-${gdataVersion}.pom \
					-Dclassifier=sources \
					""")
						
		stdout = new BufferedReader (
		    new InputStreamReader(proc.getInputStream()));
		while ((s = stdout.readLine()) != null) {
		    println(s);
		}
	}
	catch(IOException ex){
		ex.printStacktrace();
		// if any exception, brutally exits 
		System.exit(0);
	}
}

