package building.Makeshift;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Files;
import java.nio.file.Path;


/** A builder of a project’s software.  It compiles the code of the project and prepares it for use.
  * In place of the {@linkplain BuilderDefault default}, a project may define its own builder
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


    /** Builds the code to the level of a given target.
      *
      *     @param targ The name of the target, or a unique substring of it.
      *     @throws UserError If `targ` does not match exactly one build target of the owning project.
      *       This exception may be thrown for other reasons as well.
      */
    public void build( String targ ) throws UserError;



    /** Gives the proper path of a builder’s source file.  The given path is either
      * `<i>{@linkplain BuilderBuilder#internalBuildingCode(Path) internalBuildingCode}</i>/Builder.java`
      * if a file exists there, or the path to the {@linkplain BuilderDefault default implementation}.
      *
      *     @param projectPath The proper path of the owning project.
      */
    public static Path implementationFile( final Path projectPath ) { // Cf. @ `BuilderBuilder`.
        Bootstrap.verify( projectPath );
        Path p = BuilderBuilder.internalBuildingCode(projectPath).resolve( "Builder.java" );
        if( !Files.isRegularFile( p )) p = implementationFileDefault;
        return p; }



    /** The proper path of the source file for the {@linkplain BuilderDefault default implementation}.
      */
    public static final Path implementationFileDefault =
      Bootstrap.buildingProjectPath.resolve( "BuilderDefault.java" );



    /** Gives the target name that matches `targ`.
      *
      *     @param targ The name of a build target, or a unique substring of it.
      *     @param targetClass The class of build targets.
      *     @throws UserError If `targ` does not match exactly one build target of `targetClass`.
      */
    public static <T extends Enum<T>> String matchingTargetName( final String targ,
          final Class<T> targetClass ) throws UserError {
        final Enum<?>[] targets;
        try { targets = (Enum[])targetClass.getMethod("values").invoke( null/*static*/ ); }
        catch( ReflectiveOperationException x ) { throw new RuntimeException( x ); }
        String name = null;
        for( final Enum<?> t: targets ) {
            final String tS = t.toString();
            if( tS.contains( targ )) {
                if( name != null ) {
                    throw new UserError( "Ambiguous in `" + targetClass.getName() + "`: " + targ ); }
                name = tS; }}
        if( name == null ) {
            throw new UserError( "Unmatched in `" + targetClass.getName() + "`: " + targ ); }
        return name; }



   // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    /** Thrown on encountering an anomaly the user is likely in a position to correct.
      */
    public static final class UserError extends Exception {

        public UserError( String message ) { super( message ); }}}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
