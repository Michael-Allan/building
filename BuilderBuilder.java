package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.*;

import static building.Bootstrap.buildingProjectPath;
import static building.Builder.UserError;
import static java.io.File.separatorChar;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.Files.getLastModifiedTime;


/** A builder of software builders.  In lieu of the {@linkplain BuilderBuilderD default implementation},
  * a project may define its own builder by putting a source file named `BuilderBuilder.java` into its
  * {@linkplain #internalBuildingCode(Path) building code}.  The class definition must be public and must
  * include a public constructor that takes no parameters.  It must inherit from the present interface.
  * It must depend on no code outside of:
  *
  * <ul><li>The standard libraries</li>
  *     <li>`{@linkplain Bootstrap       Bootstrap}`</li>
  *     <li>`{@linkplain Builder         Builder}`</li>
  *     <li>                            `BuilderBuilder` (the present interface)</li>
  *     <li>`{@linkplain BuilderBuilderD BuilderBuilderD}`</li></ul>
  */
public interface BuilderBuilder {


    /** Compiles the code of the software builder, including any
      * {@linkplain #externalBuildingCode() external building code} on which it depends,
      * and prepares it for use.
      *
      * <p>To get an instance of the builder once built, use {@linkplain #newBuilder() newBuilder}.</p>
      *
      *     @throws IllegalStateException If already a build had started.
      */
    public default void build() throws UserError {
        final String owningProject = projectPackage();
        if( projectsUnderBuild.contains( owningProject )) throw new IllegalStateException();
        projectsUnderBuild.add( owningProject );

      // Build the external building code
      // ────────────────────────────────
        for( final String externalProject: externalBuildingCode() ) { /* Iteration order is unimportant;
              regardless projects will build here in correct order.  The building project, for instance,
              will always build before any other project that nominally depends on it. */
            if( projectsUnderBuild.contains( externalProject )) continue;
            forPackage(externalProject).build(); }

      // Compile the internal building code
      // ──────────────────────────────────
        final Path sourceDirectory = internalBuildingCode( projectPath() );
        final List<String> sourceNames = new ArrayList<>();
        try( final Stream<Path> pp = Files.list( sourceDirectory )) {
            for( final Path sourceFile: (Iterable<Path>)pp::iterator ) {
                if( Files.isDirectory( sourceFile )) continue;
                final String name = sourceFile.toString();
                if( !name.endsWith( ".java" )) continue;
                if( toCompile( sourceFile, simpleClassName(sourceFile) )) {
                    sourceNames.add( sourceFile.toString() ); }}}
        catch( IOException x ) { throw new RuntimeException( x ); }
        if( sourceNames.size() > 0 ) compile( sourceNames ); }



    /** Gives the proper package of each project, less the {@linkplain #projectPackage() owning project},
      * whose {@linkplain #internalBuildingCode() building code} the software builder may depend on.
      * The default implementation gives a singleton set comprising ‘building’, the proper package
      * of the <a href='http://reluk.ca/project/building/'>building project</a>.
      *
      *     @see #internalBuildingCode(Path)
      *     @return Set of proper packages.
      */
    public default Set<String> externalBuildingCode() { return Set.of( "building" ); }



    /** Gives a builder builder for a given project, first compiling its code if necessary.
      *
      *     @param projectPackage The proper package of the project.
      */
    public static BuilderBuilder forPackage( final String projectPackage ) throws UserError {
        return get( projectPackage, /*projectPath*/FileSystems.getDefault().getPath(
          projectPackage.replace( '.', separatorChar ))); }



    /** Gives a builder builder for a given project, first compiling its code if necessary.
      *
      *     @param projectPath The proper path of the project.
      */
    public static BuilderBuilder forPath( final Path projectPath ) throws UserError {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        return get( /*projectPackage*/projectPath.toString().replace(separatorChar,'.'), projectPath ); }



    /** Gives the proper path of the builder-builder source file for a given project, namely
      * `<i>{@linkplain #internalBuildingCode(Path) internalBuildingCode}</i>/BuilderBuilder.java`
      * if a file exists there, else the path of the {@linkplain BuilderBuilderD default implementation}.
      *
      *     @param projectPath The proper path of the project.
      */
    public static Path implementationFile( final Path projectPath ) { // Cf. @ `Builder`.
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = internalBuildingCode(projectPath).resolve(
          projectPath.equals(buildingProjectPath)? "BuilderBuilderP.java":"BuilderBuilder.java" );
            // So avoiding a name conflict with the present file.
        if( !Files.isRegularFile( p )) p = implementationFileDefault;
        return p; }



    /** The proper path of the source file for the {@linkplain BuilderBuilderD default implementation}.
      */
    public static final Path implementationFileDefault =
      buildingProjectPath.resolve( "BuilderBuilderD.java" );



    /** Gives the proper path of the directory containing the internal building code for a given project,
      * namely `<i>projectPath</i>/builder/` if a directory exists there, else `<i>projectPath</i>/`.
      * A project may store the code in this directory alone, exclusive of subdirectories.
      *
      *     @param projectPath The proper path of the project.
      *     @see #externalBuildingCode()
      */
    public static Path internalBuildingCode( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = projectPath.resolve( "builder" );
        if( !Files.isDirectory( p )) p = projectPath;
        return p; }



    /** Makes an instance of the software builder, once {@linkplain #build() built}.
      */
    public default Builder newBuilder() {
        try {
            final Path projectPath = projectPath();
            final Class<? extends Builder> cBuilder =
              Class.forName( className( Builder.implementationFile( projectPath )))
              .asSubclass( Builder.class );
            final Class<? extends Enum> cTarget =
              Class.forName( className( internalBuildingCode(projectPath).resolve( "Target.java" )))
              .asSubclass( Enum.class );
            try { // One of (a) the default implementation of `BuilderD`, or (b) a custom one:
                return cBuilder.getConstructor( String.class, Path.class, Class.class ) // (a)
                  .newInstance( projectPackage(), projectPath, cTarget ); }
            catch( NoSuchMethodException x ) { return cBuilder.getConstructor().newInstance(); }} // (b)
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}



    /** The proper package of the owning project.
      */
    public String projectPackage();



    /** The proper path of the owning project.
      */
    public Path projectPath();



    /** Set of projects for which a {@linkplain #build() builder build} was called in the present
      * runtime.  Here each project is represented by its proper package.
      *
      * <p>Never remove a project from this set.  Rather use it in builder builds to avoid
      * duplicate builds of a project.</p>
      */
    public static final Set<String> projectsUnderBuild = new HashSet<>();



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    /** @see #simpleClassName(Path)
      */
    private static String className( final Path sourceFile ) {
        return sourceFile.getParent().toString().replace( separatorChar, '.' )
          + '.' + simpleClassName(sourceFile); }



    /** @param sourceNames The proper path of each source file to compile.
      */
    private static void compile( final List<String> sourceNames ) throws UserError {
        // Changing?  Sync → `BuildCommand.execute`.
        final List<String> compilerArguments = new ArrayList<>();
        compilerArguments.add( System.getProperty("java.home") + "/bin/javac" );
          // The Java installation at `java.home` is known to include `javac` because also
          // it is a JDK installation, as assured by the `JDK_HOME` at top.
        compilerArguments.add( "@building/java_javac_arguments" );
        compilerArguments.add( "@building/javac_arguments" );
        compilerArguments.addAll( sourceNames );
        final ProcessBuilder pB = new ProcessBuilder( compilerArguments );
        pB.redirectOutput( INHERIT );
        pB.redirectError( INHERIT );
        try {
            final int exitValue =  pB.start().waitFor();
            if( exitValue == 1 ) throw new UserError( "build: Stopped on `javac` error" );
              // Already `javac` has told the details.
            else if( exitValue != 0 ) throw new RuntimeException( "Exit value of " + exitValue
              + " from process: " + pB.command() ); }
        catch( InterruptedException|IOException x ) { throw new RuntimeException( x ); }
        Bootstrap.i().printCompilation( sourceNames.size() ); }



    /** Gives a builder builder for a given project, first compiling its code if necessary.
      *
      *     @param projectPackage The proper package of the project.
      *     @param projectPath The proper path of the project.
      */
    private static BuilderBuilder get( final String projectPackage, final Path projectPath )
      throws UserError {
        Path sourceDirectory = null; // Of the implementation file.
        String simpleClassName = null; // Of the implementation, e.g. ‘BuilderBuilderD’.

      // Compile the code
      // ────────────────
        final List<String> sourceNames = new ArrayList<>(); {
            final List<Path> sources = new ArrayList<>(); {
                final Path iF = implementationFile( projectPath );
                sources.add( iF );
                if( !iF.equals( implementationFileDefault )) { // Then the project defines a custom one.
                    sources.add( 0, implementationFileDefault ); }} /* Nevertheless insert the default
                      in case the custom one inherits from it.  Insert it at 0 to ensure correct setting
                      of `sourceDirectory` and `simpleClassName` above. */
            for( final Path sourceFile: sources ) {
                sourceDirectory = sourceFile.getParent();
                simpleClassName = simpleClassName( sourceFile );
                final Path classFile = Builder.outDirectory.resolve(
                  sourceDirectory.resolve( simpleClassName + ".class" ));
                if( toCompile( sourceFile, simpleClassName )) {
                    sourceNames.add( sourceFile.toString() ); }}}
        if( sourceNames.size() > 0 ) compile( sourceNames );

      // Construct an instance
      // ─────────────────────
        final String cName = sourceDirectory.toString().replace( separatorChar, '.' )
          + '.' + simpleClassName;
        try {
            final Class<? extends BuilderBuilder> c = Class.forName( cName )
              .asSubclass( BuilderBuilder.class );
            try { // One of (a) the default implementation of `BuilderBuilderD`, or (b) a custom one:
                return c.getConstructor( String.class, Path.class ) // (a)
                  .newInstance( projectPackage, projectPath ); }
            catch( NoSuchMethodException x ) { return c.getConstructor().newInstance(); }} // (b)
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}



    /** @see #className(Path)
      */
    private static String simpleClassName( final Path sourceFile ) {
        final String s = sourceFile.getFileName().toString();
        assert s.endsWith( ".java" );
        return s.substring( 0, s.length()-".java".length() ); }



    private static boolean toCompile( final Path sourceFile, final String simpleClassName ) {
        final Path classFile = Builder.outDirectory.resolve(
          sourceFile.getParent().resolve( simpleClassName + ".class" ));
        if( Files.exists( classFile )) {
            try {
                return getLastModifiedTime(sourceFile).compareTo(getLastModifiedTime(classFile)) >= 0; }
            catch( IOException x ) { throw new RuntimeException( x ); }}
        return true; }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
