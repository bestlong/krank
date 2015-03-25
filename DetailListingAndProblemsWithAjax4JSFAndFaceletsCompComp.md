Facelets has composition components which are very similar to working with Facelets templates. You can for example pass a composition component ui:defines tag in its body as well at attributes. In the composition component you use named ui:inserts to pull the ui:defines into various parts of the body. The docs for this are a bit sketchy, but I did write a prototype and it worked.

However when using named ui:inserts/ui:defines with Ajax4JSF the named ui:inserts don't seem to work. Tom has documented more about this here and here (TBD).

For example here is how we would like to use detailListing composition component:

```
<crank:detailListing detailController="${taskDetailController}"

                    propertyNames="name,startDate,endDate,complete">

	<ui:define name="formBody">

            <h:outputText value="Hello World!" />

        </ui:define>

	<ui:define name="columActions">

 	    <a4j:commandLink action="#{hello.sayHello}" value="hello" >
											  
                  <f:param name="id" value="${row.id}" />
										  
            </a4j:commandLink>

        </ui:define>
		
</crank:detailListing>

```

Prototypes of the above technique worked until we combined it with Ajax4JSF. Essentially we are passing two area of the detailController (similar to a facet in JSF), namely the form body and the extra actions we want to show up per row in the listing.


It would have been great if the above worked with Ajax4JSF but it does not. The work around is to submit issues to the Ajax4JSF mailing list and the Facelets mailing list, and then add the following hack until we hear back (last time we submitted something to the Ajax4JSF guys, it was decided that it was a "feature" not a bug):

```
<crank:detailListing detailController="${taskDetailController}"
	propertyNames="name,startDate,endDate,complete"
	useFormBody="${true}">

		<h:outputText value="Hello World!" />
	
</crank:detailListing>

```

The above passes a form body. Note the use of useFormBody attribute.

```
<crank:detailListing detailController="${taskDetailController}"
	propertyNames="name,startDate,endDate,complete"
	useFormBody="${false}">

        <a4j:commandLink action="#{hello.sayHello}" value="hello" >
            <f:param name="id" value="${row.id}" />
        </a4j:commandLink>
	
</crank:detailListing>

```

The above passes colum actions.

The hack works as long as you want a form body or column actions. What if you want both?

Good question. Until we hear back from the JSF heroes over at Facelets and Ajax4JSF we are going to create two tags as follows:

  * 

&lt;crank:detailForm /&gt;


  * 

&lt;crank:detailTable /&gt;



Then you will be able to pass each of these a body and it will get used where apropos.

If someone else has some other ideas for workaround or better yet a solution to our problem. Please let me know.

For internal team members: We need this for FMT so it has to get done.