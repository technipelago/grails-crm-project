package grails.plugins.crm.project

import grails.plugins.crm.contact.CrmContact
import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.CrmEmbeddedAddress
import grails.plugins.crm.core.TenantEntity
import grails.plugins.sequence.SequenceEntity
import grails.util.Holders

/**
 * Created by goran on 2016-04-20.
 */
@TenantEntity
@AuditEntity
@SequenceEntity
class CrmProject {

    private def _crmCoreService

    CrmProject parent
    String number
    String name
    String description
    CrmProjectStatus status
    String username
    String ref
    String currency
    Double budget
    Double actual
    java.sql.Date date1
    java.sql.Date date2
    java.sql.Date date3
    java.sql.Date date4

    CrmEmbeddedAddress address

    static hasMany = [children: CrmProject, items: CrmProjectItem, roles: CrmProjectRole]

    static mappedBy = [children: 'parent']

    static constraints = {
        parent(nullable: true)
        number(maxSize: 20, blank: false, unique: 'tenantId')
        name(maxSize: 80, blank: false)
        description(maxSize: 2000, nullable: true, widget: 'textarea')
        status()
        username(maxSize: 80, nullable: true)
        ref(maxSize: 80, nullable: true)
        date1(nullable: true)
        date2(nullable: true)
        date3(nullable: true)
        date4(nullable: true)
        address(nullable: true)
        currency(maxSize: 4, blank: false)
        budget(min: -999999999d, max: 999999999d, scale: 2)
        actual(min: -999999999d, max: 999999999d, scale: 2)
    }

    static embedded = ['address']

    static mapping = {
        sort 'number': 'asc'
        items sort: 'orderIndex'
        number index: 'crm_project_number_idx'
        name index: 'crm_project_name_idx'
        ref index: 'crm_project_ref_idx'
    }

    static transients = ['customer', 'contact', 'reference', 'active', 'diff', 'budgetEditable', 'dao']

    static taggable = true
    static attachmentable = true
    static dynamicProperties = true
    static relatable = true
    static auditable = true

    static final List<String> BIND_WHITELIST = [
            'parent',
            'number',
            'name',
            'description',
            'status',
            'username',
            'currency',
            'budget',
            'actual',
            'date1',
            'date2',
            'date3',
            'date4'
    ].asImmutable()


    transient CrmContact getCustomer() {
        roles?.find { it.type?.param == 'customer' }?.contact
    }

    transient CrmContact getContact() {
        roles?.find { it.type?.param == 'contact' }?.contact
    }

    transient void setReference(object) {
        ref = object ? crmCoreService.getReferenceIdentifier(object) : null
    }

    transient Object getReference() {
        ref ? crmCoreService.getReference(ref) : null
    }

    transient boolean isActive() {
        status?.isActive()
    }

    transient Double getDiff() {
        calculateBudget() - calculateActual()
    }

    transient boolean isBudgetEditable() {
        !(items)
    }

    def beforeValidate() {
        if (!number) {
            number = getNextSequenceNumber()
        }
        if(! currency) {
            currency = Holders.getConfig().crm.currency.default ?: 'EUR'
        }

        budget = calculateBudget()
        actual = calculateActual()
    }

    protected Double calculateBudget() {
        Double sum
        // If we have no items we just return whatever in budget.
        // This way we can have a project without items.
        if(items == null || items.isEmpty()) {
            sum = this.budget ?: 0
        } else {
            sum = 0
            for (item in items) {
                sum += (item.budget ?: 0)
            }
        }
        sum
    }

    protected Double calculateActual() {
        Double sum
        // If we have no items we just return whatever in actual.
        // This way we can have a project without items.
        if(items == null || items.isEmpty()) {
            sum = this.actual ?: 0
        } else {
            sum = 0
            for (item in items) {
                sum += (item.actual ?: 0)
            }
        }
        sum
    }

    protected Double calculateTotalBudget() {
        Double sum = calculateBudget()
        for (child in children) {
            sum += child.calculateBudget()
        }
        sum
    }

    protected Double calculateTotalActual() {
        Double sum = calculateActual()
        for (child in children) {
            sum += child.calculateActual()
        }
        sum
    }

    // Lazy injection of service.
    private def getCrmCoreService() {
        if (_crmCoreService == null) {
            synchronized (this) {
                if (_crmCoreService == null) {
                    _crmCoreService = this.getDomainClass().getGrailsApplication().getMainContext().getBean('crmCoreService')
                }
            }
        }
        _crmCoreService
    }

    private Map<String, Object> getSelfProperties(List<String> props) {
        props.inject([:]) { m, i ->
            def v = this."$i"
            if (v != null) {
                m[i] = v
            }
            m
        }
    }

    transient Map<String, Object> getDao() {
        final Map<String, Object> map = getSelfProperties(BIND_WHITELIST - 'status')
        map.tenant = tenantId
        map.status = status.dao
        if (address != null) {
            map.address = address.getDao()
        }
        if (parent != null) {
            map.parent = parent.getDao()
        }
        // Removed due to performance impact and lack of requirements.
        //if(ref != null) {
        //    map.reference = getReference().getDao() // TODO No interface
        //}
        return map
    }

    String toString() {
        name.toString()
    }
}
