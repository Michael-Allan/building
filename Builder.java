package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Files;
import java.nio.file.Path;

import static building.Bootstrap.buildingProjectPath;


/** A builder of a project’s software.  It compiles the code of the project and prepares it for use.
  * In lieu of the {@linkplain BuilderD default implementation}, a project may define its own builder
  * by putting into its {@linkplain BuilderBuilder#internalBuildingCode(Path) building code}
  * a source file named `Builder.java`.  The class definition must be public and must include
  * a public constructor that takes no parameters.  It must inherit from the present interface.
  * It must depend on no code outside of the:
  *
  * <ul><li>Standard libraries</li>
  *     <li>{@linkplain BuilderBuilder#externalBuildingCode() External building code}</li>
  *     <li>{@linkplain BuilderBuilder#internalBuildingCode(Path) Internal building code}</li></ul>
  */
public interface Builder {


    /** Builds the code to the level of the given target.
      */
    public void build( String target ) throws UserError;



    /** Gives the path of the builder source file for a given project, namely
      * `<i>{@linkplain BuilderBuilder#internalBuildingCode(Path) internalBuildingCode}</i>/Builder.java`
      * if a file exists there, else the file path of the {@linkplain BuilderD default implementation}.
      *
      *     @param projectPath The proper path of the project.
      */
    public static Path implementationFile( final Path projectPath ) { // Cf. @ `BuilderBuilder`.
        if( projectPath.isAbsolute() ) throw new IllegalArgumentException();
        Path p = BuilderBuilder.internalBuildingCode(projectPath).resolve(
          projectPath.equals(buildingProjectPath)? "BuilderP.java":"Builder.java" );
            // So avoiding a name conflict with the present file.
        if( !Files.isRegularFile( p )) p = implementationFileDefault;
        return p; }



    /** The proper path of the source file for the {@linkplain BuilderD default implementation}.
      */
    public static final Path implementationFileDefault = buildingProjectPath.resolve( "BuilderD.java" );



    /** The output directory for builds.
      */
    public static final Path outDirectory = Path.of( System.getProperty( "java.io.tmpdir" ))
      .resolve( buildingProjectPath );



   // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    /** Thrown on encountering an error that the user is likely in a position to correct.
      */
    public static final class UserError extends Exception {

        public UserError( String message ) { super( message ); }}}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
