# Introduction #

Keeping the kids in order.

# Details #

In a future release, the JsfDetailController will no doubt be enhanced to use a relationship manager that is aware of the FilterablePageable “chicken richness” that is currently available to the DataSource based controllers.  In the meantime, some child collections of some entities may need to be ordered for presentations sake.  When order is needed, it can be enforced by using the new orderByComparator property.  This new property takes a java.util.Comparator that will be applied to the collection of child entities returned from the parent.

It is optional and may be implemented in the configuration class as follows:

```

@SuppressWarnings( "unchecked" )
@Bean( scope = DefaultScopes.SESSION )
public JsfCrudAdapter parentEntityCrud() throws Exception {
    JsfCrudAdapter adapter = cruds().get( "ParentEntity" );
    JsfDetailController childController = 
    	new JsfDetailController( ChildEntity.class );
    childController.setOrderByComparator( new Comparator<ChildEntity>() {

	    public int compare( ChildEntity o1, ChildEntity o2 ) {
		if (o1.getOrdinal() > o2.getOrdinal()) {
		    return 1;
		}
		if (o1.getOrdinal() < o2.getOrdinal()) {
		    return -1;
		}
		return 0;
	    }
	} );

    adapter.getController().addChild( "kids", childController );
    return adapter;
}

```