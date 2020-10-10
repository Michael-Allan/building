package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;

import static java.io.File.separatorChar;


/** A miscellany of resources for building the building code,
  * residual odds and ends that properly fit nowhere else.
  */
public final class Bootstrap {


    private Bootstrap() {}



    /** The proper path of the building project.
      */
    public static final Path buildingProjectPath = Path.of( "building" );



    /** The single instance of `Bootstrap`.
      */
    public static Bootstrap i() { return i; }


        private static final Bootstrap i = new Bootstrap();



    /** Prints an indication of successful compilation by `javac` of building code.
      */
    public void printCompilation( final int count ) {
        if( !wasPrintingStarted ) {
            System.out.print( "(bootstrap)\n" );
            wasPrintingStarted = true; }
        System.out.print( "    javac " );
        System.out.println( count ); }



    /** Tests the validity of a proper package given for a project.
      *
      *     @throws IllegalArgumentException
      */
    public void verify( final String projectPackage ) {
        if( projectPackage.equals("builder") || projectPackage.endsWith( ".builder" )) {
          throw new IllegalArgumentException( "Project package ends with `builder`: "
            + projectPackage ); }} // Simpler than allowing it, as explained for `#verify(Path)`.



    /** Tests the validity of a proper path given for a project.
      *
      *     @throws IllegalArgumentException
      */
    public void verify( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException( "Absolute `projectPath`" );
        if( projectPath.getFileName().toString().equals( "builder" )) {
          throw new IllegalArgumentException( "Project path ends with `builder`: " + projectPath ); }}
          // Simpler than trying to fathom the repercussions of allowing it, given that subdirectory
          // `builder/` is reserved for a project’s building code.



    /** Tests for consistency between parameters given for a project.
      * Where applicable, individually test each parameter before calling this method.
      *
      *     @param projectPackage The proper package of the project.
      *     @param projectPath The proper path of the project.
      *     @throws IllegalArgumentException
      */
    public void verify( final String projectPackage, final Path projectPath ) {
        if( !projectPackage.replace('.',separatorChar).equals( projectPath.toString() )) {
            throw new IllegalArgumentException( "Inequivalent `projectPackage` and `projectPath`" ); }}



    /** Tests for consistency between parameters given for a project.
      * Where applicable, individually test each parameter before calling this method.
      *
      *     @param projectPackage The proper package of the project.
      *     @param targetClass The class of the project’s build targets.
      *     @throws IllegalArgumentException
      */
    public void verify( final String projectPackage, final Class<?> targetClass ) {
        final String buildingCodePackage = targetClass.getPackageName();
        // According to `BuilderBuilder.internalBuildingCode`, one of these tests must pass:
        if( buildingCodePackage.equals( projectPackage )) return;
        if( buildingCodePackage.length() == projectPackage.length() + ".builder".length()
          && buildingCodePackage.startsWith( projectPackage )
          && buildingCodePackage.endsWith( ".builder" )) return;
        throw new IllegalArgumentException(
          "Inconsistency between `projectPackage` and `targetClass` package" ); }



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private boolean wasPrintingStarted; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
