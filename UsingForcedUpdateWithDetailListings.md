# Introduction #

By default when dealing with the master detail framework in Crank if you cancel the outer object, the changes you made to the detail listing are rolled back (as they were never synced with the database). For example, you open up an `Employee` form and add three `Task`s to the `Employee`, and change a Task details (update task description), then decide that you are editing the wrong `Employee`, if you hit cancel, no changes will be written to the database.

However, if you want the `Task`s to be written to the database as soon as the user hits add button (instead of just relying on the `Task`s being added to the `Employee`.`tasks` list and letting the cascade operation add them to the db), then this documents shows you how to set this up. Note when you do this, the outer cancel button will no longer roll back the changes.




# Details #

Prior to updating the sample app (crank-crud-webapp) to have this feature, its configuration for employeeCrud looked as follows:

```
	@Bean(scope = DefaultScopes.SESSION, aliases = "empCrud")
	public JsfCrudAdapter employeeCrud() throws Exception {
				
		
		JsfCrudAdapter adapter = cruds().get("Employee");
		...

		/* Setup tasks and contacts DetailControllers. */
		JsfDetailController taskController = new JsfDetailController(Task.class);
		adapter.getController().addChild("tasks",
				taskController);
		adapter.getController().addChild("contacts",
				new JsfDetailController(ContactInfo.class));
		...

		return adapter;
	}
```

In order to enable `forcedUpdate`s to the database you need to need to pass the `taskController` a `dao` object and set the `forceUpdate` property to true as follows:

```
	@Bean(scope = DefaultScopes.SESSION, aliases = "empCrud")
	public JsfCrudAdapter employeeCrud() throws Exception {
				
		
		JsfCrudAdapter adapter = cruds().get("Employee");
		...

		/* Setup tasks and contacts DetailControllers. */
		JsfDetailController taskController = new JsfDetailController(Task.class);
		adapter.getController().addChild("tasks",
				taskController);
		taskController.setForceUpdate(true);  //*********** LOOK
		taskController.setDao(repos().get("Task")); //****  HERE
		adapter.getController().addChild("contacts",
				new JsfDetailController(ContactInfo.class));
		...

		return adapter;
	}
```

Note that we had to add Task as a managed bean as well

```
	@Bean(scope = DefaultScopes.SINGLETON)
	public List<CrudManagedObject> managedObjects() {
		if (managedObjects == null) {
			managedObjects = new ArrayList<CrudManagedObject>();
			managedObjects.add(new CrudManagedObject(Employee.class,
					EmployeeDAO.class));
                        ...		
			managedObjects.add(new CrudManagedObject(Task.class));

```