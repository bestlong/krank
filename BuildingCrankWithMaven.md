To build from source you have to checkout the whole stack of stuff...

```
svn co http://krank.googlecode.com/svn/trunk crank 
cd crank 
mvn install 
```

That will build and install all of crank.

You can run the sample with the jetty plugin like this;

```
cd examples/crank-crud-webapp-sample 
mvn jetty:run 
```

then you can go to;

http://localhost:8080/crank-crud-webapp

and see the example app.

## Alternative Build Stuff ##

The build by default will use hibernate for the jpa implementation and mysql for the database.

However if you don't want to hassle with setting up and installing mysql you can use hsqldb.

When you build you specify the db to use with the db property like this;

```
mvn -Ddb=hsqldb -Djpa=hibernate clean install
```

and that will build everything in the stack using hsql instead of mysql. You can run the examples with hsqldb as well, just include the -Ddb=hsqldb -Djpa=hibernate options on the same command line you use to start jetty.

The plan is to eventually move to hsqldb and/or derby as the default db but we gotta take some baby steps first.