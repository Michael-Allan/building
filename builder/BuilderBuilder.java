package building.builder;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.util.Set;


/** The builder builder proper to the building project.
  */
public final class BuilderBuilder extends building.BuilderBuilderDefault {


    public BuilderBuilder() { super( "building", building.Bootstrap.buildingProjectPath ); }


    public @Override Set<java.nio.file.Path> addedBuildingCode() { return Set.of( projectPath() ); }


    public @Override Set<String> externalBuildingCode() { return Set.of(); }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
