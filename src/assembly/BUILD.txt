Starting with version 1.14 the FITS library uses a number of features that
make Maven the recommended method for building the FITS library.  Look
at http://github.com/nom.tam.fits/nom.tam.fits to download the latest release.
However users who still wish to build the library directly from
the source jar can do so if they follow procedures similar to 
the instructions below.  

Perhaps you wish to compile for an older version of Java or 
maybe you want to try some tweaks to the library.  It's not hard.

These instructions  are given in a Unix/Linux context and should be suitably 
adapted to work in Windows and other architectures. (Architecture independence
is one reason using tools like Maven in nice!)

1. Extract the source files and configuration into a suitable directory.

E.g. suppose you have downloaded the source jar file into the directory
'downloads' and wish to create a local version of the library

   wget -O ~/downloads/fits_src.jar http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/fits_src.jar
   mkdir ~/myfits
   cd ~/myfits
   jar xf ~/downloads/fits_src.jar
   

2. A couple of external Jar files are needed to compile the library 
(but generally are not required to run it).  These libraries are available at:
  
    http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/dependencies

Download these Jar files into some location on your machine.  The 
commons-compress Jar is used for bzip decompression if you do not
use an executable command.  If you want to use this library for
bzip decompression, then you will need to have it in the classpath
during runtime too, but otherwise not.

The Annotation jar is only needed for compilation.  If
you delete all lines with SuppressFBWarnings from the source code
you can dispense with it altogether.  It is used to suppress warning
messages in some of the diagnostic tools.

So, 
    mkdir ~/dependencies
    cd ~/dependencies
    wget -O annotations.jar      http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/dependencies/annotations-2.0.1.jar
    wget -O commons.compress.jar http://heasarc.gsfc.nasa.gov/docs/heasarc/fits/java/v1.0/dependencies/commons-compress-1.11.jar
    
3. Now compile the code.  In the example we'll assume you are interested
in building a Java 1.6 compatible jar file.  First we need to get the list
of all of the Java files we are going to compile, then we set
the classpath to include our dependencies and then we do the compile.

    cd ~/myfits
    find . -name '*.java' > ../src.list    
    setenv CLASSPATH .:../dependencies/annotations.jar:../dependencies/commons.compress.jar
    javac -source 1.6 -target 1.6 -encoding utf8 @../src.list
    
There are a few source files that have UTF characters in them so
you will need to use the  '-encoding utf8' flag.  The UTF characters
are only in the JavaDoc comments so you can simply delete them
if you like.  The CLASSPATH (which you can set in a variety of ways) must
include both the tree we are compiling and the jars we just downloaded.

The -source 1.6 and -target 1.6 arguments allow you to use a more modern
Java compiler to create 1.6 compatible outputs.  If
you are using JDK 1.6, then they are superfluous.  We are endeavoring
to ensure that all 1.xx releases will remain compatible with Java 1.6.

    
4. Finally generate a new Jar file with the compiled classes.  
You can get rid of the .java files if you want, but it's OK to 
leave them in.

    find . -name '*.java' -exec rm {} \;  # Delete the Java source files
    jar cf ../myfits.jar .
    
You will need to package things up in a jar to run the library.
It now uses some resource files that it will look for in particular locations.
Putting them in the jar and then putting that Jar in your CLASSPATH will
ensure that the resource files are found.

5. Clean up (if you like):

    cd
    rm -rf dependencies # But not if using bzip decompression.
    rm -rf myfits
    rm  src.list
   
6. Use the library

    cd ~/mycode     
    setenv CLASSPATH ../myfits.jar
    javac MyClass.java   # Where this is some class you write.
    java MyClass
     
     
Good luck! 
