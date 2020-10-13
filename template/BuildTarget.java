package building.template;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

/** A build target of the present project.
  */
public enum BuildTarget {


    /** A software builder compiled from Java source code into `.class` files.  All other targets
      * implicitly include it, none can build without first building a builder.
      */
    builder; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
