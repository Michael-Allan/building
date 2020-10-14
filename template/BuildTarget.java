package building.template;

// Changes to this file immediately affect the next runtime.  Treat it as a script.


/** A build target of the present project.  A particular target name may be specified in the shell
  * by any substring that uniquely matches it.
  */
public enum BuildTarget {


    /** A software builder compiled from source code into Java class files.  All other targets
      * implicitly include it, none can build without first building a builder.
      */
    builder,


    /** Java class files compiled from source code.
      */
    Java_class_files; }



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
