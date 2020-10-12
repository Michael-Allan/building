package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.

import java.util.Set;


/** The builder builder proper to the building project.
  */
public final class BuilderBuilderP extends BuilderBuilderDefault {


    public BuilderBuilderP() { super( "building", Bootstrap.buildingProjectPath ); }


    /** Gives an empty set.
      */
    public @Override Set<String> externalBuildingCode() { return Set.of(); }}



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
