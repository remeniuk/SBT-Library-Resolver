package com.vasilrem.sbtplugins

import sbt._
import org.specs._

class SbtLibPluginSpec extends Specification {

	object SbtLibPluginTest extends DefaultProject(ProjectInfo(new java.io.File(""), None, None)(new ConsoleLogger, null, null)) 
	with SbtLibPlugin{
		override val getLibraryJarNames = List("commons-codec-1.3.jar", "sac-1.3.jar", "spring-security-core-2.0.4.jar", "unknown.jar") 
	}
	
	"3 of 4 jars should be resolved into artifact" in {
		((0, 0) /: SbtLibPluginTest.resolveLibArtifacts){
			(count, artifact) => if(artifact.isRight) (count._1 + 1, count._2) else (count._1, count._2 + 1)
		} must be equalTo((3, 1))
	}
	
	"specs-1.6.2.jar is successfully resolved" in {
		SbtLibPluginTest.resolveArtifactsByJarName("specs-1.6.2.jar")(0) must be equalTo(ResolvedArtifact("org.scala-tools.testing", "specs", "1.6.2"))
	}
	
	"Find artifacts for class com.gargoylesoftware.htmlunit.WebClient" in {	
		SbtLibPluginTest.resolveArtifactByClass("com.gargoylesoftware.htmlunit.WebClient", 1).size must be greaterThan(50)
	}
  
}