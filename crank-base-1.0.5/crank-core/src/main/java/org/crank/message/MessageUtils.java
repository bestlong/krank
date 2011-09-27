package org.crank.message;

import java.util.*;

import org.crank.core.CrankContext;
import org.apache.log4j.Logger;

/**
 * Functions to aid developing JSF applications.
 */
public final class MessageUtils {
	public static final String TOOL_TIP = "toolTip";
	public static final String LABEL_TOOL_TIP = "labelToolTip";
    protected static Logger logger = Logger.getLogger(MessageUtils.class);

    /**
	 * Stops creation of a new MessageUtils object.
	 */
	private MessageUtils() {
	}

	public static String createLabelNoPlural(String fieldName, final ResourceBundle bundle) {
		if (fieldName.endsWith("es")) {
			fieldName = fieldName.substring(0, fieldName.length()-2);
		} else if (fieldName.endsWith("s")) {
			fieldName = fieldName.substring(0, fieldName.length()-1);
		} 
		return getLabel(fieldName, bundle);
	}
	
	
		

	
	/**
	 * Get the field label.
	 *
	 * @param fieldName
	 *            fieldName
	 * @param messageSource
	 *            messageSource
	 *
	 * @return Label from the Message Source.
	 */
	public static String getLabel(final String fieldName,
			final ResourceBundle bundle) {

		String label;

		/** Look for fieldName, e.g., firstName. */
		try {
			label = bundle.getString(fieldName);
		} catch (MissingResourceException mre) {
			label = generateLabelValue(fieldName);
		}

		return label;
	}

	/**
	 * Get the field label.
	 *
	 * @param namespace
	 *            Namespace is a qualifier usually the name of a Form or Entity.
	 * @param fieldName
	 *            fieldName is the name of the label we want to look up.
	 * @param messageSource
	 *            messageSource
	 *
	 * @return Label from the Message Source.
	 */
	public static String createLabelWithNameSpace(final String namespace, final String fieldName,
			final ResourceBundle bundle) {
        logger.debug(String.format("called createLabelWithNameSpace(namespace=%s, fieldName=%s)",namespace, fieldName));

        String label;
		try {
			try {
				/** Look for name-space + . + fieldName, e.g., Employee.firstName. */
				label = bundle.getString(namespace + '.' + fieldName);
                logger.debug(String.format("found with namespace and fieldname: namespace=%s, fieldName=%s, retrieved label=%s", namespace, fieldName, label));
            } catch (MissingResourceException mre) {
				/** Look for fieldName only, e.g., firstName. */
				label = bundle.getString(fieldName);
                logger.debug(String.format("found with fieldName ONLY: namespace=%s, fieldName=%s, retrieved label=%s", namespace, fieldName, label));
            }
		} catch (MissingResourceException mre) {
			/** If you can't find the label, generate it thus, "firstName" becomes "First Name".*/
			label = generateLabelValue(fieldName);
            logger.debug(String.format("generateLabelValue generated label: namespace=%s, fieldName=%s, retrieved label=%s", namespace, fieldName, label));
        }

		return label;
	}

	public static String createLabelWithNameSpace(final String namespace, final String fieldName) {

		return createLabelWithNameSpace(namespace, fieldName, CrankContext.getResourceBundleLocator().getBundle());
	}
	
	public static String createLabel(final String fieldName) {
		return createLabelWithNameSpace(null, fieldName, CrankContext.getResourceBundleLocator().getBundle());
	}	
	
	
	/**
	 * Get the tool tip.
	 *
	 * @param namespace
	 *            Namespace is a qualifier usually the name of a Form or Entity.
	 * @param fieldName
	 *            fieldName is the name of the tool tip we want to look up.
	 * @param messageSource
	 *            messageSource
	 *
	 * @return Tool Tip from the Message Source.
	 */
	public static String createToolTipWithNameSpace(final String namespace, final String fieldName,
			final ResourceBundle bundle, final String toolTipType) {

		String toolTip = null;
		try {
			try {
				/** Look for name-space + . + fieldName, e.g., Employee.firstName.toolTip. */
				toolTip = bundle.getString(namespace + '.' + fieldName + '.'
						+ toolTipType);
			} catch (MissingResourceException mre) {
				/** Look for fieldName only, e.g., firstName.toolTip. */
				toolTip = bundle.getString(fieldName + '.' + toolTipType);
			}
		} catch (MissingResourceException mre) {
		}

		return toolTip;
	}
	

	/**
	 * Generate the field. Transforms firstName into First Name. This allows
	 * reasonable defaults for labels.
	 *
	 * @param fieldName
	 *            fieldName
	 *
	 * @return generated label name.
	 */
	public static String generateLabelValue(final String fieldName) {
		
		final StringBuilder buffer = new StringBuilder(fieldName.length() * 2);
		
        
        class GenerationCommand {
            boolean capNextChar = false;
            boolean lastCharWasUpperCase = false;
            boolean lastCharWasNumber = false;
            boolean lastCharWasSpecial = false;
            boolean shouldContinue = true;
            char[] chars = fieldName.toCharArray();
            void processFieldName() {

            	for (int index = 0; index < chars.length; index++) {
        			char cchar = chars[index];
        			shouldContinue = true;

        			processCharWasNumber(buffer, index, cchar);
        			if (!shouldContinue) {
        				continue;
        			}

        			processCharWasUpperCase(buffer, index, cchar);
        			if (!shouldContinue) {
        				continue;
        			}
                    
        			processSpecialChars(buffer, cchar);

        			if (!shouldContinue) {
        				continue;
        			}

                    cchar = processCapitalizeCommand(cchar);

                    cchar = processFirstCharacterCheck(buffer, index, cchar);
        			
                    if (!shouldContinue) {
        				continue;
        			}

        			buffer.append(cchar);
        		}
            	
            }
			private void processCharWasNumber(StringBuilder buffer,
					int index, char cchar) {
				if (lastCharWasSpecial) {
					return;
				}
				
				if (Character.isDigit(cchar)) {
				    
				    if (index!=0 && !lastCharWasNumber) {
				        buffer.append(' ');
				    }
				    
				    lastCharWasNumber = true;                
					buffer.append(cchar);

					this.shouldContinue = false;
				} else {
					lastCharWasNumber = false;
				}
			}
			private char processFirstCharacterCheck(final StringBuilder buffer,
					int index, char cchar) {
				/* Always capitalize the first character. */
				if (index == 0) {
					cchar = Character.toUpperCase(cchar);
					buffer.append(cchar);
					this.shouldContinue = false;        				
				}
				return cchar;
			}
			private char processCapitalizeCommand(char cchar) {
				/* Capitalize the character. */
				if (capNextChar) {
				    capNextChar = false;
				    cchar = Character.toUpperCase(cchar);
				}
				return cchar;
			}
			private void processSpecialChars(final StringBuilder buffer,
					char cchar) {
				lastCharWasSpecial = false;
				/* If the character is '.' or '_' then append a space and mark
				 * the next iteration to capitalize.
				 */
				if (cchar == '.' || cchar == '_') {
				    buffer.append(' ');
				    capNextChar = true;
					lastCharWasSpecial = false;				    
				    this.shouldContinue = false;
				}
				
				
			}
			private void processCharWasUpperCase(final StringBuilder buffer,
					int index, char cchar) {
				/* If the character is uppercase, append a space and keep track
				 * that the last character was uppercase for the next iteration.
				 */
				if (Character.isUpperCase(cchar)) {
				    
				    if (index!=0 && !lastCharWasUpperCase) {
				        buffer.append(' ');
				    }
				    
				    lastCharWasUpperCase = true;                
					buffer.append(cchar);

					this.shouldContinue = false;
				} else {
				    lastCharWasUpperCase = false;
				}
			}
        }

        GenerationCommand gc = new GenerationCommand();
        gc.processFieldName();
        
        /* This is a hack to get address.line_1 to work. */
		return buffer.toString().replace("  ", " ");
	}

}
