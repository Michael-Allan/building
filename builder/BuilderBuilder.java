package building.builder;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;
import java.util.Set;


/** The builder builder proper to the building project.
  */
public final class BuilderBuilder extends building.BuilderBuilderDefault {


    public BuilderBuilder() { super( "building", building.Bootstrap.buildingProjectPath ); }


    public @Override Set<Path> addedBuildingCode() {
        final Path p = projectPath();
        return Set.of( p, p.resolve("template") ); }


    public @Override Set<String> externalBuildingCode() { return Set.of(); }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
