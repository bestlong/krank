package org.crank.crud;

import org.crank.crud.test.DbUnitTestBase;
import org.crank.crud.test.dao.EmployeeDAO;
import org.crank.crud.test.model.Department;
import org.crank.crud.test.model.Employee;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.AssertJUnit;

public class GenericDaoJpa3Test extends DbUnitTestBase {
    private GenericDao<Employee, Long> employeeDao;
    private GenericDao<Department, Long> departmentDao;
    private EmployeeDataCreationUtility creationUtility = new EmployeeDataCreationUtility();
    private List<Employee> testEmployees;

    @Override
    public String getDataSetXml() {
        return "data/Employee.xml";
    }

    @BeforeClass(dependsOnGroups = {"initPersist"})
    public void setupEmployeeData() {
        creationUtility.init(employeeDao, departmentDao);
        creationUtility.setupEmployeeData();
        testEmployees = creationUtility.getTestEmployees();
    }

    @AfterClass
    public void deleteTestEmployeeData() {
        try {
            employeeDao.delete(testEmployees);
        } catch (Exception ex) {

        }
    }

    @Test(groups = "modifies")
    public void testUpdateSupport() {
        EmployeeDAO employeeDAO = (EmployeeDAO) this.employeeDao;
        List<Employee> employees = employeeDAO.findEmployeesByDepartment("Engineering");
        AssertJUnit.assertTrue(employees.size() > 0);

        long recordsEffected = employeeDAO.updateEmployeeActiveStatusByLastName("Hightower", true);
        assertEquals(1, recordsEffected);
    }

    @Test(groups = "updates", dependsOnGroups = "modifies")
    public void testDeleteSupport() {
        EmployeeDAO employeeDAO = (EmployeeDAO) this.employeeDao;

        List<Employee> employees = employeeDAO.findEmployeesByDepartment("Engineering");
        AssertJUnit.assertTrue(employees.size() > 0);

        int deletedEmps = employeeDAO.deleteEmployeesByLastName("Hightower");
        AssertJUnit.assertEquals(1, deletedEmps);
    }

    public void setEmployeeDao(final GenericDao<Employee, Long> baseJpaDao) {
        this.employeeDao = baseJpaDao;
    }

    public void setDepartmentDao(GenericDao<Department, Long> departmentDao) {
        this.departmentDao = departmentDao;
    }
}