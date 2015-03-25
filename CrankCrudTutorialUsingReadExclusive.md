# Introduction #

The GenericCrudDoa’s readExclusive(PK):Entity method allows the user to obtain an entity and exclusive access to it for duration of the transaction.

Determining the database concurrency plan is always a difficult step in any application design.  A purely pessimistic plan insures that data will never be lost to concurrency issues but carries a tremendous cost in throughput time, whereas a purely optimistic plan provides the fastest possible throughput at the cost of occasional missed updates.  For most applications, neither extreme is acceptable and the concurrency plan seeks to balance the risk of lost updates against the advantage of improved throughput.

There are occasions, however, when the application must be absolutely certain that only the current operation has access to a specific entity.  When, for example, an application needs to generate serial numbers, the application might have a service that performs this function and stores the last number generated in a database.  The number generating service needs to be sure that it and only it can change the last number used.

In Crank CRUD this kind of exclusive lock can be obtained by using the GenericCrudDoa’s readExclusive(PK):Entity method.  This method has the same signature as the standard read, and retrieves the requested entity from the database just as the standard read does.  However, it also locks the database row containing the entity, thereby preventing any other application from affecting it.  The lock is implemented by the database, so the row is safe not only from the application, but from anything thing else that might be accessing the database.

There are a few things to bear in mind when using this method:

  1. The entity being retrieved must implement JPA version control (@Version tag on one of its properties.)
  1. The method must be called from within a transaction.
  1. The entity is locked until the transaction commits/rolls back.
  1. Because the method is in a transaction, the state of the entity will be persisted to the database when the transaction is committed, unless, of course, the transaction is rolled back.



## Crank CRUD readExclusive Example ##

```
// Note that the method must be invoked within a transaction.
@Transactional
public int getNextSerialNumber() {
	// The readExclusive method retrieves the entity and locks the
	// entity's database row.
	SerialNumber serialNumber = serialNumberDao.readExclusive(1l);
		
	// The last serial number used is incremented and set back 
	// into the entity. 
	int lastSerialNumber = serialNumber.getLastNumber();
	lastSerialNumber++;
	serialNumber.setLastNumber(lastSerialNumber);
		
	return lastSerialNumber;
}

// Upon commitment of the transaction, the entity's row
// is persisted and the lock is released. 

```