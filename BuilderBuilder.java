package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.io.IOException;
import java.nio.file.*;

import static java.io.File.separatorChar;
import static java.lang.ProcessBuilder.Redirect.INHERIT;
import static java.nio.file.Files.getLastModifiedTime;


/** A builder of software builders.
  * In lieu of the {@linkplain BuilderBuilderD default implementation}, a project may define one its own
  * by placing in its {@linkplain #implementationDirectory(Path) implementation directory} a source file
  * named `BuilderBuilderP.java`, viz. the builder builder proper to the project.  It must have a public
  * constructor with the same parameters as the default implementation.
  */
public interface BuilderBuilder {


    /** Compile the code of the software builder and prepare it for use.
      */
    default void build() throws UserError {
        throw new UnsupportedOperationException( "Not yet coded" ); }



    /** Return a builder builder for a given project, first compiling its code if necessary.
      *
      *     @see #projectPath()
      */
    static BuilderBuilder forPath( final Path projectPath ) throws UserError {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        return get( /*projectPackage*/projectPath.toString().replace(separatorChar,'.'), projectPath ); }



    /** Return the proper path of the implementation directory for a given project, namely
      * `<i>projectPath</i>/builder` if a directory exists there, else <i>projectPath</i>.
      *
      *     @see #projectPath()
      *     @see #implementationFile(Path)
      */
    static Path implementationDirectory( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = projectPath.resolve( "builder" );
        if( !Files.isDirectory( p )) p = projectPath;
        return p; }



    /** Return the path of the builder-builder source file for a given project, namely
      * `<i>{@linkplain #implementationDirectory(Path) implementationDirectory}</i>/BuilderBuilderP.java`
      * if a file exists there, else the path of the {@linkplain BuilderBuilderD default implementation}.
      *
      *     @see #projectPath()
      */
    static Path implementationFile( final Path projectPath ) {
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = implementationDirectory(projectPath).resolve( "BuilderBuilderP.java" );
        if( !Files.isRegularFile( p )) p = Path.of( "building", "BuilderBuilderD.java" );
        return p; }



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper package</a> of the owning project.
      */
    String projectPackage();



    /** The <a href='http://reluk.ca/project/building/lexicon.brec'>
      * proper path</a> of the owning project.
      */
    Path projectPath();



   // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    /** Thrown on encountering an error that the user is likely in a position to correct.
      */
    static final class UserError extends Exception {

        public UserError( String message ) { super( message ); }}



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    /** Return a builder builder for a given project, first compiling its code if necessary.
      *
      *     @see #projectPackage()
      *     @see #projectPath()
      */
    private static BuilderBuilder get( final String projectPackage, final Path projectPath )
      throws UserError {
        // Already the working directory is the command directory.  `../build_instructions.brec`
        final Path sourceFile = implementationFile( projectPath );
        final Path sourceDirectory = sourceFile.getParent();
        final String simpleClassName; {
            final String s = sourceFile.getFileName().toString();
            assert s.endsWith( ".java" );
            simpleClassName = s.substring( 0, s.length()-".java".length() ); }
        final boolean toCompile; {
            final Path outDirectory = Path.of(System.getProperty("java.io.tmpdir")).resolve(
              "building" ); // Proper path of the present project.
            final Path classFile = outDirectory.resolve(
              sourceDirectory.resolve( simpleClassName + ".class" ));
            if( Files.exists( classFile )) {
                try { toCompile =
                  getLastModifiedTime(sourceFile).compareTo(getLastModifiedTime(classFile)) >= 0; }
                catch( IOException x ) { throw new RuntimeException( x ); }}
            else toCompile = true; }
        if( toCompile ) {

          // Compile the code  cf. `BuildCommand.execute`
          // ────────────────
            final ProcessBuilder pB = new ProcessBuilder( System.getProperty("java.home") + "/bin/javac",
                // The Java installation at `java.home` is known to include `javac` because also
                // it is a JDK installation, as assured by the `JDK_HOME` atop `bin/build`.
              "@building/javac_arguments", "@building/javac_arguments_addendum", sourceFile.toString() );
            pB.redirectOutput( INHERIT );
            pB.redirectError( INHERIT );
            try {
                final int exitValue =  pB.start().waitFor();
                if( exitValue == 1 ) throw new UserError( "build: Stopped on `javac` error" );
                  // Already `javac` has told the details.
                else if( exitValue != 0 ) throw new RuntimeException( "Exit value of " + exitValue
                  + " from process: " + pB.command() ); }
            catch( InterruptedException|IOException x ) { throw new RuntimeException( x ); }}
        try {

          // Load the code
          // ─────────────
          final String cName = sourceDirectory.toString().replace( separatorChar, '.' )
            + '.' + simpleClassName;
          final Class<? extends BuilderBuilder> c = Class.forName( cName )
            .asSubclass( BuilderBuilder.class );

          // Construct an instance
          // ─────────────────────
            return c.getConstructor(String.class,Path.class).newInstance(
              projectPackage, projectPath ); }
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }}}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
