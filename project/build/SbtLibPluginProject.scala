import sbt._

class SbtLibPluginProject(info: ProjectInfo) extends PluginProject(info){

	val scala_tools_releases = "scala-tools.releases" at "http://scala-tools.org/repo-releases"
	val scala_tools_snapshots = "scala-tools.snapshots" at "http://scala-tools.org/repo-snapshots"
	val jboss_repo = "jboss.com" at "http://repository.jboss.com/maven2/"
	
	val htmlunit = "net.sourceforge.htmlunit" % "htmlunit" % "2.4"
	val specs_1_6_2 = "org.scala-tools.testing" % "specs" % "1.6.2" % "test"	
	
} 			