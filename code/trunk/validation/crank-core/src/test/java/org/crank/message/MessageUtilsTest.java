package org.crank.message;

import java.util.ResourceBundle;


import junit.framework.TestCase;

public class MessageUtilsTest extends TestCase {

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void test () throws Exception {
        String label = MessageUtils.getLabel( "foo", ResourceBundle.getBundle( "org/crank/message/resources" ));
        assertNotNull(label);
        assertEquals( "Foo", label );

        label = MessageUtils.getLabel( "Dad", ResourceBundle.getBundle( "org/crank/message/resources" ));
        assertNotNull(label);
        assertEquals( "Dad", label );
        
        label = MessageUtils.getLabel( "mom.lovesDad", ResourceBundle.getBundle( "org/crank/message/resources" ));
        assertNotNull(label);
        assertEquals( "Mom Loves Dad", label );

    }

}

