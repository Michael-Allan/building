package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;


/** Default implementation of a builder builder.
  */
public class BuilderBuilderD implements BuilderBuilder {


    /** @see #projectPackage()
      * @see #projectPath()
      */
    public BuilderBuilderD( final String projectPackage, final Path projectPath ) {
        this.projectPackage = projectPackage;
        this.projectPath = projectPath; }



   // ━━━  B u i l d e r   B u i l d e r  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━


    public final String projectPackage() { return projectPackage; }


        private final String projectPackage;



    public final Path projectPath() { return projectPath; }


        private final Path projectPath; }


                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
