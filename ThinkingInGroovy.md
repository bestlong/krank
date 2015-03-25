# Introduction #

Random thoughts about using Groovy to write a code generator for Crank.

# Details #

I am new to Groovy. I a have been putting it off for two things: good IDE support, and support for Java 5 features like Generics and Enums. Well JetBrains has a great plugin for Groovy and Groovy 1.5 has the Java 5 features so here I am.

The IDEA plugin for Groovy is excellent. It provides code completion in areas that I would think were not possible. (The last time I tried the Groovy Eclipse plugin, I was majorly dissappointed, and after my recent "Thanks Zed" blog, I learned that IDEA had a great groovy plugin.)

I have played around with Groovy before but still consider myself a complete novice. Here is what I have come up with so far (by just fooling around):

```
package com.arcmind.codegen

class Table {
    String name    

}
```

The above defines a Java class called Table with a name property with a getter and setter.

Here is a little JDBC utility class that uses a closure:

```
package com.arcmind.codegen
import java.sql.*;

class JdbcUtils {
    String url
    String driver
    String userName
    String password

    def execute(callme) {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,userName,password);
        try {
            callme(connection);
        }   catch (SQLException sqe) {
            throw new RuntimeException(sqe);
        } finally {
            connection.close();
        }
        
    }
}

```

I thought about using Groovy's GSQL but did not know how to access the database metadata from GSQL.

Now I use the two classes to read the metadata (table name at this point) as follows:

```
package com.arcmind.codegen
import java.sql.*;

class DataBaseMetaDataReader {
    List <Table> tables = []
    static main (args) {
        DataBaseMetaDataReader reader = new DataBaseMetaDataReader()
        JdbcUtils jdbcUtils = new JdbcUtils();
        jdbcUtils.url = "jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true"
        jdbcUtils.userName = "crank"
        jdbcUtils.password =  "crank"
        jdbcUtils.driver = "com.mysql.jdbc.Driver"
        
        jdbcUtils.execute ({ Connection connection ->
               def resultSet = connection.metaData.getTables (null, null, null, null)
               while(resultSet.next()) {
                   Table table = new Table()
                   table.name = resultSet.getString ("TABLE_NAME")
                   reader.tables << table
               }
        });

        reader.tables.each {Table table ->
            println table.name
        }

    }
}
```

### Creating another closure for iteration and using Groovy-style constructors ###


I figured there was a built-in way to use closures with ResultSet but could not find it. If someone can point to a more groovy way to get database metadata, let me know.

I took another swipe at it to add my own groovyness as follows:

```
package com.arcmind.codegen
import java.sql.*;

class JdbcUtils {
    String url
    String driver
    String userName
    String password

    def execute(callme) {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(url,userName,password);
        try {
            callme(connection)
        }  finally {
            connection.close()
        }
        
    }

    def iterate(ResultSet resultSet, callme) {
        try {
            while (resultSet.next()) {
                callme(resultSet)
            }
        } finally {
            resultSet.close()
        }

    }
}

```

Notice I added the iterate method that takes a closure. Now we use the closure and tried to make the code more groovy by using the property setters in the constructor call.

```
package com.arcmind.codegen
import java.sql.*;

class DataBaseMetaDataReader {
    List <Table> tables = []
    static main (args) {
        DataBaseMetaDataReader reader = new DataBaseMetaDataReader()
        JdbcUtils jdbcUtils = new JdbcUtils(url:"jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true",
                userName:"crank", password:"crank", driver:"com.mysql.jdbc.Driver");

        jdbcUtils.execute ({ Connection connection ->
               jdbcUtils.iterate(connection.metaData.getTables (null, null, null, null),
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         reader.tables << table
                       }
               )
        });

        reader.tables.each {Table table ->
            println table.name
        }

    }
}
```

So far I am not sure that Groovy has helped me much yet. I could have done the above in Java but faster. Mainly because I don't know Groovy yet. The code is much shorter. Later I need to use the Groovy CLI and Groovy templates. I thinnk Groovy will be a good fit for code generation for Crank. I will try to write my experiences as I go.

### I spent about another 1/2 hour on my code generator ###

I am working on this a bit at a time at night after I finish all my real tasks, chores and such. I just finished an article for IBM on JSF and posted another story on DZone.

```
package com.arcmind.codegen
import java.sql.*;

class DataBaseMetaDataReader {
    List <Table> tables = []
    String catalog
    String schema
    List <String> tableTypes = ["TABLE"]
    JdbcUtils jdbcUtils

    def processTables() {
        jdbcUtils.execute ({ Connection connection ->
               //jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes),
               jdbcUtils.iterate(connection.metaData.getTables (null, null, null, null),
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         tables << table
                       }
               )
        });
    }

    def processColumns() {
        tables.each {Table table ->
            println table.name
            jdbcUtils.execute ({ Connection connection ->
                   //jdbcUtils.iterate(connection.metaData.getColumns(catalog, schema, table.name, null)
                   jdbcUtils.iterate(connection.metaData.getColumns(null, null, table.name, null),
                           { ResultSet resultSet ->
                             Column column = new Column()
                             column.name = resultSet.getString ("COLUMN_NAME")
                             int type = resultSet.getInt ("DATA_TYPE")
                             println "${column.name} ${type}"
                             table.columns << column;
                           }
                   )
            })
        }
    }
    
    static main (args) {

        JdbcUtils jdbcUtils = new JdbcUtils(url:"jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true",
                userName:"crank", password:"crank", driver:"com.mysql.jdbc.Driver");
        DataBaseMetaDataReader reader = new DataBaseMetaDataReader()
        reader.jdbcUtils = jdbcUtils
        reader.processTables()
        reader.processColumns()
    }
}
```

```
package com.arcmind.codegen

class Table {
    String name    
    List <Column> columns = []
}
```

```
package com.arcmind.codegen;

public enum ColumnType {
    VARCHAR, INTEGER, LONG;
}

```

Note that ColumnType is in Java not groovy when I tried to do an enum in Groovy, IDEA barfed.

```
package com.arcmind.codegen

class Column {
    String name
    ColumnType type
}
```

It still has a way to go. I want to try out the CLI and Groovy template soon. I still need to convert Table/Column to Class/Property. Now I go to bed.

### Another late night with my new toy ###
Okay here is my first Groovy complaint. I have this class as follows:

```
...
class DataBaseMetaDataReader {
    List <Table> tables = []
    String catalog
    String schema
    List <String> tableTypes = ["TABLE"]
    JdbcUtils jdbcUtils

    def processTables() {
        jdbcUtils.execute ({ Connection connection ->
               jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes.toArray(new String[tableTypes.size()])),
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         tables << table
                       }
               )
        });
    }
...
```

Focus on List `<String> tableTypes` and `tableTypes.toArray(new String[tableTypes.size()]))`. Now my assumption was that this would work

```
...
class DataBaseMetaDataReader {
    List <Table> tables = []
    String catalog
    String schema
    List <String> tableTypes = ["TABLE"]
    JdbcUtils jdbcUtils

    def processTables() {
        jdbcUtils.execute ({ Connection connection ->
               jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null,  
                       tableTypes,
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         tables << table
                       }
               )
        });
    }
...
```

Say what... Yes I expected Groovy would convert the `List` into a `String array` automatically. Why you say? Why would I expect such a thing? Well, because Jython (then called JPython) could do it 10 years ago. Just a thought.... Just trying to use some pent up Jython Foo on a Groovy project.

This is not a complaint, but a feature request. Maybe it does not fit the language design but it seems like it should happen.

Then I figured maybe it is becuase I typed it to begin with so I tried this:

```
class DataBaseMetaDataReader {
    List <Table> tables = []
    String catalog
    String schema
    def tableTypes = ["TABLE"]
    JdbcUtils jdbcUtils

    def processTables() {

        jdbcUtils.execute ({ Connection connection ->
               //jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes.toArray(new String[tableTypes.size()])),
               jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes),
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         tables << table
                       }
               )
        });
    }

```

Nope. This works in Jython. Should it work in Groovy?

I can't deal with `tableTypes.toArray(new String[tableTypes.size()])` in a scripting language. Yikes.

Okay so I settled on this:

```
class DataBaseMetaDataReader {
    List <Table> tables = []
    String catalog
    String schema
    String[] tableTypes = ["TABLE"]
    JdbcUtils jdbcUtils

    def processTables() {

        jdbcUtils.execute ({ Connection connection ->
               jdbcUtils.iterate(connection.metaData.getTables (catalog, schema, null, tableTypes),
                       { ResultSet resultSet ->
                         Table table = new Table()
                         table.name = resultSet.getString ("TABLE_NAME")
                         tables << table
                       }
               )
        });
    }

```

I wanted the list to be mutable, but not that much. It is really only going to be ["TABLE"] or ["TABLE", "VIEW"].

## More fun with Groovy ##

A bit at a time. Today I learned a bit more about Groovy. It has been a few days since I played with my new toy. I been busy writing JSF articles over at developeWorks, posting on java.dzone.com(I am a zone leader for Java), working on a new listing feature to join abstract class properties (done BTW), and then keeping up with four kids.

Trying to figure out how to parse table names like EMPLOYEE, DEPT\_FOO to Employee and DeptFoo using Groovy.

I needed to figure out how to use the String indexing notation which is similar to Pythons (but its been a while since I pythoned as well). I tried a few combos (and read the docs). Enter stage left... Groovy Console. Fired up the up Groovy console, typed some Groovy code (or what I thought was Groovy code), and it yelled at me (no thats not it). Finally bent it to my will. I really dig the Groovy console... I forgot how much fun it was to use an iteractive console....

This is what I came up with:

```
String tableName = "FOO_BOB"
StringBuilder builder = new StringBuilder()
tableName.split("_").each{ String namePart ->
       builder.append(namePart[0].toUpperCase() + namePart[1..-1].toLowerCase())
}
println builder.toString()
```

The above converts FOO\_BOB to FooBob as expected. I love this syntax namePart[1..-1], and I miss it from my Python days. This is the same as saying namePart.subString(1, namePart.length-1) in Java, i.e.,

```
//Groovy
namePart[1..-1]

//Java
namePart.subString(1, namePart.length-1)
```

For some reason I assumed that "<<" would work with StringBuilder, but not so much.

```
       builder << (namePart[0].toUpperCase() + namePart[1..-1].toLowerCase()) //don't work

```


Now I get this far....

```
...
    static main (args) {

        JdbcUtils jdbcUtils = new JdbcUtils(url:"jdbc:mysql://localhost:3306/crank_crud?autoReconnect=true",
                userName:"crank", password:"crank", driver:"com.mysql.jdbc.Driver");
        DataBaseMetaDataReader reader = new DataBaseMetaDataReader()
        reader.jdbcUtils = jdbcUtils
        reader.processDB()
    }

...
...

    def processDB(){
         processTables()
         processColumns()
         tables.each{Table table ->
            StringBuilder builder = new StringBuilder();
            table.name.split("_").each{ String namePart ->
                    builder.append(namePart[0].toUpperCase() + namePart[1..-1].toLowerCase())
            }
            println builder
            classes << new JavaClass(name:builder.toString())
         }
         classes.each{JavaClass javaClass ->
            println javaClass.name
         }
    }


```

I am having fun with Groovy. Oh yeah... I used the map literal too:

```
static Map <Integer, JavaType> mappings = [(Types.CHAR) : JavaType.STRING , (Types.VARCHAR) : JavaType.STRING]
```

I thought

```
static Map <Integer, JavaType> mappings = [Types.CHAR : JavaType.STRING , Types.VARCHAR : JavaType.STRING]
```

would work, but the keys needed to in parenthesis.

I am really liking the strong typing mixed with scripting... terse, tight and I get code completion from Idea.... I am digging it.