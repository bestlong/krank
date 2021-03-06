/*
 * $Id: SelectItemsIterator.java,v 1.16 2007/01/29 07:56:05 rlubke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at
 * https://javaserverfaces.dev.java.net/CDDL.html or
 * legal/CDDLv1.0.txt. 
 * See the License for the specific language governing
 * permission and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.    
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * [Name of File] [ver.__] [Date]
 * 
 * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
 */

package org.crank.javax.faces.component;


import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Map;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.UISelectItems;
import javax.faces.model.SelectItem;

import com.sun.faces.util.TypedCollections;


/**
 * <p>Package private class for iterating over the set of {@link SelectItem}s
 * for a parent {@link UISelectMany} or {@link UISelectOne}.</p>
 */

final class SelectItemsIterator implements Iterator<SelectItem> {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct an iterator instance for the specified parent component.</p>
     *
     * @param parent The parent {@link UIComponent} whose children will be
     *  processed
     */
    public SelectItemsIterator(UIComponent parent) {

        kids = parent.getChildren().listIterator();

    }


    // ------------------------------------------------------ Instance Variables


    /**
     * <p>Iterator over the SelectItem elements pointed at by a
     * <code>UISelectItems</code> component, or <code>null</code>.</p>
     */
    private Iterator<SelectItem> items = null;


    /**
     * <p>Iterator over the children of the parent component.</p>
     */
    private ListIterator<UIComponent> kids = null;


    // -------------------------------------------------------- Iterator Methods


    /**
     * <p>Return <code>true</code> if the iteration has more elements.</p>
     */
    public boolean hasNext() {

        if (items != null) {
            if (items.hasNext()) {
                return (true);
            } else {
                items = null;
            }
        }
        Object next = findNextValidChild();
        if (next != null) {
            kids.previous();
            return true;
        }
        return false;

    }


    /**
     * <p>Return the next element in the iteration.</p>
     *
     * @throws NoSuchElementException if there are no more elements
     */
    @SuppressWarnings("unchecked")
	public SelectItem next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (items != null) {
            return (items.next());
        }
        UIComponent kid = (UIComponent) findNextValidChild();
        if (kid instanceof UISelectItem) {
            UISelectItem ui = (UISelectItem) kid;
            SelectItem item = (SelectItem) ui.getValue();
            if (item == null) {
                item = new SelectItem(ui.getItemValue(),
                                      ui.getItemLabel(),
                                      ui.getItemDescription(),
                                      ui.isItemDisabled(), 
                                      ui.isItemEscaped());
            }
            return (item);
        } else if (kid instanceof UISelectItems) {
            UISelectItems ui = (UISelectItems) kid;
            Object value = ui.getValue();
            if (value instanceof SelectItem) {
                return ((SelectItem)value);
            } else if (value instanceof SelectItem[]) {
                items = Arrays.asList((SelectItem[]) value).iterator();
                return (next());
            } else if (value instanceof List) {
                items = TypedCollections.dynamicallyCastList((List) value, SelectItem.class).iterator();
                return (next());
            } else if (value instanceof Map) {
                List<SelectItem> list = new ArrayList<SelectItem>(((Map) value).size());
                for (Iterator keys = ((Map) value).keySet().iterator();
                    keys.hasNext(); ) {

                    Object key = keys.next();
                    if (key == null) {
                        continue;
                    }
                    Object val = ((Map) value).get(key);
                    if (val == null) {
                        continue;
                    }
                    list.add(new SelectItem(val, key.toString(),
                        null));

                }

                items = list.iterator();
                return (next());
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            throw new NoSuchElementException();
        }

    }


    /**
     * <p>Throw UnsupportedOperationException.</p>
     */
    public void remove() {

        throw new UnsupportedOperationException();

    }

    private Object findNextValidChild() {
        if (kids.hasNext()) {
            Object next = kids.next();
            while (kids.hasNext() && !(next instanceof UISelectItem || next instanceof UISelectItems)) {
                next = kids.next();
            }
            if (next instanceof UISelectItem || next instanceof UISelectItems) {
                return next;
            }
        }
        return null;
    }
}
