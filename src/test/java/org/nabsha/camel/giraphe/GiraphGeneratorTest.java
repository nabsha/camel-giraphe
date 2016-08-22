package org.nabsha.camel.giraphe;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by nabeel on 21/08/16.
 */
public class GiraphGeneratorTest {

    @Test
    public void testActivityGeneration() throws Exception {
        GiraphGenerator giraphGenerator = new GiraphGenerator();
        giraphGenerator.generateActivityFromRoutes();


    }

}