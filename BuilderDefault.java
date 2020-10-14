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


    /** @param targetClass The class of build targets.
      * @see projectPackage()
      */
    public BuilderDefault( final Class<T> targetClass, final String projectPackage ) {
        Bootstrap.i.verify( targetClass );
        Bootstrap.i.verify( projectPackage );
        Bootstrap.i.verify( targetClass, projectPackage );
        this.targetClass = targetClass;
        this.projectPackage = projectPackage;
        projectPath = Bootstrap.pathOf( projectPackage ); }



    /** @param targetClass The class of build targets.
      * @see projectPackage()
      * @see projectPath()
      */
    public BuilderDefault( final Class<T> targetClass, final String projectPackage,
          final Path projectPath ) {
        Bootstrap.i.verify( targetClass );
        Bootstrap.i.verify( projectPackage );
        Bootstrap.i.verify( projectPath );
        Bootstrap.i.verify( projectPackage, projectPath );
        Bootstrap.i.verify( targetClass, projectPackage );
        this.targetClass = targetClass;
        this.projectPackage = projectPackage;
        this.projectPath = projectPath; }



    /** Builds the code to the level of `target`.
      *
      *     @param target The full name of the target.
      *     @throws IllegalArgumentException If `target` is unsupported by this implementation.
      */
    protected final void buildTo( final String target ) throws UserError {
        switch( target ) {
            case "builder" -> buildTo_builder();
            default -> {
                assert !isSupportDocumented( target );
                throw new IllegalArgumentException(); }}
        assert isSupportDocumented( target ); }



    /** Does nothing, this builder is already built.
      */
    protected void buildTo_builder() {}



    /** The proper package of the owning project.
      */
    public final String projectPackage() { return projectPackage; }


        private final String projectPackage;



    /** The proper path of the owning project.
      */
    public final Path projectPath() { return projectPath; }


        private final Path projectPath;



   // ━━━  B u i l d e r  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    /** @throws IllegalArgumentException If the matching target is unsupported by this implementation.
      */
    public @Override void build( final String targ ) throws UserError {
        buildTo( Builder.matchingTargetName( targ, targetClass )); }



////  P r i v a t e  ////////////////////////////////////////////////////////////////////////////////////


    private static boolean isSupportDocumented( final String target ) {
        boolean is = true;
        try { building.template.BuildTarget.valueOf( target ); }
        catch( IllegalArgumentException _x ) { is = false; }
        return is; }



    private final Class<T> targetClass; }



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
