package com.vasilrem.sbtplugins

import sbt._
import com.gargoylesoftware.htmlunit.BrowserVersion
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html._
import java.io.BufferedWriter
import java.io.File
import java.net.InetAddress
import java.util.Properties
import org.apache.commons.io.output.FileWriterWithEncoding
import scala.xml.XML
import com.gargoylesoftware.htmlunit.xml.XmlPage
import com.gargoylesoftware.htmlunit.SgmlPage
import scala.actors.Futures._

case class ResolvedArtifact(groupId: String, artifactId: String, version: String, repositoryUrl: String){
	override def toString = """ "%s" %% "%s" %% "%s" at "%s" """.format(groupId, artifactId, version, repositoryUrl)
	def copy(_repositoryUrl: String) = ResolvedArtifact(groupId, artifactId, version, _repositoryUrl)
}

trait SbtLibPlugin extends Project {
	
	def processInWebClient[T](url: String)(request: SgmlPage => T) = {
		val webClient = new WebClient 	
		val result = request(webClient.getPage(url))
		webClient.closeAllWindows
		result
	}	
	
	/**
	* Searches for artifact definotion by jar name at Jarvana.com
	* @return URL of the definition page
	*/
	def searchArtifactDefinitionURL(jarName: String) = 
		processInWebClient("http://www.jarvana.com/jarvana/search?search_type=project&project=%s" format(jarName)){page =>
			try {
				Some((XML.loadString((page.asInstanceOf[HtmlPage].getHtmlElementById("resulttable"):HtmlTable).asXml) \\ "a").
				flatMap(_.attribute("href")).
				map(_.text).
				filter(_.contains("inspect-pom"))(0))
			} catch{case e=> log.debug(e.getMessage); None}
		}	
	
	/**
	* Returns either a classified artifact or error message
	*/
	def classifyArtifact(artifactDefinitionURL: String) = 
		processInWebClient("http://www.jarvana.com%s" format(artifactDefinitionURL)){ artifactDefinition => 
			val artifactLinks = artifactDefinition.getByXPath("//a[@class='repoLink']")
			
			val pomLocation = (try{Some(artifactLinks.get(artifactLinks.size - 1))} catch {case e => None}).
				map(_.asInstanceOf[HtmlAnchor].getAttribute("href"))
			
			val ivyDefinition = XML.loadString(
				artifactDefinition	
				.getByXPath("//pre[@class='pomdisplay']").get(1)
				.asInstanceOf[HtmlPreformattedText]
				.asXml)		

			val artifactAttributes = (ivyDefinition \\ "span")
				.filter(span => (span \ "@class").text == "attributeValue")
				.map(_.text.trim)				
				.map(attribute => attribute.substring(1, attribute.length - 1))
			
			for( artifact <- (artifactAttributes match {
				case Seq(groupId, artifactId, version) => Some(ResolvedArtifact(groupId, artifactId, version, ""))
				case _ => None
			}); pom <- pomLocation) yield artifact.copy(pom.split("/%s/" format(artifact.groupId.split("\\.")(0)))(0))
		}	

	/**
	* Gets list of jar names in /lib folder
	*/	
	def getLibraryJarNames = ("lib" ** "*.jar").get.
		map(_.asFile.getName)
	
	/**
	* Resolves artifact by jar-name
	*/
	def resolveArtifactByJarName(jarName: String) = 
		searchArtifactDefinitionURL(jarName.replace("jar", "pom")).
		flatMap(classifyArtifact) match {
			case Some(artifact) => Right(artifact)
			case None => Left("%s" format(jarName))
		}		
	
	/**
	* Resolves jar in the /lib folder
    */	
	def resolveLibArtifacts = 
		getLibraryJarNames.
		map(jarName => 
				future[Either[String, ResolvedArtifact]]{
					resolveArtifactByJarName(jarName.replace("jar", "pom"))
				}).
		map(_.apply)	
		
		
	def printResults(resolvingResults: Iterable[Either[String, ResolvedArtifact]]) = {		
	    log.info("\r\n==================================================")
		log.info("==============SUCCESSFULLY RESOLVED===============")
		log.info("==================================================\r\n")
		resolvingResults.foreach(_.right.map(artifact => log.info(artifact.toString)))
	    log.info("\r\n==================================================")
		log.info("================FAILED TO RESOLVE=================")
		log.info("==================================================\r\n")		
		resolvingResults.foreach(_.left.map(name => log.info(" " + name.replace("pom", "jar"))))
		log.info("")
	}	
		
		
	/* *****************************	
	//        SBT actions	
	*******************************/
	
	lazy val libs = task {
		getLibraryJarNames.foreach(log.info(_))
		None
	}	
	
	lazy val resolveLibs = task {
		printResults(resolveLibArtifacts)
		None 
	}
	
	lazy val resolveLib = task { args =>
		if(args.length == 1){
			printResults(List(resolveArtifactByJarName(args(0))))
			task { None }
		}else
			task { Some("Usage: resolve-lib <jar name>.jar") }
	} completeWith getLibraryJarNames.toSeq

  
}