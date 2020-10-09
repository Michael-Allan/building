package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;


/** A miscellany of resources for building the building code,
  * residual odds and ends that properly fit nowhere else.
  */
public final class Bootstrap {


    private Bootstrap() {}



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper path</a> of the building project.
      */
    public static final Path buildingProjectPath = Path.of( "building" );



    /** The single instance of `Bootstrap`.
      */
    public static Bootstrap i() { return i; }


        private static final Bootstrap i = new Bootstrap();



    /** Print an indication of successful compilation by `javac` of building code.
      */
    public void printCompilation( final int count ) {
        if( !wasPrintingStarted ) {
            System.out.print( "(bootstrap)\n" );
            wasPrintingStarted = true; }
        System.out.print( "    javac " );
        System.out.println( count ); }



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private boolean wasPrintingStarted; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
