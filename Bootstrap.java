package building.Makeshift;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static building.Makeshift.Builder.UserError;
import static java.io.File.separatorChar;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.Files.getLastModifiedTime;


/** A miscellany of resources for building software builders, residual odds and ends that properly fit
  * nowhere else during the earliest build stages.
  */
public final class Bootstrap {


    private Bootstrap() {}



    /** Appends to `names` the proper path of each `.java` file of `directory` that needs to be compiled
      * or recompiled.  Does not descend into subdirectories.
      *
      *     @see #compile(List)
      */
    public static void addCompilableSource( final List<String> names, final Path directory ) {
        addCompilableSource( names, directory, pathTester_true ); }



    /** Appends to `names` the proper path of each `.java` file of `directory` that a) tests true
      * with `tester` and b) needs to be compiled or recompiled.  Does not descend into subdirectories.
      *
      *     @see #compile(List)
      */
    public static void addCompilableSource( final List<String> names, final Path directory,
          final Predicate<Path> tester ) {
        try( final Stream<Path> pp = Files.list( directory )) {
            for( final Path p: (Iterable<Path>)pp::iterator ) {
                if( Files.isDirectory( p )) continue;
                final String name = p.toString();
                if( !name.endsWith( ".java" )) continue;
                if( !tester.test( p )) continue;
                if( toCompile( p, simpleTypeName(p) )) {
                    names.add( p.toString() ); }}}
        catch( IOException x ) { throw new RuntimeException( x ); }}



    /** The proper path of Makeshift.
      */
    public static final Path buildingProjectPath = pathOf( "building.Makeshift" );



    /** Compiles Java source code to class files.
      *
      *     @param sourceNames The proper path of each source file to compile.
      */
    public void compile( final List<String> sourceNames ) throws UserError {
        // Changing?  Sync → `execute` @ `bin/build`.
        printProgressLeader( null/*bootstrapping*/, "javac" );
        final List<String> compilerArguments = new ArrayList<>();
        compilerArguments.add( System.getProperty("java.home") + "/bin/javac" );
          // The Java installation at `java.home` is known to include `javac` because also
          // it is a JDK installation, as assured by the `JDK_HOME` atop `bin/build`.
        compilerArguments.add( "@building/Makeshift/java_javac_arguments" );
        compilerArguments.add( "@building/Makeshift/javac_arguments" );
        compilerArguments.addAll( sourceNames );
        final ProcessBuilder pB = new ProcessBuilder( compilerArguments );
        pB.redirectOutput( INHERIT );
        pB.redirectError( INHERIT );
        try {
            final int exitValue =  pB.start().waitFor();
            if( exitValue == 1 ) throw new UserError( "Stopped on `javac` error" );
              // Already `javac` has told the details.
            else if( exitValue != 0 ) throw new RuntimeException( "Exit value of " + exitValue
              + " from process: " + pB.command() ); }
        catch( InterruptedException|IOException x ) { throw new RuntimeException( x ); }
        finally{ System.out.println( sourceNames.size() ); }}



    /** The single instance of `Bootstrap`.
      */
    public static final Bootstrap i = new Bootstrap();



    /** The output directory for builds.
      */
    public static final Path outDirectory = Path.of( System.getProperty( "java.io.tmpdir" ))
      .resolve( buildingProjectPath );



    /** Converts `path` to an equivalent Java package name.
      *
      *     @throws IllegalArgumentException If `path` is absolute.
      */
    public static String packageOf( final Path path ) {
        if( path.isAbsolute() ) throw new IllegalArgumentException();
        return path.toString().replace( separatorChar, '.' ); }



    /** Converts `JavaPackage` to an equivalent relative path.
      */
    public static Path pathOf( final String JavaPackage ) {
        return Path.of( pathStringOf( JavaPackage )); }
        // Changing?  Sync → any `pathOf` in each of `bin/*`.



    /** Converts `JavaPackage` to an equivalent relative path.
      */
    public static String pathStringOf( final String JavaPackage ) {
        return JavaPackage.replace( '.', separatorChar ); }
        // Changing?  Sync → any `pathStringOf` in each of `bin/*`.



    /** A path tester that answers only `true`.
      */
    public static final Predicate<Path> pathTester_true = _p -> { return true; };



    /** Prints and flushes through standard output the beginning of a message of incremental
      * build progress.  Be sure to print the remainder of the message and terminate it with
      * a newline character.
      *
      *     @param projectPackage The proper package of the project whose software is being built,
      *       or null if the software builder itself is being built.
      *     @param type A short name to identify the type of progress.
      */
    public void printProgressLeader( final String projectPackage, final String type ) {
        final String project = projectPackage == null? "(bootstrap)": projectPackage;
        if( !projectsShowingProgress.contains( project )) {
            System.out.println( project );
            projectsShowingProgress.add( project ); }
        System.out.print( "    " );
        System.out.print( type );
        System.out.print( ' ' );
        System.out.flush(); }



    /** Gives the simple name of the Java type proper to a source file at path `sourcePath`.
      * This assumes the restriction described at the end of §7.6 of the language specification,
      * e.g. giving type name ‘Toad’ for a path of `wet/sprocket/Toad.java`.
      *
      *     @param sourcePath The proper path of the source file.
      *     @see <a href='https://docs.oracle.com/javase/specs/jls/se15/html/jls-7.html#jls-7.6'>§7.6</a>
      *     @see #typeName(Path)
      */
    public static String simpleTypeName( final Path sourcePath ) {
        final String s = sourcePath.getFileName().toString();
        assert s.endsWith( ".java" );
        return s.substring( 0, s.length()-".java".length() ); }



    /** Answers whether `sourceFile` needs to be compiled or recompiled.
      *
      *     @param sourcePath The proper path of a Java source file.
      *     @param simpleTypeName The corresponding {@linkplain #simpleTypeName(Path) simple type name}.
      */
    public static boolean toCompile( final Path sourceFile, final String simpleTypeName ) {
        final Path classFile = outDirectory.resolve(
          sourceFile.getParent().resolve( simpleTypeName + ".class" ));
        if( Files.exists( classFile )) {
            try {
                return getLastModifiedTime(sourceFile).compareTo(getLastModifiedTime(classFile)) >= 0; }
            catch( IOException x ) { throw new RuntimeException( x ); }}
        return true; }



    /** Gives the fully extended name of the Java type proper to a source file at path `sourcePath`.
      * This assumes the restriction described at the end of §7.6 of the language specification,
      * e.g. giving type name ‘wet.sprocket.Toad’ for a path of `wet/sprocket/Toad.java`.
      *
      *     @param sourcePath The proper path of the source file.
      *     @see <a href='https://docs.oracle.com/javase/specs/jls/se15/html/jls-7.html#jls-7.6'>§7.6</a>
      *     @see #simpleTypeName(Path)
      */
    public static String typeName( final Path sourcePath ) {
        return packageOf(sourcePath.getParent()) + '.' + simpleTypeName(sourcePath); }



    /** Tests the validity of a `targetClass` given as the class of build targets for a project.
      *
      *     @throws IllegalArgumentException
      */
    public static <T extends Enum<T>> void verify( final Class<T> targetClass ) {
        Enum.valueOf( targetClass, "builder" ); }



    /** Tests for consistency between parameters given for a project.
      * Where applicable, individually test each parameter before calling this method.
      *
      *     @param targetClass The class of the project’s build targets.
      *     @param projectPackage The proper package of the project.
      *     @throws IllegalArgumentException
      */
    public static void verify( final Class<?> targetClass, final String projectPackage ) {
        final String iBC = targetClass.getPackageName(); /* Of `BuilderBuilder.internalBuildingCode`,
          that is, according to whose API documentation one of the following tests must pass. */
        if( iBC.equals( projectPackage )) return;
        if( iBC.length() == projectPackage.length() + ".builder".length()
          && iBC.startsWith( projectPackage )
          && iBC.endsWith( ".builder" )) return;
        throw new IllegalArgumentException(
          "Inconsistency between `projectPackage` and `targetClass` package" ); }



    /** Tests the validity of a `projectPath` given as the proper path of a project.
      *
      *     @throws IllegalArgumentException
      */
    public static void verify( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException( "Absolute `projectPath`" );
        if( projectPath.getFileName().toString().equals( "builder" )) {
          throw new IllegalArgumentException( "Project path ends with `builder`: " + projectPath ); }}
          // Simpler than trying to fathom the repercussions of allowing it, given that subdirectory
          // `builder/` is reserved for a project’s building code.



    /** Tests the validity of a `projectPackage` given as the proper package of a project.
      *
      *     @throws IllegalArgumentException
      */
    public static void verify( final String projectPackage ) {
        if( projectPackage.equals("builder") || projectPackage.endsWith( ".builder" )) {
          throw new IllegalArgumentException( "Project package ends with `builder`: "
            + projectPackage ); }} // Simpler than allowing it, as explained for `#verify(Path)`.



    /** Tests for consistency between parameters given for a project.
      * Where applicable, individually test each parameter before calling this method.
      *
      *     @param projectPackage The proper package of the project.
      *     @param projectPath The proper path of the project.
      *     @throws IllegalArgumentException
      */
    public static void verify( final String projectPackage, final Path projectPath ) {
        if( !pathStringOf(projectPackage).equals( projectPath.toString() )) {
            throw new IllegalArgumentException( "Inequivalent `projectPackage` and `projectPath`" ); }}



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private final Set<String> projectsShowingProgress = new HashSet<>(); } /* Generally a maximum
      of two members, unless one project has customized its build to entail the build of another. */



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
