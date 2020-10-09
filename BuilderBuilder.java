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


/** A builder of software builders.
  * In lieu of the {@linkplain BuilderBuilderD default implementation}, a project may define
  * its own by including with its {@linkplain #internalBuildingCode(Path) internal building code}
  * a source file named `BuilderBuilder.java`.  The class definition must be public.  It must have
  * a public constructor that takes either the same parameters as the default implementation, or no
  * parameters.  It must inherit from the present interface.  It must depend on no code outside of:
  *
  * <ul><li>The standard libraries</li>
  *     <li>`{@linkplain Bootstrap       Bootstrap}`</li>
  *     <li>`{@linkplain Builder         Builder}`</li>
  *     <li>                            `BuilderBuilder` (the present interface)</li>
  *     <li>`{@linkplain BuilderBuilderD BuilderBuilderD}`</li></ul>
  */
public interface BuilderBuilder {


    /** Compile the code of the software builder, including any
      * {@linkplain #externalBuildingCode() external building code} on which it depends,
      * and prepare it for use.
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



    /** The proper package of each project, aside from the {@linkplain #projectPackage() owning project},
      * whose {@linkplain #internalBuildingCode() building code} the software builder may depend on.
      * The default implementation returns a singleton set comprising ‘building’, the proper package
      * of the <a href='http://reluk.ca/project/building/'>building project</a>.
      *
      *     @see #internalBuildingCode(Path)
      */
    public default Set<String> externalBuildingCode() { return Set.of( "building" ); }



    /** Give a builder builder for a given project, first compiling its code if necessary.
      *
      *     @see #projectPackage()
      */
    public static BuilderBuilder forPackage( final String projectPackage ) throws UserError {
        return get( projectPackage, /*projectPath*/FileSystems.getDefault().getPath(
          projectPackage.replace( '.', separatorChar ))); }



    /** Give a builder builder for a given project, first compiling its code if necessary.
      *
      *     @see #projectPath()
      */
    public static BuilderBuilder forPath( final Path projectPath ) throws UserError {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        return get( /*projectPackage*/projectPath.toString().replace(separatorChar,'.'), projectPath ); }



    /** Give the path of the builder-builder source file for a given project, namely
      * `<i>{@linkplain #internalBuildingCode(Path) internalBuildingCode}</i>/BuilderBuilder.java`
      * if a file exists there, else the path of the {@linkplain BuilderBuilderD default implementation}.
      *
      *     @see #projectPath()
      */
    public static Path implementationFile( final Path projectPath ) { // Cf. @ `Builder`.
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = internalBuildingCode(projectPath).resolve(
          projectPath.equals(buildingProjectPath)? "BuilderBuilderP.java":"BuilderBuilder.java" );
            // So avoiding a name conflict with the present file.
        if( !Files.isRegularFile( p )) p = implementationFileDefault;
        return p; }



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper path</a> of the source file for the {@linkplain BuilderBuilderD default implementation}.
      */
    public static final Path implementationFileDefault =
      buildingProjectPath.resolve( "BuilderBuilderD.java" );



    /** Give the proper path of the directory containing the internal building code for a given project,
      * namely `<i>projectPath</i>/builder/` if a directory exists there, else `<i>projectPath</i>/`.
      * A project may store the code in this directory alone, exclusive of subdirectories.
      *
      *     @see #projectPath()
      *     @see #externalBuildingCode()
      */
    public static Path internalBuildingCode( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = projectPath.resolve( "builder" );
        if( !Files.isDirectory( p )) p = projectPath;
        return p; }



    public default Builder newBuilder() {
        final Path sourceFile = Builder.implementationFile( projectPath() );
        final String cName = sourceFile.getParent().toString().replace( separatorChar, '.' )
          + '.' + simpleClassName(sourceFile);
        try {
            final Class<? extends Builder> c = Class.forName(cName).asSubclass(Builder.class);
            return c.getConstructor().newInstance(); }
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper package</a> of the owning project.
      */
    public String projectPackage();



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper path</a> of the owning project.
      */
    public Path projectPath();



    /** Set of projects for which a {@linkplain #build() builder build} was called in the present
      * runtime.  Here each project is represented by its {@linkplain #projectPackage() proper package}.
      * Never remove a project from this set.  Use it in builder builds to avoid duplicate builds.
      */
    public static final Set<String> projectsUnderBuild = new HashSet<>();



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


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



    /** Give a builder builder for a given project, first compiling its code if necessary.
      *
      *     @see #projectPackage()
      *     @see #projectPath()
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
            try {
                return c.getConstructor(String.class,Path.class).newInstance(
                  projectPackage, projectPath ); }
            catch( NoSuchMethodException x ) { return c.getConstructor().newInstance(); }}
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}



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
