# Introduction #

A new feature has been added to the Crank Crud Controller to allow for the deletion of the full entity.  Before this feature, Crank Crud invoked the GenericDao delete method that removes database rows by executing a query that deletes the row with the entity’s id.  If the entity had collections of related entities, a referential integrity error would usually result. The user would then have to delete each of the children before deleting the entity.

This new feature allows GenericDao’s delete method that performs a remove on the entity to be invoked.  When the entity has collections of other entities that implement the remove option in their cascade strategy, the entities in the collections will also be deleted.

The new feature is implemented by setting a value for the ‘DeleteStrategy” property of the crud controller.  By default, the value is set to “BY\_ID”, so existing controllers will continue to function as they always have.


# Details #


To set a controller to delete full entities –

  1. Override the controller’s delete strategy in its configuration.
  1. Be sure to use the controller for the listing as well as the crud form.
  1. Be sure that the remove cascade strategy is included for the collections to be deleted with the entity.

For Example -

### Override the controller’s delete strategy in its configuration ###

CrudConfiguration.java

```
    @SuppressWarnings("unchecked")
    @Bean (scope = DefaultScopes.SESSION) 
    public JsfCrudAdapter pixelCrud() throws Exception {
        // Get the Crud Adapter from the usual place.
        JsfCrudAdapter adapter = cruds().get( "PixelBO" );
        
        // Get the controller from the adapter.
        // We need to cast it to a controller in order to get at the method, 
        // which is not in the CrudOperations interface.
        CrudController controller =  (CrudController) adapter.getController();
        
        // Set the controller's delete strategy to entity based. 
        controller.setDeleteStrategy( CrudOperations.DELETE_BY_ENTITY );
        
        // Add a detail controller for the child entities.
        // In this case we have a specific add and remove method, so we'll
        // set overrides for them as well.
        JsfDetailController flowViewPixels = 
            new JsfDetailController(FlowViewPixelBO.class);
        flowViewPixels.getRelationshipManager().setAddToParentMethodName( "addFlowViewPixels" );
        flowViewPixels.getRelationshipManager().setRemoveFromParentMethodName( "removeFlowViewPixels" );
        controller.addChild("flowViewPixels", flowViewPixels);
        return adapter;
    }

```

### Be sure to use the controller for the listing as well as the crud form ###

Listing.xhtml

```
...
<ui:composition template="/templates/layout.xhtml">
    <ui:define name="content">

        <c:set var="daoName" value="PixelBO" />
        <!-- Adapter holding our overriden controller -->
        <c:set var="adapter" value="${pixelCrud}" />
        <c:set var="crud" value="${adapter.controller}" />
        <c:set var="pageTitle" value="Pixels" />
        <c:set var="listPropertyNames" 
                value="" />
        
        <h:form>
...
```

Form.xhtml

```
...
<ui:composition template="/templates/layout.xhtml">
    <ui:define name="content">
    
        <c:set var="daoName" value="PixelBO" />
        <!-- Adapter holding our overriden controller -->
        <c:set var="adapter" value="${pixelCrud}" />
        <c:set var="crud" value="${adapter.controller}" />
        <c:set var="pageTitle" value="Pixel" />
        <c:set var="propertyNames" 
               value="name,description,active,mode,type,pixelCode" />
        <c:set var="flowViewPixelCrud" value="${crud.children.flowViewPixels}" />               

...
```

### Be sure that the remove cascade strategy is included for the collections to be deleted with the entity ###

PixelBO.java
```

    // CascadeType.ALL includes remove.
    @OneToMany( cascade = CascadeType.ALL, mappedBy = "pixel" )
    private List<FlowViewPixelBO > flowViewPixels;
    

```