package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;


/** Default implementation of a software builder.  It supports all the targets
  * named in `building.template.{@linkplain building.template.BuildTarget BuildTarget}`,
  * but will refuse to build any outside of `T`.
  *
  *     @param <T> The type of build targets.  The names of all its targets should comprise
  *       a subset of {@linkplain building.template.BuildTarget those supported}.
  */
public class BuilderDefault<T extends Enum<T>> implements Builder {


    /** @see projectPackage()
      * @see projectPath()
      * @param targetClass The class of build targets.
      */
    public BuilderDefault( final String projectPackage, final Path projectPath,
          final Class<T> targetClass ) {
        Bootstrap.i().verify( projectPackage );
        Bootstrap.i().verify( projectPath );
        Bootstrap.i().verify( targetClass );
        Bootstrap.i().verify( projectPackage, projectPath );
        Bootstrap.i().verify( projectPackage, targetClass );
        this.projectPackage = projectPackage;
        this.projectPath = projectPath;
        this.targetClass = targetClass; }



    /** Does nothing, this builder is already built.
      */
    public final void buildTarget_builder() {}



    /** The proper package of the owning project.
      */
    public final String projectPackage() { return projectPackage; }


        private final String projectPackage;



    /** The proper path of the owning project.
      */
    public final Path projectPath() { return projectPath; }


        private final Path projectPath;



   // ━━━  B u i l d e r  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    /** @throws UserError If `target` is undefined for the owning project.
      *    This exception may be thrown for other reasons as well.
      * @throws IllegalArgumentException If `target` is unsupported by this implementation.
      */
    public @Override void build( final String target ) throws UserError {
        try { Enum.valueOf( targetClass, target ); }
        catch( IllegalArgumentException x ) {
            throw new UserError( "build: Undefined in `" + targetClass.getName() + "`: " + target ); }
        switch( target ) {
            case "builder" -> buildTarget_builder();
            default -> {
                assert !isSupportDocumented( target );
                throw new IllegalArgumentException(); }}
        assert isSupportDocumented( target ); }



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private static boolean isSupportDocumented( final String target ) {
        boolean is = true;
        try { building.template.BuildTarget.valueOf( target ); }
        catch( IllegalArgumentException _x ) { is = false; }
        return is; }



    private final Class<T> targetClass; }



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
