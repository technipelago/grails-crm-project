package grails.plugins.crm.project

import grails.plugins.crm.core.CrmValidationException
import grails.test.spock.IntegrationSpec

/**
 * Created by goran on 2014-08-14.
 */
class CrmProjectServiceSpec extends IntegrationSpec {

    def crmProjectService
    def crmContactService

    def "create project"() {
        given:
        def status = crmProjectService.createProjectStatus(name: "Negotiation", param: "3", true)

        when:
        def project1 = crmProjectService.createProject(name: "Test project", status: status, true)
        def project2 = crmProjectService.createProject(name: "Dummy project", status: status, true)

        then:
        project1.ident()
        project1.customer == null

        when:
        def company = crmContactService.createRelationType(name: "Company", true)
        def technipelago = crmContactService.createCompany(name: "Technipelago AB", true)
        def goran = crmContactService.createPerson(firstName: "Goran", lastName: "Ehrsson", related: [technipelago, company], true)
        def customer = crmProjectService.createProjectRoleType(name: "Customer", true)
        def contact = crmProjectService.createProjectRoleType(name: "Contact", true)

        then:
        technipelago.ident()
        goran.ident()
        customer.ident()
        contact.ident()

        when:
        crmProjectService.addRole(project1, technipelago, customer)
        crmProjectService.addRole(project1, goran, "contact", "Nice guy")
        project1.save(flush: true)

        then:
        project1.customer.name == "Technipelago AB"
        project1.contact.name == "Goran Ehrsson"
        crmProjectService.findProjectsByContact(technipelago).iterator().next().toString() == "Test project"
        crmProjectService.findProjectsByContact(technipelago, "bogusRole").isEmpty()
        crmProjectService.findProjectsByContact(goran).iterator().next().toString() == "Test project"
        crmProjectService.findProjectsByContact(goran, "bogusRole").isEmpty()
        crmProjectService.list().size() == 2
    }

    def "save project"() {
        given:
        def status = crmProjectService.createProjectStatus(name: "Negotiation", param: "3", true)

        when:
        def project1 = crmProjectService.save(new CrmProject(), [name: "Test project", status: status])
        def project2 = crmProjectService.save(new CrmProject(), [name: "Dummy project", status: status])

        then:
        project1.ident()
        project1.customer == null

        when:
        def company = crmContactService.createRelationType(name: "Company", true)
        def technipelago = crmContactService.createCompany(name: "Technipelago AB", true)
        def goran = crmContactService.createPerson(firstName: "Goran", lastName: "Ehrsson", related: [technipelago, company], true)
        def customer = crmProjectService.createProjectRoleType(name: "Customer", true)
        def contact = crmProjectService.createProjectRoleType(name: "Contact", true)

        then:
        technipelago.ident()
        goran.ident()
        customer.ident()
        contact.ident()

        when:
        crmProjectService.save(project1, [customer: technipelago, contact: goran])
        //crmProjectService.addRole(project1, technipelago, customer)
        //crmProjectService.addRole(project1, goran, "contact", "Nice guy")
        //project1.save(flush:true)

        then:
        project1.customer.name == "Technipelago AB"
        project1.contact.name == "Goran Ehrsson"
        crmProjectService.findProjectsByContact(technipelago).iterator().next().toString() == "Test project"
        crmProjectService.findProjectsByContact(technipelago, "bogusRole").isEmpty()
        crmProjectService.findProjectsByContact(goran).iterator().next().toString() == "Test project"
        crmProjectService.findProjectsByContact(goran, "bogusRole").isEmpty()
        crmProjectService.list().size() == 2

        when:
        def pivotal = crmContactService.createCompany(name: "Pivotal", true)
        def grails = crmContactService.createPerson(firstName: "Grails", lastName: "Framework", related: [pivotal, company], true)

        then:
        pivotal.ident()
        grails.ident()

        when:
        crmProjectService.save(project1, [customer: pivotal, contact: grails])

        then:
        project1.customer.name == "Pivotal"
        project1.contact.name == "Grails Framework"

        when:
        crmProjectService.save(project1, [name: "Updated project"])

        then:
        project1.name == "Updated project"
        project1.customer.name == "Pivotal"
        project1.contact.name == "Grails Framework"

        when:
        crmProjectService.save(project1, [date1: "Not a date"])

        then:
        def e = thrown(CrmValidationException)
        e.message == "crmProject.invalid.date.message"
        e.domainInstance == project1
        e.domainInstance.errors
    }

    def "find project"() {
        given:
        def company = crmContactService.createRelationType(name: "Company", true)
        def technipelago = crmContactService.createCompany(name: "Technipelago AB", true)
        def goran = crmContactService.createPerson(firstName: "Goran", lastName: "Ehrsson", related: [technipelago, company], true)
        def status1 = crmProjectService.createProjectStatus(name: "Presentation", param: "2", true)
        def status2 = crmProjectService.createProjectStatus(name: "Negotiation", param: "3", true)

        when:
        def project1 = crmProjectService.createProject(customer: technipelago, contact: goran, name: "Test project", status: status1, true)
        def project2 = crmProjectService.createProject(customer: technipelago, name: "Dummy project", status: status2, true)

        then:
        project1.ident()
        project1.customer != null
        project1.contact != null
        project2.customer != null
        project2.contact == null

        when:
        def result1 = crmProjectService.list([customer: 'Technipelago'], [sort: 'number', order: 'asc'])

        then:
        result1.size() == 2

        when:
        def result2 = crmProjectService.list([customer: 'Goran'], [sort: 'number', order: 'asc'])

        then:
        result2.size() == 1

        when:
        def result3 = crmProjectService.list([customer: 'Sven'], [sort: 'number', order: 'asc'])

        then:
        result3.size() == 0
    }
}
