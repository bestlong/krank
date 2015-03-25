AbstractTreeModelBuilder turns a list into a tree model that can be used by GUIs (Tomahawk, Swing, Dojo, RichFaces).

Takes directions on how to build the tree as a String or String array. Then it uses these directions to build a tree.

This is a base class whose subclasses can work with the two tree models from Tomahawk, and possible in the future ADF and/or IceFaces models.

The base classes just need to override the following:

```
protected abstract Object createTableModel(Object root); 
protected abstract void addToNode(Object parent, Object child); 
protected abstract Object createNode(String name);
```

In addition, the class has several extention points (protected methods) for classes that want to process nodes a bit different than the default.

Given the directions:
```
Departments->this.name->employees.name;groups.title->employees.pets.cuteName
```

It would build a tree like this:
```
 Departments
  	     ->this.name
          ->employees.name
              -> employees.pets.cuteName
          ->groups.title
```

Thus this list:
```
Department cas = new Department("cas");  
cas.getEmployees().add(new Employee("Rick Hightower"));  
cas.getEmployees().add(new Employee("David E."));  
cas.getGroups().add(new Group("PRESTO"));  
cas.getGroups().add(new Group("ArcMind"));  list.add(cas);    
Department hrIt = new Department("hrIt");  
Employee angelica = new Employee("Angelica W.");  
angelica.getPets().add(new Pet("Mooney"));  
hrIt.getEmployees().add(angelica);  
hrIt.getEmployees().add(new Employee("Gordon B."));  
hrIt.getGroups().add(new Group("PRESTO2"));  
hrIt.getGroups().add(new Group("ArcMind2"));    
list.add(hrIt);  
```

Would produce a tree like this:
```
 Departments
 	    	    -> cas
 	    	    	    -> employees
 	    	    	    	    -> Rick Hightower
 	    	    	    	    -> David E.
 	    	    	    -> groups
 	    	    	    	    -> PRESTO
 	    	    	    	    -> ArcMind
 	    	    -> hrIt
 	    	    	    -> employees
	    	    	    	    -> Angelica W.
                                  -> Mooney
 	    	    	    	    -> Gordon B.
 	    	    	    -> groups
 	    	    	    	    -> PRESTO2
 	    	    	    	    -> ArcMind2
```

#### UML Class Diagram Showing TreeBuilders ####
> ![http://krank.googlecode.com/svn/wiki/img/tree-builder.png](http://krank.googlecode.com/svn/wiki/img/tree-builder.png)

For some code examples and such read this (which is related):

We need a way to easily create a tree model. I have a way already cooked up. We just need to add support for RichFaces tree.

See http://www.thearcmind.com/confluence/display/SpribernateSF/Working+with+Rollups,+Hibernate+and+Tree+model+generation+redux
and http://www.thearcmind.com/confluence/display/SpribernateSF/Tree+model+generation

http://www.thearcmind.com/confluence/display/SpribernateSF/Integrating+Hibernate+and+JSF+so+you+can+easily+pass+JSF+expressions+as+Hibernate+Query+params
