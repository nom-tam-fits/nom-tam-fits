![Build Status](https://github.com/attipaci/nom-tam-fits/actions/workflows/maven.yml/badge.svg)
[![Coverage Status](https://codecov.io/gh/attipaci/nom-tam-fits/branch/master/graph/badge.svg?token=F85TJ906TN)](https://codecov.io/gh/attipaci/nom-tam-fits)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits/badge.svg)](https://maven-badges.herokuapp.com/maven-central/gov.nasa.gsfc.heasarc/nom-tam-fits)

# nom-tam-fits


Pure java Java library for reading and writing FITS files. FITS, the Flexible Image Transport System, is the format commonly used in the archiving and transport of astronomical data.

This is the original library from http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/ ported to maven and published to the central repo by the original developers!

Please feel free to fork us and report any found issues at our github pages (Pull request are very welcome):

[nom-tam-fits git repository](https://github.com/nom-tam-fits/nom-tam-fits "nom-tam-fits git repository")

visit our documentation at: 

[nom-tam-fits project site](http://nom-tam-fits.github.io/nom-tam-fits/ "nom-tam-fits project site")

If you want to use the bleeding edge version of nom-tam-fits, you can get it from sonatype:

	<dependencies>
		<dependency>
			<groupId>gov.nasa.gsfc.heasarc</groupId>
			<artifactId>nom-tam-fits</artifactId>
			<version>xxxxx-SNAPSHOT</version>
		</dependency>
	</dependencies>
	...
	<repositories>
		<repository>
			<id>sonatype-snapshots</id>
			<url>https://oss.sonatype.org/content/repositories/snapshots</url>
			<snapshots>
				<enabled>true</enabled>
			</snapshots>
		</repository>
	</repositories>    


