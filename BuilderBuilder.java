package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static building.Bootstrap.buildingProjectPath;
import static building.Bootstrap.pathTester_true;
import static building.Builder.UserError;
import static java.io.File.separatorChar;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.Files.getLastModifiedTime;


/** A builder of software builders.  In place of the {@linkplain BuilderBuilderDefault default},
  * a project may define its own builder by putting a source file named `BuilderBuilder.java` into its
  * {@linkplain #internalBuildingCode(Path) building code}.  The class definition must be public and must
  * include a public constructor that takes no parameters.  It must inherit from the present interface.
  * It must depend on no code outside of:
  *
  * <ul><li>The standard libraries</li>
  *     <li>`{@linkplain Bootstrap       Bootstrap}`</li>
  *     <li>`{@linkplain Builder         Builder}`</li>
  *     <li>                            `BuilderBuilder` (the present interface)</li>
  *     <li>`{@linkplain BuilderBuilderDefault BuilderBuilderDefault}`</li></ul>
  */
public interface BuilderBuilder {


    /** Gives the proper path of each directory of building code additional to the
      * {@linkplain #internalBuildingCode() internal building code}.  The added code
      * comprises all the files of each directory, exclusive of its subdirectories.
      * Such code may be intended for the use of other projects, for example, as part
      * of their {@linkplain #externalBuildingCode() <em>external</em> building code}.
      *
      * <p>The default implementation gives an empty set.</p>
      *
      *     @see #internalBuildingCode(Path)
      */
    public default Set<Path> addedBuildingCode() { return Set.of(); }



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

      // Compile the project’s own building code
      // ───────────────────────────────────────
        final List<String> sourceNames = new ArrayList<>();
        final Predicate<Path> tester = targetFile().getFileName().toString().equals( "Target.java" )?
          pathTester_true: p -> { return p.getFileName().toString().startsWith("Build"); };
        addCompilableSource( sourceNames, internalBuildingCode(projectPath()), tester );
        addedBuildingCode().forEach( directory -> addCompilableSource( sourceNames, directory ));
        if( sourceNames.size() > 0 ) compile( sourceNames ); }



    /** Gives the proper package of each project, less the {@linkplain #projectPackage() owning project},
      * whose {@linkplain #internalBuildingCode() building code} the software builder may depend on.
      * The default implementation gives a singleton set comprising ‘building’, the proper package
      * of the <a href='http://reluk.ca/project/building/'>building project</a>.
      *
      *     @see #internalBuildingCode(Path)
      */
    public default Set<String> externalBuildingCode() { return Set.of( "building" ); }



    /** Gives a builder builder, first compiling its code if necessary.
      *
      *     @param projectPackage The proper package of the owning project.
      */
    public static BuilderBuilder forPackage( final String projectPackage ) throws UserError {
        Bootstrap.i().verify( projectPackage );
        return get( projectPackage, /*projectPath*/FileSystems.getDefault().getPath(
          projectPackage.replace( '.', separatorChar ))); }



    /** Gives a builder builder, first compiling its code if necessary.
      *
      *     @param projectPath The proper path of the owning project.
      */
    public static BuilderBuilder forPath( final Path projectPath ) throws UserError {
        Bootstrap.i().verify( projectPath );
        return get( /*projectPackage*/projectPath.toString().replace(separatorChar,'.'), projectPath ); }



    /** Gives the proper path of a builder builder’s source file.  The given path is either
      * `<i>{@linkplain #internalBuildingCode(Path) internalBuildingCode}</i>/BuilderBuilder.java` if a
      * file exists there, or the path to the {@linkplain BuilderBuilderDefault default implementation}.
      *
      *     @param projectPath The proper path of the owning project.
      */
    public static Path implementationFile( final Path projectPath ) { // Cf. @ `Builder`.
        Bootstrap.i().verify( projectPath );
        Path p = internalBuildingCode(projectPath).resolve( "BuilderBuilder.java" );
        if( !Files.isRegularFile( p )) p = implementationFileDefault;
        return p; }



    /** The proper path of the source file for the
      * {@linkplain BuilderBuilderDefault default implementation}.
      */
    public static final Path implementationFileDefault =
      buildingProjectPath.resolve( "BuilderBuilderDefault.java" );



    /** Gives the proper path of the directory containing a project’s internal building code.
      * Gives either (a) `<i>projectPath</i>/builder/` if a directory exists there,
      * else (b) `<i>projectPath</i>/`.  A project may store its internal building code
      * in this directory alone, exclusive of subdirectories.
      *
      * <p>Moreover, in the default implementation, if (c) this directory contains a file
      * named `BuildTarget.java`, then it defines the build targets of the project
      * and the building code comprises only those files whose names begin with `Build`.
      * Otherwise the building code comprises all child files of the directory.</p>
      *
      *     @param projectPath The proper path of the project.
      *     @see #addedBuildingCode()
      *     @see #externalBuildingCode()
      *     @see <a href='http://reluk.ca/project/building/example/sub/'      >Example of (a)</a>
      *     @see <a href='http://reluk.ca/project/building/example/top/'      >Example of (b)</a>
      *     @see <a href='http://reluk.ca/project/building/example/mixed_top/'>Example of (c)</a>
      */
    public static Path internalBuildingCode( final Path projectPath ) {
        Bootstrap.i().verify( projectPath );
        Path p = projectPath.resolve( "builder" );
        if( !Files.isDirectory( p )) p = projectPath;
        return p; }



    /** Makes an instance of the software builder, once {@linkplain #build() built}.
      */
    public default Builder newBuilder() {
        try {
            final Class<? extends Builder> cBuilder =
              Class.forName( className( Builder.implementationFile( projectPath() )))
              .asSubclass( Builder.class );
            final Class<? extends Enum> cTarget =
              Class.forName( className( targetFile() )).asSubclass( Enum.class );
            try { // One of (a) the default implementation `BuilderDefault`, or (b) a custom one:
                return cBuilder.getConstructor( String.class, Path.class, Class.class ) // (a)
                  .newInstance( projectPackage(), projectPath(), cTarget ); }
            catch( NoSuchMethodException x ) { return cBuilder.getConstructor().newInstance(); }} // (b)
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}



    /** The proper package of the owning project.
      */
    public String projectPackage();



    /** The proper path of the owning project.
      */
    public Path projectPath();



    /** Set of projects for which a {@linkplain #build() builder build} was called in the present
      * runtime, each identified by its proper package.
      *
      * <p>Never remove a project from this set.  Rather use it in builder builds to avoid
      * duplicate builds of a project.</p>
      */
    public static final Set<String> projectsUnderBuild = new HashSet<>();



    /** Gives the proper path of the source file that defines the build targets of the project.
      * The default implementation gives a subpath
      * of the {@linkplain #internalBuildingCode(Path) internal building code} with a simple name
      * of either `BuildTarget.java` if a file exists there, else `Target.java`.
      */
    public default Path targetFile() {
        final Path iBC = internalBuildingCode( projectPath() );
        Path p = iBC.resolve( "BuildTarget.java" );
        if( !Files.isRegularFile( p )) p = iBC.resolve( "Target.java" );
        return p; }



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private static void addCompilableSource( final List<String> names, final Path directory ) {
        addCompilableSource( names, directory, pathTester_true ); }



    private static void addCompilableSource( final List<String> names, final Path directory,
          final Predicate<Path> tester ) {
        try( final Stream<Path> pp = Files.list( directory )) {
            for( final Path p: (Iterable<Path>)pp::iterator ) {
                if( Files.isDirectory( p )) continue;
                final String name = p.toString();
                if( !name.endsWith( ".java" )) continue;
                if( !tester.test( p )) continue;
                if( toCompile( p, simpleClassName(p) )) {
                    names.add( p.toString() ); }}}
        catch( IOException x ) { throw new RuntimeException( x ); }}



    /** @see #simpleClassName(Path)
      */
    private static String className( final Path sourceFile ) {
        return sourceFile.getParent().toString().replace( separatorChar, '.' )
          + '.' + simpleClassName(sourceFile); }



    /** @param sourceNames The proper path of each source file to compile.
      */
    private static void compile( final List<String> sourceNames ) throws UserError {
        // Changing?  Sync → `BuildCommand.execute`.
        Bootstrap.i().printProgressLeader( null/*bootstrapping*/, "javac" );
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
        finally{ System.out.println( sourceNames.size() ); }}



    /** Gives a builder builder, first compiling its code if necessary.
      *
      *     @param projectPackage The proper package of the owning project.
      *     @param projectPath The proper path of the owning project.
      */
    private static BuilderBuilder get( final String projectPackage, final Path projectPath )
      throws UserError {
        Path sourceDirectory = null; // Of the implementation file.
        String simpleClassName = null; // Of the implementation, e.g. ‘BuilderBuilderDefault’.

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
            try { // One of (a) the default implementation `BuilderBuilderDefault`, or (b) a custom one:
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
