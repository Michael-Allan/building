package building.builder;

// Changes to this file immediately affect the next runtime.  Treat it as a script.


/** A build target of the present project.  A particular target name may be specified in the shell
  * by any substring that uniquely matches it.
  */
public enum Target {


    /** A software builder compiled from Java source code into `.class` files.  All other targets
      * implicitly include it, none can build without first building a builder.
      */
    builder; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
