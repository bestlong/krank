# Introduction #
I just implemented case insensitive sorts and filters

I think this test shows how to use the Criteria DSL with case sensitive sorts and filters.

# Details #
```
package org.crank.crud;

import org.crank.crud.criteria.OrderBy;
import org.crank.crud.criteria.OrderDirection;
import org.crank.crud.criteria.Group;
import org.crank.crud.criteria.Comparison;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: richardhightower
 * Date: Feb 6, 2009
 * Time: 11:22:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class CriteriaUtilsTest extends TestCase
{
    public void testOrderBy () {
        /* Create a list of OrderBy objects. */
        List<OrderBy> list = new ArrayList<OrderBy>();
        list.add(new OrderBy("foobar", OrderDirection.ASC));
        list.add(new OrderBy("baz", OrderDirection.DESC));
        /* Make this OrderBy one that is case insensitive. */
        OrderBy bacon = new OrderBy("bacon", OrderDirection.DESC);
        list.add(bacon);
        bacon.setCaseSensitive(false);
        /* Convert the list into an array. */
        OrderBy[] ob = list.toArray(new OrderBy[list.size()]);
        /* Invoke the utility class that constructs the Order By. */
        String orderByClause = CriteriaUtils.constructOrderBy(ob);
        assertEquals(" ORDER BY o.foobar ASC, o.baz DESC, UPPER( o.bacon ) DESC", orderByClause);
    }


    public void testWhereClause () {
        Group group = new Group();
        group.eq("lastName", "Jones").eq("firstName", "Tom");
        Comparison comparison = Comparison.eq("middleName", "Milhouse");
        comparison.setCaseSensitive(false);
        group.add(comparison);
        String whereClause = CriteriaUtils.constuctWhereClause(group);
        assertEquals(" WHERE  o.lastName = :lastName  AND  o.firstName = :firstName  " +
                "AND  UPPER(  o.middleName )  =  UPPER( :middleName  ) ", whereClause);

    }


}

```