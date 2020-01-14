package org.crank.validation.readers;

import java.util.List;

import org.crank.validation.ValidatorMetaData;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class AnnotationValidatorMetaDataReaderTest {

	private AnnotationValidatorMetaDataReader annotationValidatorMetaDataReader;
	@BeforeMethod
	protected void setUp() throws Exception {
		annotationValidatorMetaDataReader = new AnnotationValidatorMetaDataReader(); 
	}

	@AfterMethod
	protected void tearDown() throws Exception {
	}

	@Test()
	public void testReadMetaData() {
		List<ValidatorMetaData> list = annotationValidatorMetaDataReader.readMetaData(Employee.class, "age");
		assertNotNull(list);
		assertEquals(2, list.size());
		ValidatorMetaData range = list.get(0);
		assertEquals("range", range.getName());
		assertEquals("10",range.getProperties().get("max"));
		assertEquals("1",range.getProperties().get("min"));
		ValidatorMetaData required = list.get(1);
		assertEquals("required", required.getName());

	}

	@Test
	public void testReadMetaDataWithNonStringValues() {
		List<ValidatorMetaData> list = 
			annotationValidatorMetaDataReader.readMetaData(Employee.class, "iq");
		assertNotNull(list);
		assertEquals(2, list.size());
		ValidatorMetaData range = list.get(1);
		assertEquals("longRange", range.getName());
		assertEquals(100L,range.getProperties().get("max"));
		assertEquals(1L,range.getProperties().get("min"));
		
	}

    
}

