package org.crank.core;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Date;

public class TypeUtilsTest {

    public static class SuperFoo {
        @SuppressWarnings("unused")
        private String bar;
    }

    public static class Foo extends SuperFoo {
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Test {
        private boolean bool;
        private String text;
        private Date date;
        private java.sql.Date sqlDate;
        private Foo theFoo;

        public Foo getTheFoo() {
            return theFoo;
        }

        public void setTheFoo(Foo theFoo) {
            this.theFoo = theFoo;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean bool) {
            this.bool = bool;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public java.sql.Date getSqlDate() {
            return sqlDate;
        }

        public void setSqlDate(java.sql.Date sqlDate) {
            this.sqlDate = sqlDate;
        }
    }

    @org.testng.annotations.Test
    public void testGetField() {
        assertNotNull(TypeUtils.getField(Test.class, "theFoo.name"));
        assertNotNull(TypeUtils.getField(Test.class, "theFoo.bar"));
    }

    @org.testng.annotations.Test
    public void testGetProperyDescriptor() {
        assertNotNull(TypeUtils
                .getPropertyDescriptor(Test.class, "theFoo.name"));
    }

    @org.testng.annotations.Test
    public void testIsText() {

        assertFalse(TypeUtils.isText(Test.class, "bool"));
        assertTrue(TypeUtils.isText(Test.class, "text"));

    }

    @org.testng.annotations.Test
    public void testIsTextCompoundProperty() {
        assertTrue(TypeUtils.isText(Employee.class, "address.line1"));
    }

    public void testIsBoolean() {
        assertTrue(TypeUtils.isBoolean(Test.class, "bool"));
        assertFalse(TypeUtils.isBoolean(Test.class, "text"));
    }

    @org.testng.annotations.Test
    public void testIsDate() {
        assertFalse(TypeUtils.isDate(Test.class, "bool"));
        assertFalse(TypeUtils.isDate(Test.class, "text"));
        assertTrue(TypeUtils.isDate(Test.class, "date"));
        assertTrue(TypeUtils.isDate(Test.class, "sqlDate"));

    }

}
