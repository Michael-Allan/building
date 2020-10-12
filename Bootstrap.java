package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;
import java.util.*;

import static java.io.File.separatorChar;


/** A miscellany of resources for building software builders, residual odds and ends that properly fit
  * nowhere else during the earliest build stages.
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



    /** Prints a message of incremental build progress to standard output.
      *
      *     @param projectPackage The proper package of the project whose software is being built,
      *       or null if the software builder itself is being built.
      *     @param type A short name to identify the type of progress.
      *     @param volume The amount of progress.
      */
    public void showProgress( final String projectPackage, final String type, final int volume ) {
        final String project = projectPackage == null? "(bootstrap)": projectPackage;
        if( !projectsShowingProgress.contains( project )) {
            System.out.println( project );
            projectsShowingProgress.add( project ); }
        System.out.print( "    " );
        System.out.print( type );
        System.out.print( ' ' );
        System.out.println( volume ); }



    /** Tests the validity of `projectPackage` given as the proper package of a project.
      *
      *     @throws IllegalArgumentException
      */
    public void verify( final String projectPackage ) {
        if( projectPackage.equals("builder") || projectPackage.endsWith( ".builder" )) {
          throw new IllegalArgumentException( "Project package ends with `builder`: "
            + projectPackage ); }} // Simpler than allowing it, as explained for `#verify(Path)`.



    /** Tests the validity of `projectPath` given as the proper path of a project.
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


    private static final Set<String> projectsShowingProgress = new HashSet<>(); } /* Generally a maximum
      of two members, unless one project has customized its build to entail the build of another. */



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
