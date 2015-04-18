## Add mandubian-mvn repository to your pom.xml repositories ##
```
<repository>
  <id>mandubian-mvn</id>
  <url>http://mandubian-mvn.googlecode.com/svn/trunk/mandubian-mvn/repository</url>
</repository>
```


## Add dependencies to your pom.xml dependencies ##
```
<dependency>
  <groupId>com.google.gdata</groupId>
  <artifactId>gdata-core-1.0</artifactId>
  <version>1.36.0</version>
</dependency>
```
  * _1.36.0_ is a maven version but it is also the google delivery version. _Available maven versions corresponding to google delivery versions are described in [AvailableVersions](AvailableVersions.md)_
  * _gdata-core-1.0_ means gdata-core API version 1.0