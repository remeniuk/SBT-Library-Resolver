Possibility to have unmanaged dependencies under `/lib` folder greatly increases development speed - you don't have to go to project definition every time you need to add another artifact. But bunch of JARs laying under /lib can turn into over time into chaos. You may forget (or don't know) what Artifact (JAR/POM) corresponds to unspecified JAR.

Another problem: you may neither have an idea, what artifact you need, nor have a JAR by the hand, and only know the classname. The most of modern IDEs with good Maven integration can suggest you what libraries in the known repositories contain the class you're looking for. Otherwise, search engines like **[http://www.jarvana.com/jarvana](http://www.jarvana.com/jarvana)** come to the rescue.
#########
With **SBT-Library-Resolver** you shouldn't leave SBT console to resolve your dependencies:

**1.**  Find artifacts that correspond to the JAR, specifying JAR name (input is auto-completed with the names of JARs under /lib):

	spring-2.5.jar                           spring-aop-2.0.8.jar
	spring-context-2.5.jar                   spring-context-support-2.5.jar
	spring-modules-validation-0.7.jar        spring-security-core-2.0.5.RELEASE.jar
	spring-web-2.5.jar                       spring-webmvc-2.5.jar
	> resolve-lib spring-context-
	> resolve-lib spring-context-2.5.jar
	[info]
	[info] ==================================================
	[info] ==============SUCCESSFULLY RESOLVED===============
	[info] ==================================================
	[info]  "org.springframework" % "spring-context" % "2.5"
	[info]
  
**2.** Resolve all JARs under /lib

	> resolve-libs
	[info]
	[info] == resolve-libs ==
	[info]
	[info] ==================================================
	[info] ==============SUCCESSFULLY RESOLVED===============
	[info] ==================================================
	[info]  "commons-cli" % "commons-cli" % "1.0"
	[info]  "org.hibernate" % "hibernate-validator" % "4.0.2.GA"
	[info]  "commons-beanutils" % "commons-beanutils" % "1.7.0"
	[info]  "jstl" % "jstl" % "1.0"
	[info]
	[info] ==================================================
	[info] ================FAILED TO RESOLVE=================
	[info] ==================================================
	[info]  jersey-client-1.1.5.1.jar
	[info]  mgm-batch_mailer-1.0-SNAPSHOT.jar

**3.** Find artifacts by classname

	> resolve-class Scalate
	[info] Only 100 artifacts will be displayed per request. If 
	more artifacts are found, go to another page: resolve-class 
	<class name> <page of results>
	[info]
	[info] Displaying artifacts: page 1 of 1
	[info]
	[info] ==================================================
	[info] ==============SUCCESSFULLY RESOLVED===============
	[info] ==================================================
	[info]  "org.fusesource.scalate" % "scalate-tool" % "1.2, 1.1"
	[info]

## Configuration

Add **SBT-Library-Resolver** to the plugin configuration of your project(e.g., `project\plugins\Plugins.scala`):

	import sbt._

	class Plugins(info: ProjectInfo) extends PluginDefinition(info) {

	  val libResolverRepo = "LibResolver Github Repo" at "http://remeniuk.github.com/maven/"
	  val libResolver = "com.vasilrem" % "sbt-libresolver-plugin" % "0.1"

	}

Mix `com.vasilrem.sbtplugins.SbtLibPlugin` with the project definition (`project\build\<project>.scala`):

	import sbt._
	import com.vasilrem.sbtplugins._

	class SampleProject(info: ProjectInfo) extends DefaultWebProject(info) with SbtLibPlugin{	
	   ...
	}