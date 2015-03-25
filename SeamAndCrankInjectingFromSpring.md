Because of the Variable resolver we added to faces-config, we can simple inject any Spring bean using

```
@In("#{Name}")
```

and here is a example of where I use it.. enjoy

```
@Name("crudBacking")
@Scope(ScopeType.SESSION)
@AutoCreate
public class CrudBacking {

	@In("#{cruds}")
	Map<String, JsfCrudAdapter<? extends Serializable, ? extends Serializable>> cruds;
	
	@In("#{paginators}")
	Map<String , FilterablePageable> paginators;
	
	@SuppressWarnings("unchecked")
	@Factory("employeeCRUD")
	public JsfCrudAdapter<? extends Serializable, ? extends Serializable> getEmployeeCrud() throws InstantiationException, IllegalAccessException {
		final JsfCrudAdapter<? extends Serializable, ? extends Serializable> adapter = cruds.get(CrudUtils.getClassEntityName(Employee.class));
		Serializable temp = adapter.getController().getEntity();
		temp = adapter.getController().getEntityClass().newInstance();
		adapter.getController().addCrudControllerListener(new CrudControllerListener() {

			public void afterCancel(CrudEvent event) {
			}

			public void afterCreate(CrudEvent event) {
			}

			public void afterDelete(CrudEvent event) {
			}

			public void afterLoadCreate(CrudEvent event) {
			}

			public void afterLoadListing(CrudEvent event) {
			}

			public void afterRead(CrudEvent event) {
			}

			public void afterUpdate(CrudEvent event) {
			}

			public void beforeCancel(CrudEvent event) {
			}

			public void beforeCreate(CrudEvent event) {
				createThumbNail((Employee)event.getEntity());
			}

			public void beforeDelete(CrudEvent event) {
			}

			public void beforeLoadCreate(CrudEvent event) {
			}

			public void beforeLoadListing(CrudEvent event) {
			}

			public void beforeRead(CrudEvent event) {
			}

			public void beforeUpdate(CrudEvent event) {
				createThumbNail((Employee)event.getEntity());
			}

			private void createThumbNail(final Employee employee) {
				// Need to create the ThumbNail;
				try {
					PersistedFile picture = employee.getPicture().getPicture();
					byte[] thumbNailBytes = ImageUtil.getThumbNail(picture.getBytes(), picture.getContentType(),32);
					byte[] thumbNailBytesBig = ImageUtil.getThumbNail(picture.getBytes(), picture.getContentType(),64);
					PersistedFile thumbNail = new PersistedFile();
					thumbNail.setBytes(thumbNailBytes);
					thumbNail.setContentType(picture.getContentType());
					thumbNail.setName("t" + picture.getName());
					employee.getPicture().setThumbNail(thumbNail);
					employee.getPicture().getPicture().setBytes(thumbNailBytesBig);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		return adapter;
	}
	
	
	@SuppressWarnings("unchecked")
	@Factory("userGroupCRUD")
	public JsfCrudAdapter<? extends Serializable, ? extends Serializable> getUserGroupCrud() throws InstantiationException, IllegalAccessException {
		final JsfCrudAdapter<? extends Serializable, ? extends Serializable> adapter = cruds.get(CrudUtils.getClassEntityName(UserGroup.class));
		return adapter;
	}
	
	@SuppressWarnings("unchecked")
	@Factory(autoCreate=true , value="userCRUD")
	public JsfCrudAdapter<? extends Serializable, ? extends Serializable> getUserCrud() throws InstantiationException, IllegalAccessException {
		final JsfCrudAdapter<? extends Serializable, ? extends Serializable> adapter = cruds.get(CrudUtils.getClassEntityName(User.class));
		return adapter;
	}
```


And you can use any of these as normal in your facelets file.

```
<c:set var="controller" value="${employeeCRUD.controller}" />
		<a4j:form id="employeeForm" enctype="multipart/form-data">
			<h:panelGroup rendered="${controller.showForm}">
				<crank:form crud="#{controller}" propertyNames="firstName,lastName"
					parentForm="employeeForm">
					<crank:compositePanel entity="${controller.entity.picture}"
						name="picture" propertyNames="picture" />
				</crank:form>
			</h:panelGroup>
		</a4j:form>

		<a4j:form id="employeeListingForm">
			<rich:messages infoClass="infoClass" errorClass="errorClass"
				layout="table" />

			<crank:listing jsfCrudAdapter="${employeeCRUD}"
				propertyNames="firstName,lastName,picture.thumbNail"
				parentForm="employeeListingForm" />
		</a4j:form>	
```