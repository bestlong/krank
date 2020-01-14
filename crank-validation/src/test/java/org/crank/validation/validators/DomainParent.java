package org.crank.validation.validators;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DomainParent {
	private Logger log = LogManager.getLogger(this.getClass());
	private List<DomainChild> children = new ArrayList<DomainChild>();

	public List<DomainChild> getChildren() {
		return children;
	}

	public void setChildren(List<DomainChild> children) {
		this.children = children;
	}
	
	/**
	 * This is the domain method that will be invoked by the child domain validator
	 * @param child
	 * @param value
	 * @throws Exception 
	 */
	public void validateChildren(DomainChild child, int value) throws Exception {

		if (children != null) {
			// Validate the new child value summed with the children does not exceed 100
			int sum = 0;
			
			for (DomainChild c : children) {
				if (c != child) {
					log.info("Adding " + c.getScaleFactor() + " from child '" + c.getName() + "'");
					sum += c.getScaleFactor();
				}
			}
			
			// Add the new value to the sum
			log.info("Appending " + child.getScaleFactor() + " from validated child '" + child.getName() + "'");
			sum += value;
			
			if (sum > 100) {
				throw new Exception("The scale factors for all children exceed the limit of 100: " + sum + "\n" +
						            "The current child (" + child.getName() + ") must be modified before the value can be saved!");
			}
		}	
		
	}
	
}
