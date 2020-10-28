package building.Javanese;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;

import static building.Javanese.Bootstrap.verify;


/** Default implementation of a builder builder.
  */
public class BuilderBuilderDefault implements BuilderBuilder {


    /** @see #projectPackage()
      */
    public BuilderBuilderDefault( final String projectPackage ) {
        verify( projectPackage );
        this.projectPackage = projectPackage;
        this.projectPath = Bootstrap.pathOf( projectPackage ); }



    /** @see #projectPackage()
      * @see #projectPath()
      */
    public BuilderBuilderDefault( final String projectPackage, final Path projectPath ) {
        verify( projectPackage );
        verify( projectPath );
        verify( projectPackage, projectPath );
        this.projectPackage = projectPackage;
        this.projectPath = projectPath; }



   // ━━━  B u i l d e r   B u i l d e r  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    public final @Override String projectPackage() { return projectPackage; }


        private final String projectPackage;



    public final @Override Path projectPath() { return projectPath; }


        private final Path projectPath; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
