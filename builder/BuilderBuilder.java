package building.Javanese.builder;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.nio.file.Path;
import java.util.Set;


/** The builder builder proper to Javanese Builder.
  */
public final class BuilderBuilder extends building.Javanese.BuilderBuilderDefault {


    public BuilderBuilder() {
        super( "building.Javanese", building.Javanese.Bootstrap.buildingProjectPath ); }


    public @Override Set<String> addedBuildingCode() {
        return Set.of( "building.Javanese", "building.Javanese.template" ); }


    public @Override Set<String> externalBuildingCode() { return Set.of(); }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
