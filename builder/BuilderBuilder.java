package building.Makeshift.builder;

// Changes to this file immediately affect the next build.  Treat it as a build script.

import java.nio.file.Path;
import java.util.Set;


/** The builder builder proper to Makeshift.
  */
public final class BuilderBuilder extends building.Makeshift.BuilderBuilderDefault {


    public BuilderBuilder() {
        super( "building.Makeshift", building.Makeshift.Bootstrap.projectPath ); }


    public @Override Set<String> addedBuildingCode() {
        return Set.of( "building.Makeshift", "building.Makeshift.template" ); }


    public @Override Set<String> externalBuildingCode() { return Set.of(); }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
