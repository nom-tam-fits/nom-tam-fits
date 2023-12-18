# Building your own nom-tam-fits

Starting with version 1.14 the FITS library uses a number of features that
make Maven the recommended method for building the FITS library. These instructions 
are provided for a Unix/Linux but can be easily adapted for other platforms (such
as Windows or MacOS X) also (Architecture independence is one reason using tools like 
Maven is nice!)

1. Grab the source from 
  [github.com/nom-tam-fits/nom-tam-fits](https://github.com/nom.tam.fits/nom.tam.fits) 

  You can simply clone the repository, e.g.

  ```bash
    git clone https://github.com/nom-tam-fits/nom-tam-fits.git
  ```

  from your preferred location on your machine. After cloning you can of course set 
  your local repository to any given earlier commit as well, if you want to work 
  with a  particular version of the source code.

  Alternatively, you can check out 
  [Releases](https://github.com/nom-tam-fits/nom-tam-fits/releases) and grab the 
  source tarball from your preferred release, and extract it at the preferred 
  location:

  ```bash
    tar xzf nom-tam-fits-1.xx.x.tar.gz
  ```

2. Next, you will need __maven__ installed on your system as well as a JDK (such as 
   __openjdk__) version 9 or later. If you are using an RPM-based Linux distro, you 
   can install maven via __dnf__ / __yum__ as:

  ```bash
    sudo dnf install maven java-latest-openjdk
  ```

  Equivalently, for Debian-based distros: 

  ```bash
    sudo apt install maven default-jdk
  ```
    
3. Now build the package using maven:


  ```bash
    cd nom-tam-fits
    mvn package
  ```

  This will check and compile the source, create the jars, runs the tests suite, 
  builds the javadocs, checks for bugs, and builds the web-site locally.

  The JARs can be found under the `target/` directory (sorry, there is also a lot of 
  junk there from the tests_. The full API documentation can be found under 
  `target/apidocs`, while the more restricted set of Public User API documentation is 
  under `target/site/apidocs`. The local web-site including the Getting Started guide, 
  unit test coverage information, changelog, etc are under `target/site` (you can 
  simply open `index.html` in your browser to see).
    

