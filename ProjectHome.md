## Google Data Apis Mavenized ##
This project is a maven project containing no code but being a Maven repository for Google Data Java Apis.
With it, I can use Google Apis in my maven projects without having to install files manually in my local maven repository.
This is a bit dumb to store jars in a project hosting service like googlecode but this is due to the following:
  * Google doesn't provide its GData Java Api in any public maven repository and I don't understand why, even if few developers may work with it at Google.
  * I want to use GData Java API with Maven because GData API is designed in modules and it fits the maven way of thinking quite well.
  * I don't feel like asking for upload of google libs in apache central repository instead of google.
  * The size of GData API Jars and the need to download these Jars are quite small so it will not bother too much (I hope so).
  * I created a POM maven project containing a maven repository because I like stupid cycles :)

Now you can go there:
  * [Available Mavenized Google Data Api Versions](AvailableVersions.md)
  * [How To](HowTo.md)

Kind Regards
[mandubian.org](http://www.mandubian.org)


---


### Last Updates ###
  * **2010/10/09 : mavenized GData version 1.41.5 + MAVEN SOURCE JARs
> > Please try it and tell me if you discover any issues**
  * 2010/06/10 : Automatic Pom Generation + Transitive dependencies + Nexus indexing
> > POMs+dependencies are generated automatically using Maven+Ant+Tattletale+Groovy following the idea of [David Carter](http://github.com/dcarter/Google-Data-APIs-Mavenized)
> > The repository is indexed using NexusIndexer maven plugin (directory .index)
> > Wiki available versions page is generated with a groovy script also
> > Added version 1.41.1, 1.41.2, 1.41.3 + transitive dependencies
  * 2010/04/01 : mavenized GData version 1.40.3, 1.41.0, 1.41.1 (not yet providing transitive dependencies but will work on it asap)
  * 2010/01/17 : mavenized GData version 1.40.0, 1.40.1, 1.40.2
  * 2009/11/19 : mavenized GData version 1.37.0, 1.38.0, 1.39.0, 1.39.1
