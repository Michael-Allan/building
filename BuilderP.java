package building;

// Changes to this file immediately affect the next runtime.  Treat it as a script.


/** The software builder proper to the building project.
  */
public final class BuilderP implements Builder {


    public @Override void build( final String target ) throws UserError {
        if( !Target.builder.name().equals( target )) {
            throw new UserError( "build: Unrecognized target ‘" + target + '‘' ); }}}
        // Nothing to do, already this builder is built.



                                                        // Copyright © 2020  Michael Allan.  Licence MIT.
