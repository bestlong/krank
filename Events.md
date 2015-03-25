`CrankCrudControllerBase` is the base class of all `CrudController`s. It maintains a list of `CrudControllerListener`s and fires `CrudEvent`s when `update`s, `read`s, `loadCreate`s, `delete`s or `create`s happen.

The event system is needed to coordinate subviews.

#### UML Class Diagram Showing Events ####
> ![http://krank.googlecode.com/svn/wiki/img/events.png](http://krank.googlecode.com/svn/wiki/img/events.png)