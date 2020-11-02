package building.Makeshift.builder;

// Changes to this file immediately affect the next build.  Treat it as a build script.


/** A build target of the present project.  Shell commands will accept abbreviated target names:
  * a target may be specified by any substring of its name that matches no other target name.
  */
public enum Target {


    /** A software builder compiled from source code into Java class files.
      * All other targets depend on this target and include it implicitly.
      */
    builder; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
